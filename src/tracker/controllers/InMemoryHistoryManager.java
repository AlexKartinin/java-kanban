package tracker.controllers;

import tracker.model.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Простая реализация истории просмотров в памяти.
 * Допускает дублирование и хранит только последние 10 просмотренных задач.
 */
public class InMemoryHistoryManager implements HistoryManager {
    private static final int MAX_HISTORY = 10;

    private final List<Task> history = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (task == null) return;

        // сохраняем снимок (копию), а не ссылку
        Task snapshot = copy(task);

        history.add(snapshot);
        if (history.size() > MAX_HISTORY) {
            history.removeFirst();
        }
    }

    private Task copy(Task task) {
        if (task instanceof tracker.model.Subtask st) {
            return new tracker.model.Subtask(st);
        }
        if (task instanceof tracker.model.Epic e) {
            return new tracker.model.Epic(e);
        }
        return new Task(task);
    }

    @Override
    public List<Task> getHistory() {
        return List.copyOf(history);
    }
}
