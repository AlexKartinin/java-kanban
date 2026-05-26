package tracker.controllers;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.model.TaskStatus;
import tracker.model.TaskType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Менеджер задач с автосохранением состояния в CSV-файл.
 * Формат CSV:
 *   id,type,name,status,description,epic,duration,startTime
 *   (пустая строка)
 *   history:id1,id2,...
 */
public class FileBackedTaskManager extends InMemoryTaskManager {

    private static final String HEADER = "id,type,name,status,description,epic,duration,startTime";

    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    // -------------------------------------------------------------------------
    // Сохранение
    // -------------------------------------------------------------------------

    protected void save() {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);

        getTasks().stream().map(FileBackedTaskManager::toString).forEach(lines::add);
        getEpics().stream().map(FileBackedTaskManager::toString).forEach(lines::add);
        getSubtasks().stream().map(FileBackedTaskManager::toString).forEach(lines::add);

        lines.add("");

        List<Task> history = getHistory();
        if (!history.isEmpty()) {
            String historyLine = "history:" + history.stream()
                    .map(t -> String.valueOf(t.getId()))
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");
            lines.add(historyLine);
        }

        try {
            Files.writeString(file.toPath(), String.join("\n", lines) + "\n", StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось сохранить состояние менеджера в файл: " + file, e);
        }
    }

    private static String toString(Task task) {
        String epicId = "";
        if (task.getType() == TaskType.SUBTASK) {
            Epic epic = ((Subtask) task).getEpic();
            if (epic != null) epicId = String.valueOf(epic.getId());
        }

        String durationStr = task.getDuration() != null
                ? String.valueOf(task.getDuration().toMinutes())
                : "";
        String startTimeStr = task.getStartTime() != null
                ? task.getStartTime().toString()
                : "";

        return String.join(",",
                String.valueOf(task.getId()),
                task.getType().name(),
                task.getName(),
                task.getStatus().name(),
                task.getDescription(),
                epicId,
                durationStr,
                startTimeStr
        );
    }

    // -------------------------------------------------------------------------
    // Загрузка
    // -------------------------------------------------------------------------

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        String content;
        try {
            content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось прочитать файл: " + file, e);
        }

        if (content.isBlank()) return manager;

        String[] lines = content.split("\n");
        Map<Integer, Task> allById = new LinkedHashMap<>();
        boolean historySection = false;
        List<Integer> historyIds = new ArrayList<>();

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].strip();
            if (line.isEmpty()) {
                historySection = true;
                continue;
            }

            if (historySection) {
                if (line.startsWith("history:")) {
                    String ids = line.substring("history:".length());
                    if (!ids.isBlank()) {
                        for (String part : ids.split(",")) {
                            historyIds.add(Integer.parseInt(part.strip()));
                        }
                    }
                }
                continue;
            }

            Task task = fromString(line);
            if (task != null) allById.put(task.getId(), task);
        }

        // Сначала Task и Epic
        allById.values().stream()
                .filter(t -> t.getType() == TaskType.TASK)
                .forEach(manager::restoreTask);

        allById.values().stream()
                .filter(t -> t.getType() == TaskType.EPIC)
                .forEach(t -> manager.restoreEpic((Epic) t));

        // Затем Subtask — связываем с эпиком
        allById.values().stream()
                .filter(t -> t.getType() == TaskType.SUBTASK)
                .map(t -> (Subtask) t)
                .forEach(subtask -> {
                    Epic storedEpic = manager.findEpicById(subtask.getEpic().getId());
                    if (storedEpic != null) {
                        subtask.setEpic(storedEpic);
                        storedEpic.addSubTask(subtask);
                    }
                    manager.restoreSubtask(subtask);
                });

        // История
        historyIds.stream()
                .map(allById::get)
                .filter(Objects::nonNull)
                .forEach(manager::restoreHistory);

        // Синхронизируем счётчик
        int maxId = allById.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
        manager.syncCounter(maxId);

        return manager;
    }

    private static Task fromString(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 5) return null;

        int id = Integer.parseInt(parts[0].strip());
        TaskType type = TaskType.valueOf(parts[1].strip());
        String name = parts[2].strip();
        TaskStatus status = TaskStatus.valueOf(parts[3].strip());
        String description = parts[4].strip();

        // Опциональные поля
        Duration duration = null;
        LocalDateTime startTime = null;
        if (parts.length > 6 && !parts[6].isBlank()) {
            duration = Duration.ofMinutes(Long.parseLong(parts[6].strip()));
        }
        if (parts.length > 7 && !parts[7].isBlank()) {
            startTime = LocalDateTime.parse(parts[7].strip());
        }

        return switch (type) {
            case TASK -> {
                Task task = new Task(id, name, description);
                task.restoreStatus(status);
                task.setDuration(duration);
                task.setStartTime(startTime);
                yield task;
            }
            case EPIC -> {
                Epic epic = new Epic(id, name, description);
                epic.restoreStatus(status);
                yield epic;
            }
            case SUBTASK -> {
                int epicId = Integer.parseInt(parts[5].strip());
                Epic epicStub = new Epic(epicId, "", "");
                Subtask subtask = new Subtask(id, name, description, epicStub);
                subtask.restoreStatus(status);
                subtask.setDuration(duration);
                subtask.setStartTime(startTime);
                yield subtask;
            }
        };
    }

    // -------------------------------------------------------------------------
    // Переопределение методов с автосохранением
    // -------------------------------------------------------------------------

    @Override
    public Task createTask(Task task) {
        Task result = super.createTask(task);
        save();
        return result;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic result = super.createEpic(epic);
        save();
        return result;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask result = super.createSubtask(subtask);
        save();
        return result;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void removeTaskById(int id) {
        super.removeTaskById(id);
        save();
    }

    @Override
    public void removeEpicById(int id) {
        super.removeEpicById(id);
        save();
    }

    @Override
    public void removeSubtaskById(int id) {
        super.removeSubtaskById(id);
        save();
    }

    @Override
    public void removeTasks() {
        super.removeTasks();
        save();
    }

    @Override
    public void removeEpics() {
        super.removeEpics();
        save();
    }

    @Override
    public void removeSubtasks() {
        super.removeSubtasks();
        save();
    }

    @Override
    public Task getTask(int id) {
        Task result = super.getTask(id);
        save();
        return result;
    }

    @Override
    public Epic getEpic(int id) {
        Epic result = super.getEpic(id);
        save();
        return result;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask result = super.getSubtask(id);
        save();
        return result;
    }
}
