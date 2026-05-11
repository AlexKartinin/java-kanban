package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Task;
import tracker.model.TaskStatus;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerSnapshotTest {

    private TaskManager tm;

    @BeforeEach
    void setUp() {
        tm = Managers.getDefault();
    }

    @Test
    void historyShouldKeepOnlyLatestVersionOfTaskData() {
        Task created = tm.createTask(new Task(1, "Name1", "Desc1"));

        // Первый просмотр
        Task firstView = tm.getTask(created.getId());
        assertNotNull(firstView);

        // Обновляем задачу
        Task updated = new Task(created.getId(), "Name2", "Desc2");
        updated.setStatus(TaskStatus.DONE);
        tm.updateTask(updated);

        // Второй просмотр: старая версия должна исчезнуть из истории,
        // новая должна добавиться
        Task secondView = tm.getTask(created.getId());
        assertNotNull(secondView);

        List<Task> history = tm.getHistory();
        assertEquals(1, history.size(),
                "В истории должен остаться только один просмотр задачи");

        Task historyEntry = history.getFirst();

        assertEquals(created.getId(), historyEntry.getId(),
                "В истории должна остаться та же задача по id");
        assertEquals(TaskStatus.DONE, historyEntry.getStatus(),
                "В истории должна сохраниться последняя версия статуса");
        assertEquals("Name2", getFieldValue(historyEntry, "name"),
                "В истории должно сохраниться новое имя");
        assertEquals("Desc2", getFieldValue(historyEntry, "description"),
                "В истории должно сохраниться новое описание");
    }

    @Test
    void historyShouldNotContainDuplicates() {
        Task created = tm.createTask(new Task(1, "Task1", "Desc1"));

        tm.getTask(created.getId());
        tm.getTask(created.getId());
        tm.getTask(created.getId());

        List<Task> history = tm.getHistory();

        assertEquals(1, history.size(),
                "История не должна содержать дубликаты одной и той же задачи");
        assertEquals(created.getId(), history.getFirst().getId(),
                "В истории должна остаться только одна задача с тем же id");
    }

    @Test
    void historyShouldMoveTaskToEndWhenViewedAgain() {
        Task task1 = tm.createTask(new Task(1, "Task1", "Desc1"));
        Task task2 = tm.createTask(new Task(2, "Task2", "Desc2"));
        Task task3 = tm.createTask(new Task(3, "Task3", "Desc3"));

        tm.getTask(task1.getId());
        tm.getTask(task2.getId());
        tm.getTask(task3.getId());

        // Повторный просмотр task2 должен переместить её в конец
        tm.getTask(task2.getId());

        List<Task> history = tm.getHistory();

        assertEquals(3, history.size(),
                "В истории должно быть 3 уникальные задачи");
        assertEquals(task1.getId(), history.get(0).getId(),
                "Task1 должна остаться первой");
        assertEquals(task3.getId(), history.get(1).getId(),
                "Task3 должна стать второй");
        assertEquals(task2.getId(), history.get(2).getId(),
                "Повторно просмотренная Task2 должна перейти в конец");
    }

    @Test
    void removeShouldDeleteTaskFromHistory() {
        Task task1 = tm.createTask(new Task(1, "Task1", "Desc1"));
        Task task2 = tm.createTask(new Task(2, "Task2", "Desc2"));

        tm.getTask(task1.getId());
        tm.getTask(task2.getId());

        tm.removeTaskById(task1.getId());

        List<Task> history = tm.getHistory();

        assertEquals(1, history.size(),
                "После удаления задачи из менеджера она должна исчезнуть из истории");
        assertEquals(task2.getId(), history.getFirst().getId(),
                "В истории должна остаться только вторая задача");
    }

    @Test
    void historyShouldReturnEmptyListWhenNoTasksViewed() {
        List<Task> history = tm.getHistory();

        assertNotNull(history, "История не должна быть null");
        assertTrue(history.isEmpty(), "История должна быть пустой, если задач не просматривали");
    }

    private static Object getFieldValue(Object obj, String fieldName) {
        Field f = findField(obj.getClass(), fieldName);
        try {
            f.setAccessible(true);
            return f.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Field findField(Class<?> clazz, String fieldName) {
        Class<?> c = clazz;
        while (c != null) {
            try {
                return c.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                c = c.getSuperclass();
            }
        }
        throw new AssertionError("Field '" + fieldName + "' not found in class hierarchy of " + clazz);
    }
}