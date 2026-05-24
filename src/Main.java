import tracker.controllers.FileBackedTaskManager;
import tracker.controllers.Managers;
import tracker.controllers.TaskManager;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {

    public static void main(String[] args) {
        sprint_6_task(args);
        try {
            sprint_7_additional_task(args);
        } catch (IOException e) {
            System.err.println("Ошибка при работе с файлом: " + e.getMessage());
        }
    }

    public static void sprint_6_task(String[] args) {
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

    public static void sprint_7_additional_task(String[] args) throws IOException {
        File file = File.createTempFile("task-tracker", ".csv");
        file.deleteOnExit();

        System.out.println("=== Создаём первый менеджер и наполняем данными ===");
        FileBackedTaskManager manager1 = new FileBackedTaskManager(file);

        Task task1 = manager1.createTask(new Task(0, "Задача 1", "Описание задачи 1"));
        Task task2 = manager1.createTask(new Task(0, "Задача 2", "Описание задачи 2"));

        Epic epic1 = manager1.createEpic(new Epic(0, "Эпик 1", "Большая фича"));
        Subtask sub1 = manager1.createSubtask(new Subtask(0, "Подзадача 1.1", "Первый шаг", epic1));
        Subtask sub2 = manager1.createSubtask(new Subtask(0, "Подзадача 1.2", "Второй шаг", epic1));

        Epic epic2 = manager1.createEpic(new Epic(0, "Эпик 2", "Без подзадач"));

        // Формируем историю просмотров
        manager1.getTask(task1.getId());
        manager1.getEpic(epic1.getId());
        manager1.getSubtask(sub1.getId());
        manager1.getTask(task2.getId());

        System.out.println("Tasks:    " + manager1.getTasks());
        System.out.println("Epics:    " + manager1.getEpics());
        System.out.println("Subtasks: " + manager1.getSubtasks());
        System.out.println("History:  " + manager1.getHistory());
        System.out.println("\nФайл сохранён: " + file.getAbsolutePath());
        System.out.println("Содержимое:\n" + Files.readString(file.toPath()));

        System.out.println("=== Загружаем второй менеджер из того же файла ===");
        FileBackedTaskManager manager2 = FileBackedTaskManager.loadFromFile(file);

        System.out.println("Tasks:    " + manager2.getTasks());
        System.out.println("Epics:    " + manager2.getEpics());
        System.out.println("Subtasks: " + manager2.getSubtasks());
        System.out.println("History:  " + manager2.getHistory());

        System.out.println("\n=== Сверяем содержимое ===");
        boolean tasksOk = manager1.getTasks().size() == manager2.getTasks().size();
        boolean epicsOk = manager1.getEpics().size() == manager2.getEpics().size();
        boolean subtasksOk = manager1.getSubtasks().size() == manager2.getSubtasks().size();
        boolean historyOk = manager1.getHistory().size() == manager2.getHistory().size();

        System.out.println("Tasks совпадают:    " + tasksOk);
        System.out.println("Epics совпадают:    " + epicsOk);
        System.out.println("Subtasks совпадают: " + subtasksOk);
        System.out.println("History совпадает:  " + historyOk);
    }

    private static void printHistory(TaskManager tm, String label) {
        System.out.println(label);
        System.out.println("History: " + tm.getHistory());
        System.out.println();
    }
}