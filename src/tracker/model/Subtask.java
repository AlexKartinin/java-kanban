package tracker.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private Epic epic;

    public void setEpic(Epic epic) {
        if (epic == null) {
            this.epic = null;
            return;
        }
        if (epic.getId() == this.getId()) return;
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

    private void notifyEpic() {
        if (epic != null) {
            epic.recalculate();
        }
    }

    @Override
    public void setStatus(TaskStatus status) {
        super.setStatus(status);
        notifyEpic();
    }

    @Override
    public void setDuration(Duration duration) {
        super.setDuration(duration);
        notifyEpic();
    }

    @Override
    public void setStartTime(LocalDateTime startTime) {
        super.setStartTime(startTime);
        notifyEpic();
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    @Override
    public void restoreStatus(TaskStatus status) {
        super.restoreStatus(status);
    }
}
