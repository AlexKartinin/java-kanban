package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Task;

import static org.junit.jupiter.api.Assertions.*;

class IdConflictTest {

    private TaskManager tm;

    @BeforeEach
    void setUp() {
        tm = Managers.getDefault();
    }

    @Test
    void givenIdShouldNotConflictWithGeneratedId() {
        int givenId = 1000;

        Task created = tm.createTask(new Task(givenId, "Name", "Desc"));

        assertNotNull(created);
        assertNotEquals(givenId, created.getId(),
                "Менеджер должен генерировать новый id, а не использовать заданный в объекте");

        assertNotNull(tm.getTask(created.getId()), "Задача должна находиться по сгенерированному id");
        assertNull(tm.getTask(givenId), "Не должно появляться задачи по “входному” id");
    }
}
