package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Subtask;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskEpicSelfLinkingViaManagerTest {

    private TaskManager tm;

    @BeforeEach
    void setUp() {
        tm = Managers.getDefault();
    }

    @Test
    void subtaskCannotBeItsOwnEpic() {
        Epic epic = tm.createEpic(new Epic(0, "E", "d"));
        assertNotNull(epic);

        Subtask subtask = tm.createSubtask(new Subtask(0, "S", "d", epic));
        assertNotNull(subtask, "Подзадача должна создаваться корректно");
        assertNotNull(subtask.getEpic(), "У подзадачи должен быть эпик");

        // subtask не должна иметь эпик с тем же id, что у неё
        assertNotEquals(subtask.getId(), subtask.getEpic().getId(),
                "Subtask нельзя сделать своим же эпиком (id подзадачи не должен совпадать с id эпика)");

        // подзадачу невозможно назначить эпиком самой себе по типам
        assertThrows(ClassCastException.class, () -> {
            Object o = subtask;           // runtime: это Subtask
            subtask.setEpic((Epic) o);    // runtime cast -> ClassCastException
        }, "Нельзя назначить Subtask своим же Epic (типовая безопасность)");
    }
}
