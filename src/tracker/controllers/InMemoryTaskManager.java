package tracker.controllers;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.util.*;

/**
 * Менеджер задач, хранящий все данные в оперативной памяти.
 */
public class InMemoryTaskManager implements TaskManager {
    private int taskCounter = 0;

    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();

    // TreeSet хранит задачи и подзадачи с startTime, отсортированные по приоритету — O(n) получение
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime).thenComparingInt(Task::getId)
    );

    protected final HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    protected int generateId() {
        return ++taskCounter;
    }

    protected void syncCounter(int maxId) {
        if (maxId > taskCounter) {
            taskCounter = maxId;
        }
    }

    // region Restore helpers (десериализация)

    protected void restoreTask(Task task) {
        tasks.put(task.getId(), task);
        addToPrioritized(task);
    }

    protected void restoreEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    protected void restoreSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        addToPrioritized(subtask);
    }

    protected void restoreHistory(Task task) {
        historyManager.add(task);
    }

    protected Epic findEpicById(int id) {
        return epics.get(id);
    }

    // endregion

    // region Prioritized tasks

    private void addToPrioritized(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    private void removeFromPrioritized(Task task) {
        prioritizedTasks.remove(task);
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    // endregion

    // region Overlap check

    /**
     * Проверяет, пересекаются ли два временных отрезка задач.
     * Метод наложения отрезков: [a1, a2) и [b1, b2) пересекаются, если a1 < b2 && b1 < a2.
     */
    protected boolean isOverlapping(Task a, Task b) {
        if (a.getStartTime() == null || a.getEndTime() == null) return false;
        if (b.getStartTime() == null || b.getEndTime() == null) return false;
        return a.getStartTime().isBefore(b.getEndTime())
                && b.getStartTime().isBefore(a.getEndTime());
    }

    /**
     * Проверяет, пересекается ли задача с любой существующей в prioritizedTasks.
     * Исключает саму задачу (по id) — для случая обновления.
     */
    protected boolean hasOverlapWithExisting(Task candidate) {
        if (candidate.getStartTime() == null || candidate.getEndTime() == null) return false;
        return getPrioritizedTasks().stream()
                .filter(t -> t.getId() != candidate.getId())
                .anyMatch(t -> isOverlapping(candidate, t));
    }

    // endregion

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
        tasks.values().forEach(task -> {
            historyManager.remove(task.getId());
            removeFromPrioritized(task);
        });
        tasks.clear();
    }

    @Override
    public void removeEpics() {
        removeSubtasks();
        epics.keySet().forEach(historyManager::remove);
        epics.clear();
    }

    @Override
    public void removeSubtasks() {
        subtasks.values().forEach(st -> {
            historyManager.remove(st.getId());
            removeFromPrioritized(st);
        });
        subtasks.clear();
        epics.values().forEach(Epic::clearSubtasks);
    }

    // endregion

    // region GetByID

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

    // region Create

    @Override
    public Task createTask(Task task) {
        if (task == null) return null;
        if (task.getStartTime() != null && hasOverlapWithExisting(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с существующей: " + task);
        }

        Task newTask = new Task(task);
        int id = generateId();
        newTask.setId(id);
        tasks.put(id, newTask);
        addToPrioritized(newTask);
        return newTask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        if (epic == null) return null;

        Epic newEpic = new Epic(epic);
        int id = generateId();
        newEpic.setId(id);
        epics.put(id, newEpic);
        return newEpic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (subtask == null || subtask.getEpic() == null) return null;

        Epic storedEpic = epics.get(subtask.getEpic().getId());
        if (storedEpic == null) return null;

        if (subtask.getStartTime() != null && hasOverlapWithExisting(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается по времени с существующей: " + subtask);
        }

        Subtask newSubtask = new Subtask(subtask);
        int id = generateId();
        newSubtask.setId(id);
        newSubtask.setEpic(storedEpic);
        subtasks.put(id, newSubtask);
        storedEpic.addSubTask(newSubtask);
        addToPrioritized(newSubtask);
        return newSubtask;
    }

    // endregion

    // region Update

    @Override
    public void updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) return;
        if (task.getStartTime() != null && hasOverlapWithExisting(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с существующей: " + task);
        }

        removeFromPrioritized(tasks.get(task.getId()));
        tasks.put(task.getId(), task);
        addToPrioritized(task);
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null) return;

        Epic oldEpic = epics.get(epic.getId());
        if (oldEpic == null) return;

        Epic newEpic = new Epic(epic);
        newEpic.clearSubtasks();
        oldEpic.getSubtasks().forEach(st -> {
            st.setEpic(newEpic);
            newEpic.addSubTask(st);
        });

        epics.put(newEpic.getId(), newEpic);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())) return;
        if (subtask.getStartTime() != null && hasOverlapWithExisting(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается по времени с существующей: " + subtask);
        }

        removeFromPrioritized(subtasks.get(subtask.getId()));
        subtasks.put(subtask.getId(), subtask);
        addToPrioritized(subtask);
    }

    // endregion

    // region Remove by ID

    @Override
    public void removeTaskById(int id) {
        Task removed = tasks.remove(id);
        if (removed != null) {
            historyManager.remove(id);
            removeFromPrioritized(removed);
        }
    }

    @Override
    public void removeEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic == null) return;

        historyManager.remove(id);

        new ArrayList<>(epic.getSubtasks()).forEach(st -> {
            subtasks.remove(st.getId());
            historyManager.remove(st.getId());
            removeFromPrioritized(st);
            st.setEpic(null);
        });

        epic.clearSubtasks();
    }

    @Override
    public void removeSubtaskById(int id) {
        Subtask st = subtasks.remove(id);
        if (st == null) return;

        historyManager.remove(id);
        removeFromPrioritized(st);

        Epic epic = st.getEpic();
        if (epic != null) {
            epic.removeSubtaskById(id);
        }
    }

    // endregion

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        return Optional.ofNullable(epics.get(epicId))
                .map(e -> List.copyOf(e.getSubtasks()))
                .orElse(Collections.emptyList());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}
