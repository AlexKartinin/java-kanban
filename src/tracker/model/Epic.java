package tracker.model;

import java.util.ArrayList;

public class Epic extends Task {
    private int openSubtasks;

    private final ArrayList<Subtask> subtasks;

    public Epic(int taskUID, String name, String description) {
        super(taskUID, name, description);
        subtasks = new ArrayList<>();
    }

    public Epic(int taskUID, String name) {
        super(taskUID, name);
        subtasks = new ArrayList<>();
    }

    public Epic(int taskUID, String name, String description, ArrayList<Subtask> subtasks) {
        super(taskUID, name, description);
        this.subtasks = new ArrayList<>(subtasks);
    }

    public Epic(int taskUID, String name, ArrayList<Subtask> subtasks) {
        super(taskUID, name);
        this.subtasks = new ArrayList<>(subtasks);
    }

    public Epic(Epic e) {
        super(e);
        this.subtasks = new ArrayList<>(e.getSubtasks());
        this.openSubtasks = e.openSubtasks;
    }

    public void addSubTask(Subtask s) {
        if (s == null) {
            return;
        }

        if (s.getId() == this.getId()) {
            return;
        }

        if (subtasks.contains(s)) {
            return;
        }

        subtasks.add(s);
        checkStatus();
    }

    public void clearSubtasks() {
        subtasks.clear();
        checkStatus();
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    protected void checkStatus() {
        int inProgressTasks = 0;
        int doneTasks = 0;

        if (subtasks.isEmpty()) {
            super.setStatus(TaskStatus.NEW);
        } else {
            for (Subtask subtask : subtasks) {
                switch (subtask.getStatus()) {
                    case IN_PROGRESS -> inProgressTasks++;
                    case DONE -> doneTasks++;
                }
            }

            boolean inProgress = inProgressTasks > 0 || (doneTasks > 0 && doneTasks < subtasks.size());
            boolean allDone = doneTasks == subtasks.size();

            if (inProgress) {
                super.setStatus(TaskStatus.IN_PROGRESS);
            } else if (allDone) {
                super.setStatus(TaskStatus.DONE);
            } else {
                super.setStatus(TaskStatus.NEW);
            }
        }
    }

    public void removeSubtaskById(int id) {
        subtasks.removeIf(s -> s.getId() == id);
        checkStatus();
    }

    @Override
    public void setStatus(TaskStatus status) {
        throw new UnsupportedOperationException(
                "Статус Epic рассчитывается автоматически по статусам подзадач"
        );
    }

    @Override
    public void restoreStatus(TaskStatus status) {
        super.restoreStatus(status);
    }
}
