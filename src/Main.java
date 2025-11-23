import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        Task t1 = new Task("First app check");
        t1.setDescription("you need to test app!");
        System.out.println(t1.getDescription());
        System.out.println(t1.getName());
        System.out.println(t1.getStatus());
        System.out.println(t1.getTaskUID());

        Task t2 = new Task("First app check");
        t2.setDescription("you need to test app!");
        System.out.println(t2.getDescription());
        System.out.println(t2.getName());
        System.out.println(t2.getStatus());
        System.out.println(t2.getTaskUID());

        Epic e1 = new Epic("This is first epic task!");

    }
}
