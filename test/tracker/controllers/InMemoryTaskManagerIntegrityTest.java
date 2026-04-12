package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.model.TaskStatus;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerIntegrityTest {

    private TaskManager tm;

    @BeforeEach
    void setUp() {
        tm = Managers.getDefault();
    }

    @Test
    void removedSubtaskShouldDisappearFromEpic() {
        Epic epic = tm.createEpic(new Epic(0, "Epic", "Desc"));
        Subtask subtask1 = tm.createSubtask(new Subtask(0, "Sub1", "Desc1", epic));
        Subtask subtask2 = tm.createSubtask(new Subtask(0, "Sub2", "Desc2", epic));

        assertEquals(2, tm.getEpicSubtasks(epic.getId()).size(),
                "До удаления у эпика должно быть две подзадачи");

        tm.removeSubtaskById(subtask1.getId());

        assertEquals(1, tm.getEpicSubtasks(epic.getId()).size(),
                "После удаления подзадачи в эпике должна остаться только одна");
        assertEquals(subtask2.getId(), tm.getEpicSubtasks(epic.getId()).getFirst().getId(),
                "В эпике должна остаться актуальная подзадача");
    }

    @Test
    void removeSubtasksShouldClearEpicSubtasks() {
        Epic epic = tm.createEpic(new Epic(0, "Epic", "Desc"));
        tm.createSubtask(new Subtask(0, "Sub1", "Desc1", epic));
        tm.createSubtask(new Subtask(0, "Sub2", "Desc2", epic));

        tm.removeSubtasks();

        assertTrue(tm.getSubtasks().isEmpty(), "Все подзадачи должны быть удалены из менеджера");
        assertTrue(tm.getEpicSubtasks(epic.getId()).isEmpty(),
                "После removeSubtasks внутри эпика не должно оставаться старых подзадач");
    }

    @Test
    void removedEpicShouldBreakLinkInsideExistingSubtaskObject() {
        Epic epic = tm.createEpic(new Epic(0, "Epic", "Desc"));
        Subtask subtask = tm.createSubtask(new Subtask(0, "Sub", "Desc", epic));

        assertNotNull(subtask.getEpic(), "До удаления эпика подзадача должна ссылаться на него");

        tm.removeEpicById(epic.getId());

        assertNull(subtask.getEpic(),
                "После удаления эпика подзадача не должна хранить старую ссылку на удалённый эпик");
        assertNull(tm.getSubtask(subtask.getId()),
                "Подзадача удалённого эпика должна исчезнуть из менеджера");
    }

    @Test
    void removingEpicShouldAlsoRemoveEpicAndSubtasksFromHistory() {
        Epic epic = tm.createEpic(new Epic(0, "Epic", "Desc"));
        Subtask subtask = tm.createSubtask(new Subtask(0, "Sub", "Desc", epic));
        Task task = tm.createTask(new Task(0, "Task", "Desc"));

        tm.getEpic(epic.getId());
        tm.getSubtask(subtask.getId());
        tm.getTask(task.getId());

        tm.removeEpicById(epic.getId());

        assertEquals(1, tm.getHistory().size(),
                "После удаления эпика из истории должны исчезнуть и он сам, и его подзадачи");
        assertEquals(task.getId(), tm.getHistory().getFirst().getId(),
                "В истории должна остаться только независимая задача");
    }

    @Test
    void mutatingReturnedTaskShouldAffectManagerState_nowItIsUnsafeCase() {
        Task created = tm.createTask(new Task(0, "Original", "Desc"));

        Task fromManager = tm.getTask(created.getId());
        fromManager.setStatus(TaskStatus.DONE);

        Task loadedAgain = tm.getTask(created.getId());

        assertEquals(TaskStatus.DONE, loadedAgain.getStatus(),
                "Сейчас менеджер возвращает внутренний объект, поэтому его изменение снаружи меняет состояние менеджера");
    }
}