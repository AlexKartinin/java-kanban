package tracker.controllers;

import org.junit.jupiter.api.Test;
import tracker.exceptions.NotFoundException;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Базовый абстрактный класс с тестами для интерфейса TaskManager.
 * Каждая конкретная реализация (InMemoryTaskManager, FileBackedTaskManager)
 * наследует эти тесты и добавляет свои.
 */
public abstract class TaskManagerTest<T extends TaskManager> {

    protected T tm;

    protected abstract T createManager();

    // -------------------------------------------------------------------------
    // Создание задач
    // -------------------------------------------------------------------------

    @Test
    void shouldCreateAndReturnTask() {
        Task task = tm.createTask(new Task(0, "Task", "Desc"));
        assertNotNull(task);
        assertTrue(task.getId() > 0);
        assertEquals(TaskStatus.NEW, task.getStatus());
    }

    @Test
    void shouldCreateAndReturnEpic() {
        Epic epic = tm.createEpic(new Epic(0, "Epic", "Desc"));
        assertNotNull(epic);
        assertTrue(epic.getId() > 0);
    }

    @Test
    void shouldCreateAndReturnSubtask() {
        Epic epic = tm.createEpic(new Epic(0, "Epic", "Desc"));
        Subtask sub = tm.createSubtask(new Subtask(0, "Sub", "Desc", epic));
        assertNotNull(sub);
        assertNotNull(sub.getEpic(), "Подзадача должна быть связана с эпиком");
        assertEquals(epic.getId(), sub.getEpic().getId());
    }

    @Test
    void shouldThrowNotFoundForUnknownTaskId() {
        assertThrows(NotFoundException.class, () -> tm.getTask(9999));
    }

    @Test
    void shouldThrowNotFoundForUnknownEpicId() {
        assertThrows(NotFoundException.class, () -> tm.getEpic(9999));
    }

    @Test
    void shouldThrowNotFoundForUnknownSubtaskId() {
        assertThrows(NotFoundException.class, () -> tm.getSubtask(9999));
    }

    // -------------------------------------------------------------------------
    // Epic статус — граничные условия
    // -------------------------------------------------------------------------

    @Test
    void epicStatusShouldBeNewWhenAllSubtasksNew() {
        Epic epic = tm.createEpic(new Epic(0, "E", "d"));
        tm.createSubtask(new Subtask(0, "S1", "d", epic));
        tm.createSubtask(new Subtask(0, "S2", "d", epic));

        assertEquals(TaskStatus.NEW, tm.getEpic(epic.getId()).getStatus(),
                "Все подзадачи NEW → эпик NEW");
    }

    @Test
    void epicStatusShouldBeDoneWhenAllSubtasksDone() {
        Epic epic = tm.createEpic(new Epic(0, "E", "d"));
        Subtask s1 = tm.createSubtask(new Subtask(0, "S1", "d", epic));
        Subtask s2 = tm.createSubtask(new Subtask(0, "S2", "d", epic));

        s1.setStatus(TaskStatus.DONE);
        s2.setStatus(TaskStatus.DONE);

        assertEquals(TaskStatus.DONE, tm.getEpic(epic.getId()).getStatus(),
                "Все подзадачи DONE → эпик DONE");
    }

    @Test
    void epicStatusShouldBeInProgressWhenSubtasksMixed() {
        Epic epic = tm.createEpic(new Epic(0, "E", "d"));
        Subtask s1 = tm.createSubtask(new Subtask(0, "S1", "d", epic));
        Subtask s2 = tm.createSubtask(new Subtask(0, "S2", "d", epic));

        s1.setStatus(TaskStatus.NEW);
        s2.setStatus(TaskStatus.DONE);

        assertEquals(TaskStatus.IN_PROGRESS, tm.getEpic(epic.getId()).getStatus(),
                "Подзадачи NEW и DONE → эпик IN_PROGRESS");
    }

    @Test
    void epicStatusShouldBeInProgressWhenAllSubtasksInProgress() {
        Epic epic = tm.createEpic(new Epic(0, "E", "d"));
        Subtask s1 = tm.createSubtask(new Subtask(0, "S1", "d", epic));
        Subtask s2 = tm.createSubtask(new Subtask(0, "S2", "d", epic));

        s1.setStatus(TaskStatus.IN_PROGRESS);
        s2.setStatus(TaskStatus.IN_PROGRESS);

        assertEquals(TaskStatus.IN_PROGRESS, tm.getEpic(epic.getId()).getStatus(),
                "Все подзадачи IN_PROGRESS → эпик IN_PROGRESS");
    }

    @Test
    void epicStatusShouldBeNewWhenNoSubtasks() {
        Epic epic = tm.createEpic(new Epic(0, "E", "d"));
        assertEquals(TaskStatus.NEW, tm.getEpic(epic.getId()).getStatus(),
                "Нет подзадач → эпик NEW");
    }

    // -------------------------------------------------------------------------
    // getEpicSubtasks
    // -------------------------------------------------------------------------

    @Test
    void getEpicSubtasksShouldReturnLinkedSubtasks() {
        Epic epic = tm.createEpic(new Epic(0, "E", "d"));
        Subtask s1 = tm.createSubtask(new Subtask(0, "S1", "d", epic));
        Subtask s2 = tm.createSubtask(new Subtask(0, "S2", "d", epic));

        List<Subtask> result = tm.getEpicSubtasks(epic.getId());
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(s -> s.getId() == s1.getId()));
        assertTrue(result.stream().anyMatch(s -> s.getId() == s2.getId()));
    }

    @Test
    void getEpicSubtasksShouldReturnEmptyForUnknownEpic() {
        assertTrue(tm.getEpicSubtasks(9999).isEmpty());
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @Test
    void shouldUpdateTask() {
        Task task = tm.createTask(new Task(0, "Old", "d"));
        Task updated = new Task(task.getId(), "New", "d2");
        updated.setStatus(TaskStatus.DONE);
        tm.updateTask(updated);

        assertEquals(TaskStatus.DONE, tm.getTask(task.getId()).getStatus());
    }

    @Test
    void shouldUpdateSubtask() {
        Epic epic = tm.createEpic(new Epic(0, "E", "d"));
        Subtask sub = tm.createSubtask(new Subtask(0, "S", "d", epic));

        sub.setStatus(TaskStatus.IN_PROGRESS);
        tm.updateSubtask(sub);

        assertEquals(TaskStatus.IN_PROGRESS, tm.getSubtask(sub.getId()).getStatus());
    }

    // -------------------------------------------------------------------------
    // Remove
    // -------------------------------------------------------------------------

    @Test
    void shouldRemoveTaskById() {
        Task task = tm.createTask(new Task(0, "T", "d"));
        tm.removeTaskById(task.getId());
        assertThrows(NotFoundException.class, () -> tm.getTask(task.getId()));
    }

    @Test
    void shouldRemoveEpicWithSubtasksById() {
        Epic epic = tm.createEpic(new Epic(0, "E", "d"));
        Subtask sub = tm.createSubtask(new Subtask(0, "S", "d", epic));
        tm.removeEpicById(epic.getId());

        assertThrows(NotFoundException.class, () -> tm.getEpic(epic.getId()));
        assertThrows(NotFoundException.class, () -> tm.getSubtask(sub.getId()));
    }

    @Test
    void shouldRemoveSubtaskById() {
        Epic epic = tm.createEpic(new Epic(0, "E", "d"));
        Subtask sub = tm.createSubtask(new Subtask(0, "S", "d", epic));
        tm.removeSubtaskById(sub.getId());

        assertThrows(NotFoundException.class, () -> tm.getSubtask(sub.getId()));
        assertTrue(tm.getEpicSubtasks(epic.getId()).isEmpty());
    }

    // -------------------------------------------------------------------------
    // История
    // -------------------------------------------------------------------------

    @Test
    void historyShouldBeEmptyInitially() {
        assertTrue(tm.getHistory().isEmpty());
    }

    @Test
    void historyShouldNotContainDuplicates() {
        Task task = tm.createTask(new Task(0, "T", "d"));
        tm.getTask(task.getId());
        tm.getTask(task.getId());
        tm.getTask(task.getId());

        assertEquals(1, tm.getHistory().size());
    }

    // -------------------------------------------------------------------------
    // getPrioritizedTasks
    // -------------------------------------------------------------------------

    @Test
    void prioritizedTasksShouldBeSortedByStartTime() {
        LocalDateTime base = LocalDateTime.of(2025, 1, 1, 10, 0);

        Task t1 = new Task(0, "T1", "d");
        t1.setStartTime(base.plusHours(2));
        t1.setDuration(Duration.ofMinutes(30));

        Task t2 = new Task(0, "T2", "d");
        t2.setStartTime(base);
        t2.setDuration(Duration.ofMinutes(30));

        Task t3 = new Task(0, "T3", "d");
        t3.setStartTime(base.plusHours(1));
        t3.setDuration(Duration.ofMinutes(30));

        Task c1 = tm.createTask(t1);
        Task c2 = tm.createTask(t2);
        Task c3 = tm.createTask(t3);

        List<Task> prioritized = tm.getPrioritizedTasks();
        assertEquals(3, prioritized.size());
        assertEquals(c2.getId(), prioritized.get(0).getId(), "Первой должна быть самая ранняя задача");
        assertEquals(c3.getId(), prioritized.get(1).getId());
        assertEquals(c1.getId(), prioritized.get(2).getId());
    }

    @Test
    void tasksWithoutStartTimeShouldNotBeInPrioritizedList() {
        Task withTime = new Task(0, "T1", "d");
        withTime.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        withTime.setDuration(Duration.ofMinutes(30));

        Task withoutTime = new Task(0, "T2", "d");

        tm.createTask(withTime);
        tm.createTask(withoutTime);

        List<Task> prioritized = tm.getPrioritizedTasks();
        assertEquals(1, prioritized.size(), "Задача без startTime не должна попасть в список");
    }

    // -------------------------------------------------------------------------
    // Пересечения
    // -------------------------------------------------------------------------

    @Test
    void shouldThrowWhenTasksOverlap() {
        LocalDateTime base = LocalDateTime.of(2025, 1, 1, 10, 0);

        Task t1 = new Task(0, "T1", "d");
        t1.setStartTime(base);
        t1.setDuration(Duration.ofMinutes(60));
        tm.createTask(t1);

        Task t2 = new Task(0, "T2", "d");
        t2.setStartTime(base.plusMinutes(30));
        t2.setDuration(Duration.ofMinutes(60));

        assertThrows(IllegalArgumentException.class, () -> tm.createTask(t2),
                "Пересекающиеся задачи должны вызывать исключение");
    }

    @Test
    void shouldAllowNonOverlappingTasks() {
        LocalDateTime base = LocalDateTime.of(2025, 1, 1, 10, 0);

        Task t1 = new Task(0, "T1", "d");
        t1.setStartTime(base);
        t1.setDuration(Duration.ofMinutes(60));

        Task t2 = new Task(0, "T2", "d");
        t2.setStartTime(base.plusMinutes(60));
        t2.setDuration(Duration.ofMinutes(60));

        assertDoesNotThrow(() -> {
            tm.createTask(t1);
            tm.createTask(t2);
        }, "Непересекающиеся задачи должны создаваться без исключений");
    }

    // -------------------------------------------------------------------------
    // Epic timing
    // -------------------------------------------------------------------------

    @Test
    void epicTimingShouldBeCalculatedFromSubtasks() {
        Epic epic = tm.createEpic(new Epic(0, "E", "d"));

        LocalDateTime start1 = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime start2 = LocalDateTime.of(2025, 1, 1, 12, 0);

        Subtask s1 = new Subtask(0, "S1", "d", epic);
        s1.setStartTime(start1);
        s1.setDuration(Duration.ofMinutes(60));
        tm.createSubtask(s1);

        Subtask s2 = new Subtask(0, "S2", "d", epic);
        s2.setStartTime(start2);
        s2.setDuration(Duration.ofMinutes(30));
        tm.createSubtask(s2);

        Epic loaded = tm.getEpic(epic.getId());
        assertEquals(start1, loaded.getStartTime(), "startTime эпика = startTime самой ранней подзадачи");
        assertEquals(start2.plusMinutes(30), loaded.getEndTime(), "endTime эпика = endTime самой поздней подзадачи");
        assertEquals(Duration.ofMinutes(90), loaded.getDuration(), "duration эпика = сумма duration подзадач");
    }
}
