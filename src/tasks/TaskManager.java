//Возможность хранить задачи всех типов. Для этого вам нужно выбрать подходящую коллекцию.
//        Методы для каждого из типов задач(Задача/Эпик/Подзадача):
//        a. Получение списка всех задач.   +
//        b. Удаление всех задач.           +
//        c. Получение по идентификатору.   +
//        d. Создание. Сам объект должен передаваться в качестве параметра. +
//        e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра. +
//        f. Удаление по идентификатору. +
//        Дополнительные методы:
//        a. Получение списка всех подзадач определённого эпика.
//        Управление статусами осуществляется по следующему правилу:
//        a. Менеджер сам не выбирает статус для задачи. Информация о нём приходит менеджеру вместе с информацией о самой задаче. По этим данным в одних случаях он будет сохранять статус, в других будет рассчитывать.
//        b. Для эпиков:
//        если у эпика нет подзадач или все они имеют статус NEW, то статус должен быть NEW.
//        если все подзадачи имеют статус DONE, то и эпик считается завершённым — со статусом DONE.
//        во всех остальных случаях статус должен быть IN_PROGRESS.

package tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TaskManager {
    private static int taskCounter = 0;

    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, Subtask> subtasks;
    private HashMap<Integer, Epic> epics;

    public TaskManager() {
        this.tasks = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.epics = new HashMap<>();
    }

    private int generateId() {
        return ++taskCounter;
    }

    //region Getters
    public List<Task> getTasks() {
        return Collections.unmodifiableList(new ArrayList<>(tasks.values()));
    }

    public List<Subtask> getSubtasks() {
        return Collections.unmodifiableList(new ArrayList<>(subtasks.values()));
    }

    public List<Epic> getEpics() {
        return Collections.unmodifiableList(new ArrayList<>(epics.values()));
    }
    //endregion

    //region RemoveData
    public void removeTasks() {
        this.tasks.clear();
    }

    public void removeEpics() {
        removeSubtasks();
        epics.clear();
    }

    public void removeSubtasks() {
        this.subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
        }
    }
    //endregion

    //region GetByID
    public Task getTaskByID(int uid) {
        if (this.tasks.containsKey(uid)) {
            return this.tasks.get(uid);
        }
        return null;
    }

    public Epic getEpicByID(int uid) {
        if (this.epics.containsKey(uid)) {
            return this.epics.get(uid);
        }
        return null;
    }

    public Subtask getSubtaskByID(int uid) {
        if (this.subtasks.containsKey(uid)) {
            return this.subtasks.get(uid);
        }
        return null;
    }
    //endregion

    //region Create from data
    public void createTask(String name, String description) {
        int id = generateId();
        tasks.put(id, new Task(id, name, description));
    }
    //endregion

    //region Create from object
    public Task createTask(Task t) {
        if (t == null) return null;

        Task newTask = new Task(t);      // копируем поля
        int id = generateId();           // генерируем новый id
        newTask.setId(id);               // присваиваем
        tasks.put(id, newTask);
        return newTask;
    }

    public Epic createEpic(Epic e) {
        if (e == null) return null;

        Epic newEpic = new Epic(e);
        int id = generateId();
        newEpic.setId(id);
        epics.put(id, newEpic);
        return newEpic;
    }

    public Subtask createSubtask(Subtask t) {
        if (t == null || t.getEpic() == null) return null;

        int epicId = t.getEpic().getId();
        Epic storedEpic = epics.get(epicId);
        if (storedEpic == null) return null;

        Subtask newSubtask = new Subtask(t);
        int id = generateId();
        newSubtask.setId(id);

        newSubtask.setEpic(storedEpic);
        subtasks.put(id, newSubtask);
        storedEpic.addSubTask(newSubtask);
        return newSubtask;
    }

    //endregion

    //region Update tasks
    public void updateTask(Task t) {
        this.tasks.put(t.getId(), t);
    }

    public void updateEpic(Epic e) {
        if (e == null) return;

        Epic oldEpic = epics.get(e.getId());
        if (oldEpic == null) return;

        // 1) создаём "новую версию" эпика
        Epic newEpic = new Epic(e);

        // 2) переносим реальные подзадачи из старого эпика (источник правды)
        newEpic.clearSubtasks();
        for (Subtask st : new ArrayList<>(oldEpic.getSubtasks())) {
            st.setEpic(newEpic);
            newEpic.addSubTask(st);
        }

        // 3) заменяем эпик в хранилище
        epics.put(newEpic.getId(), newEpic);
    }

    public void updateSubtasks(Subtask st) {
        this.subtasks.put(st.getId(), st);
    }
    //endregion

    //region Remove by Id
    public void removeTaskById(int id) {
        if (this.tasks.containsKey(id)) {
            this.tasks.remove(id);
        }
    }

    public void removeEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic == null) return;

        for (Subtask st : new ArrayList<>(epic.getSubtasks())) {
            subtasks.remove(st.getId());
            st.setEpic(null);
        }
        epic.clearSubtasks(); // чистим список внутри эпика
    }

    public void removeSubtaskByID(int id) {
        Subtask st = subtasks.remove(id);
        if (st == null) return;

        Epic epic = st.getEpic();
        if (epic != null) {
            epic.removeSubtaskById(id);
            st.setEpic(null); // необязательно, но аккуратно
        }
    }
    //endregion

    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(epic.getSubtasks()));
    }

}
