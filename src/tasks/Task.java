package tasks;

import java.util.Objects;

public class Task {
    private static int taskCounter = 0;

    private int taskUID;
    private String name;
    private String description;
    private TaskStatus status;

    static private void increaseTaskCounter() {
        taskCounter++;
    }

    private final void setTaskUID() {
        increaseTaskCounter();
        this.taskUID = Task.taskCounter;
    }

    public Task(String name, String description) {
        setTaskUID();
        this.name = name;
        this.description = description;
        this.status = TaskStatus.NEW;
    }

    public Task(String name) {
        setTaskUID();
        this.name = name;
        this.status = TaskStatus.NEW;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public int getTaskUID() {
        return taskUID;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return taskUID == task.taskUID;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(taskUID);
    }
}
