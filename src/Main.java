import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskManager;
import tasks.TaskStatus;

public class Main {

    public static void main(String[] args) {
        TaskManager tm = new TaskManager();

        // 1) Создаём две задачи
        Task task1 = tm.createTask(new Task(0, "Task #1", "Simple task 1"));
        Task task2 = tm.createTask(new Task(0, "Task #2", "Simple task 2"));

        // 2) Эпик с двумя подзадачами
        Epic epic1 = tm.createEpic(new Epic(0, "Epic #1", "Epic with 2 subtasks"));
        Subtask epic1st1 = tm.createSubtask(new Subtask(0, "Epic1-Subtask #1", "First subtask", epic1));
        Subtask epic1st2 = tm.createSubtask(new Subtask(0, "Epic1-Subtask #2", "Second subtask", epic1));

        // 3) Эпик с одной подзадачей
        Epic epic2 = tm.createEpic(new Epic(0, "Epic #2", "Epic with 1 subtask"));
        Subtask epic2st1 = tm.createSubtask(new Subtask(0, "Epic2-Subtask #1", "Only subtask", epic2));

        // 4) Печать списков (как требует ТЗ)
        System.out.println("=== INITIAL LISTS ===");
        System.out.println("Tasks: " + tm.getTasks());
        System.out.println("Epics: " + tm.getEpics());
        System.out.println("Subtasks: " + tm.getSubtasks());
        printStatuses(task1, task2, epic1, epic2, epic1st1, epic1st2, epic2st1);

        // 5) Меняем статусы задач и подзадач, проверяем эпики
        System.out.println("\n=== CHANGE STATUSES ===");

        // задачи (их статус должен просто сохраниться)
        task1.setStatus(TaskStatus.IN_PROGRESS);
        task2.setStatus(TaskStatus.DONE);
        tm.updateTask(task1);
        tm.updateTask(task2);

        // Проверка смешанного кейса для эпика: NEW + DONE => IN_PROGRESS (после фикса #2)
        epic1st1.setStatus(TaskStatus.DONE);  // epic1 должен стать IN_PROGRESS (т.к. вторая ещё NEW)
        System.out.println("After epic1st1 DONE (epic1 expected IN_PROGRESS):");
        System.out.println("epic1 status = " + tm.getEpicByID(epic1.getId()).getStatus());

        // Доведём epic1 до DONE
        epic1st2.setStatus(TaskStatus.DONE);  // epic1 должен стать DONE
        System.out.println("After epic1st2 DONE (epic1 expected DONE):");
        System.out.println("epic1 status = " + tm.getEpicByID(epic1.getId()).getStatus());

        // epic2: сделаем IN_PROGRESS, потом DONE
        epic2st1.setStatus(TaskStatus.IN_PROGRESS); // epic2 должен стать IN_PROGRESS
        System.out.println("After epic2st1 IN_PROGRESS (epic2 expected IN_PROGRESS):");
        System.out.println("epic2 status = " + tm.getEpicByID(epic2.getId()).getStatus());

        epic2st1.setStatus(TaskStatus.DONE); // epic2 должен стать DONE
        System.out.println("After epic2st1 DONE (epic2 expected DONE):");
        System.out.println("epic2 status = " + tm.getEpicByID(epic2.getId()).getStatus());

        // Печать списков снова
        System.out.println("\n=== LISTS AFTER STATUS CHANGES ===");
        System.out.println("Tasks: " + tm.getTasks());
        System.out.println("Epics: " + tm.getEpics());
        System.out.println("Subtasks: " + tm.getSubtasks());
        printStatuses(task1, task2, epic1, epic2, epic1st1, epic1st2, epic2st1);

        // 6) Удаляем одну задачу и один эпик
        System.out.println("\n=== REMOVE ONE TASK AND ONE EPIC ===");
        tm.removeTaskById(task1.getId());
        tm.removeEpicById(epic1.getId()); // вместе с его подзадачами

        System.out.println("Tasks: " + tm.getTasks());
        System.out.println("Epics: " + tm.getEpics());
        System.out.println("Subtasks: " + tm.getSubtasks());

        // (не обязательно, но удобно) Проверим, что подзадачи epic1 исчезли:
        System.out.println("Epic1 subtasks by manager (expected empty): " + tm.getEpicSubtasks(epic1.getId()));
    }

    private static void printStatuses(Task task1, Task task2, Epic epic1, Epic epic2,
                                      Subtask epic1st1, Subtask epic1st2, Subtask epic2st1) {
        System.out.println("--- STATUSES ---");
        System.out.println("task1 id=" + task1.getId() + " status=" + task1.getStatus());
        System.out.println("task2 id=" + task2.getId() + " status=" + task2.getStatus());

        System.out.println("epic1 id=" + epic1.getId() + " status=" + epic1.getStatus());
        System.out.println("  epic1st1 id=" + epic1st1.getId() + " status=" + epic1st1.getStatus());
        System.out.println("  epic1st2 id=" + epic1st2.getId() + " status=" + epic1st2.getStatus());

        System.out.println("epic2 id=" + epic2.getId() + " status=" + epic2.getStatus());
        System.out.println("  epic2st1 id=" + epic2st1.getId() + " status=" + epic2st1.getStatus());
    }
}
