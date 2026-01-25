package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Task;
import tracker.model.TaskStatus;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class TaskImmutabilityOnAddTest {

    private TaskManager tm;

    @BeforeEach
    void setUp() {
        tm = Managers.getDefault();
    }

    @Test
    void taskShouldNotChangeWhenAddedToManager_snapshotByAllFields() {
        Task original = new Task(777, "Original", "OriginalDesc");
        original.setStatus(TaskStatus.IN_PROGRESS);

        Task created = tm.createTask(original);
        Task stored = tm.getTask(created.getId());

        assertNotNull(stored);
        assertNotSame(original, stored, "В менеджере должна храниться копия, а не тот же объект");

        // меняем original после добавления
        original.setStatus(TaskStatus.DONE);
        setPrivateField(original, "name", "HACKED");
        setPrivateField(original, "description", "HACKED_DESC");

        // stored не должен измениться
        assertEquals(TaskStatus.IN_PROGRESS, stored.getStatus(), "Статус в менеджере не должен измениться");
        assertEquals("Original", getPrivateField(stored, "name"), "name в менеджере не должен измениться");
        assertEquals("OriginalDesc", getPrivateField(stored, "description"), "description в менеджере не должен измениться");
    }

    private static Object getPrivateField(Object obj, String fieldName) {
        Field f = findField(obj.getClass(), fieldName);
        try {
            f.setAccessible(true);
            return f.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setPrivateField(Object obj, String fieldName, Object value) {
        Field f = findField(obj.getClass(), fieldName);
        try {
            f.setAccessible(true);
            f.set(obj, value);
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
