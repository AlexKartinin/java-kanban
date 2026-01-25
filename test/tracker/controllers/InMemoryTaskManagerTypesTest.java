package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTypesTest {

    private TaskManager tm;

    @BeforeEach
    void setUp() {
        tm = Managers.getDefault();
    }

    @Test
    void shouldAddDifferentTaskTypesAndFindById() {
        Task t = tm.createTask(new Task(123, "T", "d"));
        Epic e = tm.createEpic(new Epic(321, "E", "d"));
        Subtask s = tm.createSubtask(new Subtask(111, "S", "d", e));

        assertNotNull(t);
        assertNotNull(e);
        assertNotNull(s);

        assertEquals(t, tm.getTask(t.getId()), "Менеджер должен находить Task по id");
        assertEquals(e, tm.getEpic(e.getId()), "Менеджер должен находить Epic по id");
        assertEquals(s, tm.getSubtask(s.getId()), "Менеджер должен находить Subtask по id");
    }
}
