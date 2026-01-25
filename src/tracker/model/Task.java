package tracker.model;

import java.util.Objects;

public class Task {
    private int id;
    private final String name;
    private String description;

    public void setId(int id) {
        this.id = id;
    }

    private TaskStatus status;

    public Task(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = TaskStatus.NEW;
    }

    public Task(int id, String name) {
        this.id = id;
        this.name = name;
        this.status = TaskStatus.NEW;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public Task(Task t) {
        this.id = t.id;
        this.status = t.status;
        this.name = t.name;
        this.description = t.description;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "{id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                '}';
    }
}
