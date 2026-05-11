package tracker.controllers;

import tracker.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * История просмотров в памяти без дубликатов.
 * Хранит только последний просмотр каждой задачи.
 * Добавление, удаление и обновление позиции задачи выполняются за O(1).
 */
public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node<Task>> historyMap = new HashMap<>();
    private Node<Task> head;
    private Node<Task> tail;

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        int taskId = task.getId();

        // Если задача уже есть в истории — удаляем старый узел
        remove(taskId);

        // Сохраняем копию, а не ссылку на исходный объект
        Task snapshot = copy(task);

        // Добавляем в конец списка
        Node<Task> newNode = linkLast(snapshot);

        // Обновляем индекс
        historyMap.put(taskId, newNode);
    }

    @Override
    public void remove(int id) {
        Node<Task> node = historyMap.remove(id);
        if (node != null) {
            removeNode(node);
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private Node<Task> linkLast(Task task) {
        final Node<Task> newNode = new Node<>(tail, task, null);

        if (head == null) {
            head = newNode;
        } else {
            tail.next = newNode;
        }

        tail = newNode;
        return newNode;
    }

    private void removeNode(Node<Task> node) {
        Node<Task> prev = node.prev;
        Node<Task> next = node.next;

        if (prev == null) {
            head = next;
        } else {
            prev.next = next;
        }

        if (next == null) {
            tail = prev;
        } else {
            next.prev = prev;
        }
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node<Task> current = head;

        while (current != null) {
            tasks.add(current.data);
            current = current.next;
        }

        return tasks;
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

    private static class Node<T> {
        private final T data;
        private Node<T> prev;
        private Node<T> next;

        private Node(Node<T> prev, T data, Node<T> next) {
            this.prev = prev;
            this.data = data;
            this.next = next;
        }
    }
}