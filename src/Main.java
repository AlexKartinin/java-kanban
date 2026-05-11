import tracker.controllers.Managers;
import tracker.controllers.TaskManager;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

public class Main {

    public static void main(String[] args) {
        TaskManager tm = Managers.getDefault();

        // 1. Две обычные задачи
        Task task1 = tm.createTask(new Task(0, "Task #1", "Simple task 1"));
        Task task2 = tm.createTask(new Task(0, "Task #2", "Simple task 2"));

        // 2. Эпик с тремя подзадачами
        Epic epicWithSubtasks = tm.createEpic(new Epic(0, "Epic #1", "Epic with 3 subtasks"));
        Subtask subtask1 = tm.createSubtask(new Subtask(0, "Subtask #1", "First subtask", epicWithSubtasks));
        Subtask subtask2 = tm.createSubtask(new Subtask(0, "Subtask #2", "Second subtask", epicWithSubtasks));

        // 3. Эпик без подзадач
        Epic emptyEpic = tm.createEpic(new Epic(0, "Epic #2", "Epic without subtasks"));

        System.out.println("=== CREATED OBJECTS ===");
        System.out.println("Tasks: " + tm.getTasks());
        System.out.println("Epics: " + tm.getEpics());
        System.out.println("Subtasks: " + tm.getSubtasks());

        System.out.println("\n=== HISTORY AFTER EACH REQUEST ===");

        tm.getTask(task1.getId());
        printHistory(tm, "After getTask(task1)");

        tm.getEpic(epicWithSubtasks.getId());
        printHistory(tm, "After getEpic(epicWithSubtasks)");

        tm.getSubtask(subtask1.getId());
        printHistory(tm, "After getSubtask(subtask1)");

        tm.getTask(task2.getId());
        printHistory(tm, "After getTask(task2)");

        tm.getSubtask(subtask2.getId());
        printHistory(tm, "After getSubtask(subtask2)");

        tm.getEpic(emptyEpic.getId());
        printHistory(tm, "After getEpic(emptyEpic)");

        // Повторные просмотры в другом порядке
        tm.getTask(task1.getId());
        printHistory(tm, "After repeated getTask(task1)");

        tm.getSubtask(subtask1.getId());
        printHistory(tm, "After repeated getSubtask(subtask1)");

        tm.getEpic(epicWithSubtasks.getId());
        printHistory(tm, "After repeated getEpic(epicWithSubtasks)");

        System.out.println("\n=== REMOVE TASK FROM HISTORY ===");
        tm.removeTaskById(task2.getId());
        printHistory(tm, "After removeTaskById(task2)");

        System.out.println("\n=== REMOVE EPIC WITH 3 SUBTASKS FROM HISTORY ===");
        tm.removeEpicById(epicWithSubtasks.getId());
        printHistory(tm, "After removeEpicById(epicWithSubtasks)");

        System.out.println("\n=== FINAL STATE ===");
        System.out.println("Tasks: " + tm.getTasks());
        System.out.println("Epics: " + tm.getEpics());
        System.out.println("Subtasks: " + tm.getSubtasks());
    }

    private static void printHistory(TaskManager tm, String label) {
        System.out.println(label);
        System.out.println("History: " + tm.getHistory());
        System.out.println();
    }
}