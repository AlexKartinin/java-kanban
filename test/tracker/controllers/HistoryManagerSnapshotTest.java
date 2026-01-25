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
    void historyShouldKeepPreviousVersionOfTaskData() {
        Task created = tm.createTask(new Task(1, "Name1", "Desc1"));

        // первый просмотр -> попадает в историю
        Task firstView = tm.getTask(created.getId());
        assertNotNull(firstView);

        // делаем новую версию и обновляем в менеджере
        Task updated = new Task(created.getId(), "Name2", "Desc2");
        updated.setStatus(TaskStatus.DONE);
        tm.updateTask(updated);

        // второй просмотр
        tm.getTask(created.getId());

        List<Task> history = tm.getHistory();
        assertFalse(history.isEmpty(), "История не должна быть пустой");

        Task historyEntry = history.get(0); // самый ранний просмотр (если добавляем в конец)

        // В истории должна остаться старая версия
        assertEquals(TaskStatus.NEW, historyEntry.getStatus(),
                "В истории должна сохраниться предыдущая версия статуса (NEW), а не обновлённая (DONE)");
        assertEquals("Name1", getFieldValue(historyEntry, "name"),
                "В истории должно сохраниться старое имя");
        assertEquals("Desc1", getFieldValue(historyEntry, "description"),
                "В истории должно сохраниться старое описание");
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
