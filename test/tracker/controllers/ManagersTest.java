package tracker.controllers;

import org.junit.jupiter.api.Test;
import tracker.model.Task;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void managersShouldReturnInitializedTaskManager() {
        TaskManager tm = Managers.getDefault();
        assertNotNull(tm, "Managers.getDefault() должен возвращать не null");

        Task created = tm.createTask(new Task(999, "Name", "Desc"));
        assertNotNull(created, "Менеджер должен быть готов к работе: createTask не должен возвращать null");
        assertTrue(created.getId() > 0, "Менеджер должен уметь выдавать id");
    }

    @Test
    void managersShouldReturnInitializedHistoryManager() {
        HistoryManager hm = Managers.getDefaultHistory();
        assertNotNull(hm, "Managers.getDefaultHistory() должен возвращать не null");
        assertNotNull(hm.getHistory(), "HistoryManager должен быть готов: getHistory не null");
    }
}
