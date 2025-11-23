package tasks;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Subtask> subtasks;

    public Epic(String name, String description) {
        super(name, description);
        subtasks = new ArrayList<>();
    }

    public Epic(String name) {
        super(name);
        subtasks = new ArrayList<>();
    }

    public Epic(String name, String description, ArrayList<Subtask> subtasks) {
        super(name, description);
        this.subtasks = new ArrayList<>(subtasks);
    }

    public Epic(String name, ArrayList<Subtask> subtasks) {
        super(name);
        this.subtasks = new ArrayList<>(subtasks);
    }

    protected void addSubTask(Subtask s) {
        if (s == null) {
            return;
        }

        if (subtasks.contains(s)) {
            return;
        }

        subtasks.add(s);
    }

    @Override
    public void setStatus(TaskStatus status) {
        throw new UnsupportedOperationException("Status of Epic is calculated automatically based on its subtasks");
    }

    protected void checkStatus(TaskStatus status) {
        if (this.getStatus() == TaskStatus.NEW && status != TaskStatus.NEW) {
            setStatus(TaskStatus.IN_PROGRESS);
        }

        if (status == TaskStatus.DONE) {
            for (Subtask s : subtasks) {
                if (s.getStatus() != TaskStatus.DONE) {
                    return;
                }
            }
            setStatus(TaskStatus.DONE);
        }
    }
}
