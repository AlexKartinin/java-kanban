package tasks;

public class Subtask extends Task{
    private Epic epic;

    public Subtask(String name, String description, Epic epic) {
        super(name, description);
        this.epic = epic;
    }

    public Subtask(String name, Epic epic) {
        super(name);
        this.epic = epic;
    }

    public Subtask(String name, String description) {
        super(name, description);
    }

    public Subtask(String name) {
        super(name);
    }

    public Epic getEpic() {
        return epic;
    }

    public void setEpic(Epic epic) {
        this.epic = epic;
    }

    @Override
    public void setStatus(TaskStatus status) {
        super.setStatus(status);

        if (epic != null) {
            epic.checkStatus(status);
        }
        // проверь статус задач в родителе
    }
}
