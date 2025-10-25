package todo;

import java.time.LocalDate;
import java.util.*;

public class TaskService {
    // In-memory хранилище задач
    private final List<Task> tasks = new ArrayList<>();
    private int nextId = 1;   // Генератор уникальных id

    public Task create(String title, String desc, LocalDate due, Task.Priority prio) {
        Task t = new Task(nextId++, title, desc, due, prio);
        tasks.add(t);
        return t;
    }

    // Поиск по коллекции
    public Optional<Task> findById(int id) {
        for (Task t : tasks) {
            if (t.getId() == id) return Optional.of(t);
        }
        return Optional.empty();
    }
    // Удаление по id. Возвращаем boolean
    public boolean delete(int id) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId() == id) {
                tasks.remove(i);
                return true;
            }
        }
        return false;
    }

    public List<Task> allSortedByDueDate() {
        List<Task> sorted = new ArrayList<>(tasks);
        //
        sorted.sort((a, b) -> {
            int cmp = a.getDueDate().compareTo(b.getDueDate());
            if (cmp != 0) return cmp;
            return Integer.compare(a.getId(), b.getId());
        });
        return sorted;
    }

    // Команды изменения атрибутов
    public void updateTitle(int id, String newTitle) {
        Task t = requireTask(id);
        t.setTitle(newTitle);
    }

    public void updateDescription(int id, String newDesc) {
        Task t = requireTask(id);
        if (newDesc == null) newDesc = "";
        t.setDescription(newDesc);
    }

    public void updateDueDate(int id, LocalDate newDue) {
        Task t = requireTask(id);
        t.setDueDate(newDue);
    }

    public void updatePriority(int id, Task.Priority prio) {
        Task t = requireTask(id);
        t.setPriority(prio);
    }

    public void updateStatus(int id, Task.Status status) {
        Task t = requireTask(id);
        t.setStatus(status);
    }

    private Task requireTask(int id) {
        Optional<Task> opt = findById(id);
        if (opt.isEmpty()) throw new NoSuchElementException("Task #" + id + " not found");
        return opt.get();
    }

    // Универсальный фильтр
    public List<Task> search(String titleContains,
                             Task.Priority priority,
                             Task.Status status,
                             LocalDate dueFromInclusive,
                             LocalDate dueToInclusive) {


        List<Task> res = new ArrayList<>();
        for (Task t : tasks) {
            boolean ok = true;

            if (titleContains != null && !titleContains.isBlank()) {
                String needle = titleContains.toLowerCase();
                String hayTitle = t.getTitle() == null ? "" : t.getTitle().toLowerCase();
                String hayDesc = t.getDescription() == null ? "" : t.getDescription().toLowerCase();
                if (!(hayTitle.contains(needle) || hayDesc.contains(needle))) ok = false;
            }
            if (ok && priority != null && t.getPriority() != priority) ok = false;
            if (ok && status != null && t.getStatus() != status) ok = false;
            if (ok && dueFromInclusive != null && t.getDueDate().isBefore(dueFromInclusive)) ok = false;
            if (ok && dueToInclusive != null && t.getDueDate().isAfter(dueToInclusive)) ok = false;

            if (ok) res.add(t);
        }

        res.sort((a, b) -> {
            int cmp = a.getDueDate().compareTo(b.getDueDate());
            if (cmp != 0) return cmp;
            return Integer.compare(a.getId(), b.getId());
        });
        return res;
    }

    public boolean isEmpty() {
        return tasks.isEmpty();
    }
}