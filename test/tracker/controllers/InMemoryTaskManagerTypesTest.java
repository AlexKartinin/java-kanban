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
        Task createdTask = tm.createTask(new Task(123, "T", "d"));
        Epic createdEpic = tm.createEpic(new Epic(321, "E", "d"));
        Subtask createdSubtask = tm.createSubtask(new Subtask(111, "S", "d", createdEpic));

        assertNotNull(createdTask, "Task должна успешно создаваться");
        assertNotNull(createdEpic, "Epic должна успешно создаваться");
        assertNotNull(createdSubtask, "Subtask должна успешно создаваться");

        Task foundTask = tm.getTask(createdTask.getId());
        Epic foundEpic = tm.getEpic(createdEpic.getId());
        Subtask foundSubtask = tm.getSubtask(createdSubtask.getId());

        assertNotNull(foundTask, "Менеджер должен находить Task по id");
        assertNotNull(foundEpic, "Менеджер должен находить Epic по id");
        assertNotNull(foundSubtask, "Менеджер должен находить Subtask по id");

        assertInstanceOf(Task.class, foundTask, "По id Task должен возвращаться объект типа Task");
        assertInstanceOf(Epic.class, foundEpic, "По id Epic должен возвращаться объект типа Epic");
        assertInstanceOf(Subtask.class, foundSubtask, "По id Subtask должен возвращаться объект типа Subtask");

        assertEquals(createdTask.getId(), foundTask.getId(), "У найденной Task должен совпадать id");
        assertEquals(createdEpic.getId(), foundEpic.getId(), "У найденной Epic должен совпадать id");
        assertEquals(createdSubtask.getId(), foundSubtask.getId(), "У найденной Subtask должен совпадать id");
    }
}