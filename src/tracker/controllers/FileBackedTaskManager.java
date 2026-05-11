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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Менеджер задач с автосохранением состояния в CSV-файл.
 * Наследует всю логику работы от InMemoryTaskManager,
 * дополняя модифицирующие операции вызовом save().
 *
 * Формат CSV:
 *   id,type,name,status,description,epic
 *   (пустая строка)
 *   history: id1,id2,...
 */
public class FileBackedTaskManager extends InMemoryTaskManager {

    private static final String HEADER = "id,type,name,status,description,epic";

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

        for (Task task : getTasks()) {
            lines.add(toString(task));
        }
        for (Epic epic : getEpics()) {
            lines.add(toString(epic));
        }
        for (Subtask subtask : getSubtasks()) {
            lines.add(toString(subtask));
        }

        // Пустая строка-разделитель между задачами и историей
        lines.add("");

        // История: перечисляем id через запятую
        List<Task> history = getHistory();
        if (!history.isEmpty()) {
            StringBuilder sb = new StringBuilder("history:");
            for (int i = 0; i < history.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(history.get(i).getId());
            }
            lines.add(sb.toString());
        }

        try {
            Files.writeString(file.toPath(), String.join("\n", lines) + "\n", StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось сохранить состояние менеджера в файл: " + file, e);
        }
    }

    private static String toString(Task task) {
        TaskType type;
        String epicId = "";

        if (task instanceof Subtask subtask) {
            type = TaskType.SUBTASK;
            Epic epic = subtask.getEpic();
            if (epic != null) {
                epicId = String.valueOf(epic.getId());
            }
        } else if (task instanceof Epic) {
            type = TaskType.EPIC;
        } else {
            type = TaskType.TASK;
        }

        return String.join(",",
                String.valueOf(task.getId()),
                type.name(),
                task.getName(),
                task.getStatus().name(),
                task.getDescription(),
                epicId
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

        if (content.isBlank()) {
            return manager;
        }

        String[] lines = content.split("\n");

        // Первая строка — заголовок, пропускаем.
        // Читаем задачи до пустой строки.
        // После пустой строки — строка "history:..."

        // Сначала соберём все задачи в промежуточную карту по id,
        // чтобы потом связать подзадачи с эпиками.
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
            if (task != null) {
                allById.put(task.getId(), task);
            }
        }

        // Восстанавливаем задачи в менеджер через внутренние карты,
        // чтобы не вызывать save() на каждом шаге (используем метод родителя напрямую).
        // Для этого сначала — Task и Epic (без подзадач), потом — Subtask.
        for (Task task : allById.values()) {
            if (task instanceof Epic epic) {
                manager.restoreEpic(epic);
            } else if (!(task instanceof Subtask)) {
                manager.restoreTask(task);
            }
        }

        for (Task task : allById.values()) {
            if (task instanceof Subtask subtask) {
                // epicId временно хранится в поле epic как «заглушка» с нужным id
                Epic storedEpic = manager.findEpicById(subtask.getEpic().getId());
                if (storedEpic != null) {
                    subtask.setEpic(storedEpic);
                    storedEpic.addSubTask(subtask);
                }
                manager.restoreSubtask(subtask);
            }
        }

        // Восстанавливаем историю — добавляем в HistoryManager напрямую,
        // чтобы не создавать новые просмотры через getTask/getEpic/getSubtask
        for (int id : historyIds) {
            Task task = allById.get(id);
            if (task != null) {
                manager.restoreHistory(task);
            }
        }

        // После полной загрузки синхронизируем счётчик id
        int maxId = allById.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
        manager.syncCounter(maxId);

        return manager;
    }

    /**
     * Десериализует задачу из CSV-строки.
     * Для Subtask epic заполняется «заглушкой» только с id.
     */
    private static Task fromString(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 5) {
            return null;
        }

        int id = Integer.parseInt(parts[0].strip());
        TaskType type = TaskType.valueOf(parts[1].strip());
        String name = parts[2].strip();
        TaskStatus status = TaskStatus.valueOf(parts[3].strip());
        String description = parts[4].strip();

        return switch (type) {
            case TASK -> {
                Task task = new Task(id, name, description);
                task.setStatus(status);
                yield task;
            }
            case EPIC -> {
                Epic epic = new Epic(id, name, description);
                // Статус эпика пересчитывается по подзадачам; здесь восстанавливаем напрямую
                epic.restoreStatus(status);
                yield epic;
            }
            case SUBTASK -> {
                // Создаём заглушку эпика только с id — связь восстановится позже
                int epicId = Integer.parseInt(parts[5].strip());
                Epic epicStub = new Epic(epicId, "", "");
                Subtask subtask = new Subtask(id, name, description, epicStub);
                subtask.restoreStatus(status);
                yield subtask;
            }
        };
    }

    // -------------------------------------------------------------------------
    // Переопределение модифицирующих методов с автосохранением
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

    // getTask / getEpic / getSubtask меняют историю — тоже сохраняем
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
