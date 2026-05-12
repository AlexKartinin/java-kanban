package tracker.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.model.TaskStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    @TempDir
    File tempDir;

    private File newTempFile() throws IOException {
        return File.createTempFile("tracker", ".csv", tempDir);
    }

    // ------------------------------------------------------------------
    // 1. Пустой файл
    // ------------------------------------------------------------------

    @Test
    void shouldSaveAndLoadEmptyManager() throws IOException {
        File file = newTempFile();

        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        // Сохраняем пустое состояние (через createTask ничего не вызывается,
        // вызовем save напрямую через создание и немедленное удаление задачи)
        Task task = manager.createTask(new Task(0, "tmp", "tmp"));
        manager.removeTaskById(task.getId());

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loaded.getTasks().isEmpty(), "Задач не должно быть");
        assertTrue(loaded.getEpics().isEmpty(), "Эпиков не должно быть");
        assertTrue(loaded.getSubtasks().isEmpty(), "Подзадач не должно быть");
        assertTrue(loaded.getHistory().isEmpty(), "История должна быть пустой");
    }

    @Test
    void shouldLoadFromInitiallyEmptyFile() throws IOException {
        File file = newTempFile();
        // Файл существует, но пустой

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertNotNull(loaded);
        assertTrue(loaded.getTasks().isEmpty());
        assertTrue(loaded.getEpics().isEmpty());
        assertTrue(loaded.getSubtasks().isEmpty());
    }

    // ------------------------------------------------------------------
    // 2. Сохранение нескольких задач
    // ------------------------------------------------------------------

    @Test
    void shouldSaveAllTaskTypes() throws IOException {
        File file = newTempFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        manager.createTask(new Task(0, "Задача 1", "Описание 1"));
        manager.createTask(new Task(0, "Задача 2", "Описание 2"));
        Epic epic = manager.createEpic(new Epic(0, "Эпик 1", "Эпик"));
        manager.createSubtask(new Subtask(0, "Подзадача 1", "Подзадача", epic));

        String content = Files.readString(file.toPath());

        assertTrue(content.contains("TASK"), "Файл должен содержать тип TASK");
        assertTrue(content.contains("EPIC"), "Файл должен содержать тип EPIC");
        assertTrue(content.contains("SUBTASK"), "Файл должен содержать тип SUBTASK");
    }

    // ------------------------------------------------------------------
    // 3. Загрузка нескольких задач
    // ------------------------------------------------------------------

    @Test
    void shouldRestoreAllTasksAfterLoad() throws IOException {
        File file = newTempFile();
        FileBackedTaskManager original = new FileBackedTaskManager(file);

        Task task1 = original.createTask(new Task(0, "T1", "desc1"));
        Task task2 = original.createTask(new Task(0, "T2", "desc2"));
        Epic epic = original.createEpic(new Epic(0, "E1", "epic desc"));
        Subtask sub = original.createSubtask(new Subtask(0, "S1", "sub desc", epic));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertEquals(2, loaded.getTasks().size(), "Должны загрузиться 2 задачи");
        assertEquals(1, loaded.getEpics().size(), "Должен загрузиться 1 эпик");
        assertEquals(1, loaded.getSubtasks().size(), "Должна загрузиться 1 подзадача");

        // Проверяем, что id совпадают
        List<Task> loadedTasks = loaded.getTasks();
        assertTrue(loadedTasks.stream().anyMatch(t -> t.getId() == task1.getId()), "task1 должна быть в загруженном менеджере");
        assertTrue(loadedTasks.stream().anyMatch(t -> t.getId() == task2.getId()), "task2 должна быть в загруженном менеджере");

        // Подзадача должна быть связана с правильным эпиком
        Subtask loadedSub = loaded.getSubtasks().getFirst();
        assertNotNull(loadedSub.getEpic(), "Подзадача должна быть связана с эпиком");
        assertEquals(epic.getId(), loadedSub.getEpic().getId(), "id эпика подзадачи должен совпадать");
    }

    @Test
    void shouldRestoreTaskStatus() throws IOException {
        File file = newTempFile();
        FileBackedTaskManager original = new FileBackedTaskManager(file);

        Task task = original.createTask(new Task(0, "Task", "desc"));
        task.setStatus(TaskStatus.IN_PROGRESS);
        original.updateTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        Task loadedTask = loaded.getTasks().getFirst();
        assertEquals(TaskStatus.IN_PROGRESS, loadedTask.getStatus(), "Статус задачи должен восстановиться");
    }

    // ------------------------------------------------------------------
    // 4. История
    // ------------------------------------------------------------------

    @Test
    void shouldSaveAndRestoreHistory() throws IOException {
        File file = newTempFile();
        FileBackedTaskManager original = new FileBackedTaskManager(file);

        Task task1 = original.createTask(new Task(0, "T1", "d1"));
        Task task2 = original.createTask(new Task(0, "T2", "d2"));
        Epic epic = original.createEpic(new Epic(0, "E1", "e"));

        original.getTask(task1.getId());
        original.getEpic(epic.getId());
        original.getTask(task2.getId());

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        List<Task> history = loaded.getHistory();

        assertEquals(3, history.size(), "История должна содержать 3 записи");
        assertEquals(task1.getId(), history.get(0).getId(), "Первой в истории должна быть task1");
        assertEquals(epic.getId(), history.get(1).getId(), "Второй — epic");
        assertEquals(task2.getId(), history.get(2).getId(), "Третьей — task2");
    }

    @Test
    void shouldSaveEmptyHistory() throws IOException {
        File file = newTempFile();
        FileBackedTaskManager original = new FileBackedTaskManager(file);

        original.createTask(new Task(0, "T1", "d1"));
        // Ни один getTask не вызван — история пуста

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loaded.getHistory().isEmpty(), "История должна быть пустой");
    }

    // ------------------------------------------------------------------
    // 5. Новые задачи после загрузки получают уникальные id
    // ------------------------------------------------------------------

    @Test
    void shouldGenerateUniqueIdsAfterLoad() throws IOException {
        File file = newTempFile();
        FileBackedTaskManager original = new FileBackedTaskManager(file);

        Task existing = original.createTask(new Task(0, "Existing", "desc"));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        Task newTask = loaded.createTask(new Task(0, "New", "desc"));

        assertNotEquals(existing.getId(), newTask.getId(),
                "Новая задача не должна получить id, уже занятый существующей задачей");
    }

    // ------------------------------------------------------------------
    // 6. Связь эпика и подзадач восстанавливается корректно
    // ------------------------------------------------------------------

    @Test
    void shouldRestoreEpicSubtaskLink() throws IOException {
        File file = newTempFile();
        FileBackedTaskManager original = new FileBackedTaskManager(file);

        Epic epic = original.createEpic(new Epic(0, "Epic", "desc"));
        Subtask sub1 = original.createSubtask(new Subtask(0, "Sub1", "d", epic));
        Subtask sub2 = original.createSubtask(new Subtask(0, "Sub2", "d", epic));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        List<Subtask> epicSubtasks = loaded.getEpicSubtasks(epic.getId());
        assertEquals(2, epicSubtasks.size(), "Эпик должен содержать 2 подзадачи после загрузки");
    }
}
