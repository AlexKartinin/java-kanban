package tracker.controllers;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Менеджер задач, хранящий все данные в оперативной памяти.
 */
public class InMemoryTaskManager implements TaskManager {
    private static int taskCounter = 0;

    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();

    private final HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    private int generateId() {
        return ++taskCounter;
    }

    // region Getters
    @Override
    public List<Task> getTasks() {
        return List.copyOf(tasks.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return List.copyOf(subtasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return List.copyOf(epics.values());
    }
    // endregion

    // region RemoveData
    @Override
    public void removeTasks() {
        for (Integer id : new ArrayList<>(tasks.keySet())) {
            historyManager.remove(id);
        }
        tasks.clear();
    }

    @Override
    public void removeEpics() {
        removeSubtasks();

        for (Integer id : new ArrayList<>(epics.keySet())) {
            historyManager.remove(id);
        }
        epics.clear();
    }

    @Override
    public void removeSubtasks() {
        for (Integer id : new ArrayList<>(subtasks.keySet())) {
            historyManager.remove(id);
        }

        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
        }
    }
    // endregion

    // region GetByID (просмотры)
    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return subtask;
    }
    // endregion

    // region Create from object
    @Override
    public Task createTask(Task task) {
        if (task == null) {
            return null;
        }

        Task newTask = new Task(task);
        int id = generateId();
        newTask.setId(id);
        tasks.put(id, newTask);
        return newTask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        if (epic == null) {
            return null;
        }

        Epic newEpic = new Epic(epic);
        int id = generateId();
        newEpic.setId(id);
        epics.put(id, newEpic);
        return newEpic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (subtask == null || subtask.getEpic() == null) {
            return null;
        }

        int epicId = subtask.getEpic().getId();
        Epic storedEpic = epics.get(epicId);
        if (storedEpic == null) {
            return null;
        }

        Subtask newSubtask = new Subtask(subtask);
        int id = generateId();
        newSubtask.setId(id);

        newSubtask.setEpic(storedEpic);
        subtasks.put(id, newSubtask);
        storedEpic.addSubTask(newSubtask);
        return newSubtask;
    }
    // endregion

    // region Update
    @Override
    public void updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) {
            return;
        }
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null) {
            return;
        }

        Epic oldEpic = epics.get(epic.getId());
        if (oldEpic == null) {
            return;
        }

        Epic newEpic = new Epic(epic);

        newEpic.clearSubtasks();
        for (Subtask st : new ArrayList<>(oldEpic.getSubtasks())) {
            st.setEpic(newEpic);
            newEpic.addSubTask(st);
        }

        epics.put(newEpic.getId(), newEpic);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())) {
            return;
        }
        subtasks.put(subtask.getId(), subtask);
    }
    // endregion

    // region Remove by ID
    @Override
    public void removeTaskById(int id) {
        Task removed = tasks.remove(id);
        if (removed != null) {
            historyManager.remove(id);
        }
    }

    @Override
    public void removeEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic == null) {
            return;
        }

        historyManager.remove(id);

        for (Subtask st : new ArrayList<>(epic.getSubtasks())) {
            subtasks.remove(st.getId());
            historyManager.remove(st.getId());
            st.setEpic(null);
        }

        epic.clearSubtasks();
    }

    @Override
    public void removeSubtaskById(int id) {
        Subtask st = subtasks.remove(id);
        if (st == null) {
            return;
        }

        historyManager.remove(id);

        Epic epic = st.getEpic();
        if (epic != null) {
            epic.removeSubtaskById(id);
        }
    }
    // endregion

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return Collections.emptyList();
        }
        return List.copyOf(epic.getSubtasks());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}