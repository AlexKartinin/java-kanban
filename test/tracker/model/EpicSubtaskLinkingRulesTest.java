package tracker.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicSubtaskLinkingRulesTest {

    @Test
    void epicShouldNotAcceptSubtaskWithSameIdAsEpic() {
        Epic epic = new Epic(1, "Epic", "d");

        Subtask subtask = new Subtask(2, "S", "d", epic);

        subtask.setId(epic.getId());

        epic.addSubTask(subtask);

        assertTrue(epic.getSubtasks().isEmpty(),
                "Epic не должен принимать подзадачу с id, совпадающим с id эпика");
    }

    @Test
    void subtaskShouldNotAllowEpicWithSameIdAsSubtask() {
        Epic okEpic = new Epic(10, "OK", "d");
        Epic badEpic = new Epic(7, "BAD", "d");

        Subtask subtask = new Subtask(2, "S", "d", okEpic);
        assertEquals(okEpic, subtask.getEpic(), "Для начала должен быть назначен валидный эпик");

        subtask.setId(badEpic.getId());

        // попытка назначить самого себя по id
        subtask.setEpic(badEpic);

        assertEquals(okEpic, subtask.getEpic(),
                "Subtask не должна позволять назначить epic с таким же id, как у неё самой");
    }
}
