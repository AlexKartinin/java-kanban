package tracker.model;

public class Subtask extends Task {
    private Epic epic;

    public void setEpic(Epic epic) {
        if (epic == null) {
            this.epic = null;
            return;
        }

        if (epic.getId() == this.getId()) {
            return;
        }

        this.epic = epic;
    }

    public Subtask(int taskUID, String name, String description, Epic epic) {
        super(taskUID, name, description);
        setEpic(epic);
    }

    public Subtask(int taskUID, String name, Epic epic) {
        super(taskUID, name);
        setEpic(epic);
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

    /**
     * Восстановление статуса при десериализации — не пересчитывает эпик.
     */
    @Override
    public void restoreStatus(TaskStatus status) {
        super.restoreStatus(status);
    }
}
