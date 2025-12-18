package tasks;

public class Subtask extends Task{
    private Epic epic;

    public void setEpic(Epic epic) {
        this.epic = epic;
    }

    public Subtask(int taskUID, String name, String description, Epic epic) {
        super(taskUID, name, description);
        this.epic = epic;
    }

    public Subtask(int taskUID, String name, Epic epic) {
        super(taskUID, name);
        this.epic = epic;
    }

    public Subtask(int taskUID, String name, String description) {
        super(taskUID, name, description);
    }

    public Subtask(int taskUID, String name) {
        super(taskUID, name);
    }

    public Subtask(Subtask st) {
        super(st);
        this.epic = st.epic;
    }

    public Epic getEpic() {
        return epic;
    }

    protected void updateEpic() {
        if (this.epic != null) {
            this.epic.checkStatus();
        }
    }

    @Override
    public void setStatus(TaskStatus status) {
        super.setStatus(status);
        updateEpic();
    }
}
