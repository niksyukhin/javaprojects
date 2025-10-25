package todo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task {
    public enum Priority { LOW , MEDIUM , HIGH }
    public enum Status { TODO, IN_PROGRESS, DONE }

    // Уникальный идентификатор задачи. Генерируется в TaskService и неизменяем.
    private final int id;
    // Заголовок задачи
    private String title;
    // Описание задачи
    private String description;
    // Срок выполнения
    private LocalDate dueDate;
    // Приоритет задачи
    private Priority priority;
    // Текущий статус задачи
    private Status status;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    // Конструктор доменного объекта. Здесь выполняется базовая валидация входных данных
    public Task(int id, String title, String description, LocalDate dueDate, Priority priority) {

        if (title == null) {
            throw new IllegalArgumentException("Заголовок не может быть null");
        }
        if (title.isBlank()) {
            throw new IllegalArgumentException("Заголовок не должен быть пустым");
        }
        Objects.requireNonNull(dueDate, "Дата не должна быть нулевой");
        Objects.requireNonNull(priority, "Приоритет не должен быть нулевым");
        this.id = id;
        this.title = title.trim();
        this.description = description == null ? "" : description.trim();
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = Status.TODO;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public Priority getPriority() { return priority; }
    public Status getStatus() { return status; }

    // Мутирующие методы инкапсулирует инварианты модели
    public void setTitle(String title) {
        if (title == null) {
            throw new IllegalArgumentException("Заголовок не может быть null");
        }
        if (title.isBlank()) {
            throw new IllegalArgumentException("Заголовок не должен быть пустым");
        }
        this.title = title.trim();
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description.trim();
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = Objects.requireNonNull(dueDate, "Дата не должна быть нулевой");
    }

    public void setPriority(Priority priority) {
        this.priority = Objects.requireNonNull(priority, "Приоритет не должен быть нулевым");
    }

    public void setStatus(Status status) {
        this.status = Objects.requireNonNull(status, "Статус не должен быть нулевым");
    }

    // Представле задачи для консоли
    @Override
    public String toString() {
        return """
               Task %d | %s
               Описание : %s
               Срок     : %s
               Приоритет: %s
               Статус   : %s
               """.formatted(
                id, title,
                description.isEmpty() ? "-" : description,
                dueDate.format(FMT),
                priority,
                status
        );
    }

}