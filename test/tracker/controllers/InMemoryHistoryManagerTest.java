package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void shouldAddTasksInViewOrder() {
        Task task1 = new Task(1, "Task1", "Desc1");
        Task task2 = new Task(2, "Task2", "Desc2");
        Task task3 = new Task(3, "Task3", "Desc3");

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size(), "История должна содержать три задачи");
        assertEquals(1, history.get(0).getId(), "Первая задача должна быть первой в истории");
        assertEquals(2, history.get(1).getId(), "Вторая задача должна быть второй в истории");
        assertEquals(3, history.get(2).getId(), "Третья задача должна быть третьей в истории");
    }

    @Test
    void shouldKeepOnlyLastOccurrenceOfTask() {
        Task task1 = new Task(1, "Task1", "Desc1");
        Task task2 = new Task(2, "Task2", "Desc2");

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "В истории должны остаться только уникальные задачи");
        assertEquals(2, history.get(0).getId(), "Task2 должна остаться первой");
        assertEquals(1, history.get(1).getId(), "Повторно добавленная Task1 должна перейти в конец");
    }

    @Test
    void shouldRemoveTaskFromBeginning() {
        historyManager.add(new Task(1, "Task1", "Desc1"));
        historyManager.add(new Task(2, "Task2", "Desc2"));
        historyManager.add(new Task(3, "Task3", "Desc3"));

        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "После удаления должно остаться две задачи");
        assertEquals(2, history.get(0).getId(), "После удаления головы первой должна стать Task2");
        assertEquals(3, history.get(1).getId(), "Task3 должна остаться последней");
    }

    @Test
    void shouldRemoveTaskFromMiddle() {
        historyManager.add(new Task(1, "Task1", "Desc1"));
        historyManager.add(new Task(2, "Task2", "Desc2"));
        historyManager.add(new Task(3, "Task3", "Desc3"));

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "После удаления должно остаться две задачи");
        assertEquals(1, history.get(0).getId(), "Task1 должна остаться первой");
        assertEquals(3, history.get(1).getId(), "Task3 должна остаться второй");
    }

    @Test
    void shouldRemoveTaskFromEnd() {
        historyManager.add(new Task(1, "Task1", "Desc1"));
        historyManager.add(new Task(2, "Task2", "Desc2"));
        historyManager.add(new Task(3, "Task3", "Desc3"));

        historyManager.remove(3);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "После удаления должно остаться две задачи");
        assertEquals(1, history.get(0).getId(), "Task1 должна остаться первой");
        assertEquals(2, history.get(1).getId(), "Task2 должна стать последней");
    }

    @Test
    void shouldReturnEmptyHistoryAfterRemovingOnlyTask() {
        historyManager.add(new Task(1, "Task1", "Desc1"));

        historyManager.remove(1);

        assertTrue(historyManager.getHistory().isEmpty(),
                "После удаления единственной задачи история должна быть пустой");
    }

    @Test
    void shouldIgnoreNullTask() {
        historyManager.add(null);

        assertTrue(historyManager.getHistory().isEmpty(),
                "Добавление null не должно менять историю");
    }

    @Test
    void shouldIgnoreRemovingUnknownId() {
        historyManager.add(new Task(1, "Task1", "Desc1"));

        historyManager.remove(999);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "Удаление несуществующего id не должно менять историю");
        assertEquals(1, history.getFirst().getId(), "Исходная задача должна остаться в истории");
    }
}