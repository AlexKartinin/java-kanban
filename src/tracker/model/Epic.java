package tracker.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class Epic extends Task {
    private final ArrayList<Subtask> subtasks;
    private LocalDateTime endTime;

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
        this.endTime = e.endTime;
    }

    public void addSubTask(Subtask s) {
        if (s == null) return;
        if (s.getId() == this.getId()) return;
        if (subtasks.contains(s)) return;
        subtasks.add(s);
        recalculate();
    }

    public void clearSubtasks() {
        subtasks.clear();
        recalculate();
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    public void removeSubtaskById(int id) {
        subtasks.removeIf(s -> s.getId() == id);
        recalculate();
    }

    protected void recalculate() {
        checkStatus();
        recalculateTiming();
    }

    protected void checkStatus() {
        if (subtasks.isEmpty()) {
            super.setStatus(TaskStatus.NEW);
            return;
        }

        long inProgress = subtasks.stream()
                .filter(s -> s.getStatus() == TaskStatus.IN_PROGRESS)
                .count();
        long done = subtasks.stream()
                .filter(s -> s.getStatus() == TaskStatus.DONE)
                .count();

        boolean allDone = done == subtasks.size();
        boolean anyProgress = inProgress > 0 || (done > 0 && done < subtasks.size());

        if (anyProgress) {
            super.setStatus(TaskStatus.IN_PROGRESS);
        } else if (allDone) {
            super.setStatus(TaskStatus.DONE);
        } else {
            super.setStatus(TaskStatus.NEW);
        }
    }

    private void recalculateTiming() {
        // duration = sum of subtask durations
        Duration totalDuration = subtasks.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);
        super.setDuration(totalDuration.isZero() ? null : totalDuration);

        // startTime = earliest subtask startTime
        LocalDateTime earliest = subtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
        super.setStartTime(earliest);

        // endTime = latest subtask endTime
        endTime = subtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public void setStatus(TaskStatus status) {
        throw new UnsupportedOperationException(
                "Статус Epic рассчитывается автоматически по статусам подзадач"
        );
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public void restoreStatus(TaskStatus status) {
        super.restoreStatus(status);
    }
}
