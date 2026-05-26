package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.model.TaskStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @TempDir
    File tempDir;

    @Override
    protected FileBackedTaskManager createManager() {
        File testFile;
        try {
            testFile = File.createTempFile("tracker", ".csv", tempDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new FileBackedTaskManager(testFile);
    }

    @BeforeEach
    void setUp() {
        tm = createManager();
    }

    private File newTempFile() throws IOException {
        return File.createTempFile("tracker", ".csv", tempDir);
    }

    // ------------------------------------------------------------------
    // Пустой файл
    // ------------------------------------------------------------------

    @Test
    void shouldSaveAndLoadEmptyManager() throws IOException {
        File file = newTempFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task = manager.createTask(new Task(0, "tmp", "tmp"));
        manager.removeTaskById(task.getId());

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loaded.getTasks().isEmpty());
        assertTrue(loaded.getEpics().isEmpty());
        assertTrue(loaded.getSubtasks().isEmpty());
        assertTrue(loaded.getHistory().isEmpty());
    }

    @Test
    void shouldLoadFromInitiallyEmptyFile() throws IOException {
        File file = newTempFile();
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertNotNull(loaded);
        assertTrue(loaded.getTasks().isEmpty());
        assertTrue(loaded.getEpics().isEmpty());
        assertTrue(loaded.getSubtasks().isEmpty());
    }

    // ------------------------------------------------------------------
    // Сохранение нескольких задач
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
        assertTrue(content.contains("TASK"));
        assertTrue(content.contains("EPIC"));
        assertTrue(content.contains("SUBTASK"));
    }

    // ------------------------------------------------------------------
    // Загрузка задач
    // ------------------------------------------------------------------

    @Test
    void shouldRestoreAllTasksAfterLoad() throws IOException {
        File file = newTempFile();
        FileBackedTaskManager original = new FileBackedTaskManager(file);

        Task task1 = original.createTask(new Task(0, "T1", "desc1"));
        Task task2 = original.createTask(new Task(0, "T2", "desc2"));
        Epic epic = original.createEpic(new Epic(0, "E1", "epic desc"));
        original.createSubtask(new Subtask(0, "S1", "sub desc", epic));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertEquals(2, loaded.getTasks().size());
        assertEquals(1, loaded.getEpics().size());
        assertEquals(1, loaded.getSubtasks().size());

        List<Task> loadedTasks = loaded.getTasks();
        assertTrue(loadedTasks.stream().anyMatch(t -> t.getId() == task1.getId()));
        assertTrue(loadedTasks.stream().anyMatch(t -> t.getId() == task2.getId()));

        Subtask loadedSub = loaded.getSubtasks().getFirst();
        assertNotNull(loadedSub.getEpic());
        assertEquals(epic.getId(), loadedSub.getEpic().getId());
    }

    @Test
    void shouldRestoreTaskStatus() throws IOException {
        File file = newTempFile();
        FileBackedTaskManager original = new FileBackedTaskManager(file);

        Task task = original.createTask(new Task(0, "Task", "desc"));
        task.setStatus(TaskStatus.IN_PROGRESS);
        original.updateTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        assertEquals(TaskStatus.IN_PROGRESS, loaded.getTasks().getFirst().getStatus());
    }

    // ------------------------------------------------------------------
    // Сохранение duration и startTime
    // ------------------------------------------------------------------

    @Test
    void shouldSaveAndRestoreTaskTimingFields() throws IOException {
        File file = newTempFile();
        FileBackedTaskManager original = new FileBackedTaskManager(file);

        Task task = new Task(0, "Timed", "desc");
        task.setStartTime(LocalDateTime.of(2025, 6, 1, 9, 0));
        task.setDuration(Duration.ofMinutes(90));
        original.createTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        Task restoredTask = loaded.getTasks().getFirst();

        assertEquals(LocalDateTime.of(2025, 6, 1, 9, 0), restoredTask.getStartTime(),
                "startTime должен восстановиться из файла");
        assertEquals(Duration.ofMinutes(90), restoredTask.getDuration(),
                "duration должен восстановиться из файла");
        assertEquals(LocalDateTime.of(2025, 6, 1, 10, 30), restoredTask.getEndTime(),
                "endTime должен корректно рассчитываться после загрузки");
    }

    // ------------------------------------------------------------------
    // История
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

        assertEquals(3, history.size());
        assertEquals(task1.getId(), history.get(0).getId());
        assertEquals(epic.getId(), history.get(1).getId());
        assertEquals(task2.getId(), history.get(2).getId());
    }

    @Test
    void shouldSaveEmptyHistory() throws IOException {
        File file = newTempFile();
        FileBackedTaskManager original = new FileBackedTaskManager(file);
        original.createTask(new Task(0, "T1", "d1"));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        assertTrue(loaded.getHistory().isEmpty());
    }

    // ------------------------------------------------------------------
    // Уникальные id после загрузки
    // ------------------------------------------------------------------

    @Test
    void shouldGenerateUniqueIdsAfterLoad() throws IOException {
        File file = newTempFile();
        FileBackedTaskManager original = new FileBackedTaskManager(file);
        Task existing = original.createTask(new Task(0, "Existing", "desc"));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        Task newTask = loaded.createTask(new Task(0, "New", "desc"));

        assertNotEquals(existing.getId(), newTask.getId());
    }

    // ------------------------------------------------------------------
    // Связь эпика и подзадач
    // ------------------------------------------------------------------

    @Test
    void shouldRestoreEpicSubtaskLink() throws IOException {
        File file = newTempFile();
        FileBackedTaskManager original = new FileBackedTaskManager(file);

        Epic epic = original.createEpic(new Epic(0, "Epic", "desc"));
        original.createSubtask(new Subtask(0, "Sub1", "d", epic));
        original.createSubtask(new Subtask(0, "Sub2", "d", epic));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        assertEquals(2, loaded.getEpicSubtasks(epic.getId()).size());
    }

    // ------------------------------------------------------------------
    // Исключения при работе с файлами
    // ------------------------------------------------------------------

    @Test
    void shouldThrowManagerSaveExceptionOnUnwritableFile() {
        File badFile = new File("/nonexistent/path/tracker.csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(badFile);

        assertThrows(ManagerSaveException.class,
                () -> manager.createTask(new Task(0, "T", "d")),
                "Ошибка записи в файл должна пробрасываться как ManagerSaveException");
    }

    @Test
    void shouldThrowManagerSaveExceptionOnUnreadableFile() {
        File badFile = new File("/nonexistent/path/tracker.csv");

        assertThrows(ManagerSaveException.class,
                () -> FileBackedTaskManager.loadFromFile(badFile),
                "Ошибка чтения файла должна пробрасываться как ManagerSaveException");
    }
}
