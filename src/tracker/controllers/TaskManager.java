package tracker.controllers;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.util.List;

/**
 * Интерфейс менеджера задач.
 * Содержит только публичные методы, доступные пользователю трекера.
 */
public interface TaskManager {

    //region Getters
    List<Task> getTasks();

    List<Subtask> getSubtasks();

    List<Epic> getEpics();
    //endregion

    //region RemoveData
    void removeTasks();

    void removeEpics();

    void removeSubtasks();
    //endregion

    //region GetByID (просмотры)
    Task getTask(int id);

    Epic getEpic(int id);

    Subtask getSubtask(int id);
    //endregion

    //region Create
    Task createTask(Task task);

    Epic createEpic(Epic epic);

    Subtask createSubtask(Subtask subtask);
    //endregion

    //region Update
    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);
    //endregion

    //region RemoveById
    void removeTaskById(int id);

    void removeEpicById(int id);

    void removeSubtaskById(int id);
    //endregion

    List<Subtask> getEpicSubtasks(int epicId);

    /**
     * История просмотров (последние 10 задач), формируется по вызовам getTask/getEpic/getSubtask.
     */
    List<Task> getHistory();
}
