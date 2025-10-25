package todo;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class TodoApp {
    // Коды пунктов меню
    private static final int CMD_EXIT = 0;
    private static final int CMD_CREATE = 1;
    private static final int CMD_EDIT = 2;
    private static final int CMD_DELETE = 3;
    private static final int CMD_LIST = 4;
    private static final int CMD_SEARCH = 5;
    private static final int CMD_CHANGE_PRIORITY = 6;
    private static final int CMD_CHANGE_STATUS = 7;

    private final TaskService svc = new TaskService();
    private final Scanner scan = new Scanner(System.in);

    public static void main(String[] args) {
        new TodoApp().run();
    }
    // Основной цикл приложения: печать меню → чтение команды → обработка через switch.
    // Исключения ловим на уровне цикла, чтобы приложение не падало.
    private void run() {
        System.out.println("To-Do List");
        boolean running = true;
        while (running) {
            try {
                printMenu();
                int cmd = readInt("Выберите действие ");
                switch (cmd) {
                    case CMD_CREATE -> createFlow();
                    case CMD_EDIT -> editFlow();
                    case CMD_DELETE -> deleteFlow();
                    case CMD_LIST -> listFlow();
                    case CMD_SEARCH -> searchFlow();
                    case CMD_CHANGE_PRIORITY -> changePriorityFlow();
                    case CMD_CHANGE_STATUS -> changeStatusFlow();
                    case CMD_EXIT -> {
                        running = false;
                        System.out.println("До встречи :)");
                    }
                    default -> System.out.println("Неизвестная команда. Повторите, пожалуйста.");
                }
            } catch (IllegalArgumentException | DateTimeParseException e) {
                System.out.println("Ошибка данных: " + e.getMessage());
            } catch (NoSuchElementException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println("Непредвиденная ошибка: " + e);
            }
            System.out.println();
        }
    }
    // UI-метод
    private void printMenu() {
        System.out.println("""
                Каталог действий
                %d - Создать задачу
                %d - Редактировать задачу
                %d - Удалить задачу
                %d - Показать задачи
                %d - Поиск по атрибутам
                %d - Изменить приоритет
                %d - Изменить статус
                %d - Выход
                """.formatted(
                CMD_CREATE, CMD_EDIT, CMD_DELETE, CMD_LIST, CMD_SEARCH,
                CMD_CHANGE_PRIORITY, CMD_CHANGE_STATUS, CMD_EXIT
        ));
    }

    private void createFlow() {
        System.out.println("Окей, создаём новую задачу!");
        String title = readNonEmpty("Заголовок: ");
        String desc = readLine("Описание (можно пусто): ");
        LocalDate due = readDate("Срок (YYYY-MM-DD): ");
        Task.Priority prio = readEnum("Приоритет (LOW/MEDIUM/HIGH): ", Task.Priority.class);
        Task t = svc.create(title, desc, due, prio);
        System.out.println("Создано:\n" + t);
    }

    private void editFlow() {
        int id = readInt("ID задачи для редактирования: ");

        var taskOpt = svc.findById(id);
        if (taskOpt.isEmpty()) throw new NoSuchElementException("Задача #" + id + " не найдена");
        var t = taskOpt.get();
        System.out.println("Текущие данные:\n" + t);
        System.out.println("Введите новые значения или оставьте пустым, чтобы не менять.");

        String title = readOptional("Новый заголовок: ");
        if (!title.isBlank()) svc.updateTitle(id, title);

        String desc = readOptional("Новое описание: ");
        if (!desc.isBlank()) svc.updateDescription(id, desc);

        String dueStr = readOptional("Новый срок (YYYY-MM-DD): ");
        if (!dueStr.isBlank()) svc.updateDueDate(id, parseDate(dueStr));

        String prioStr = readOptional("Новый приоритет (LOW/MEDIUM/HIGH): ");
        if (!prioStr.isBlank()) svc.updatePriority(id, parseEnum(prioStr, Task.Priority.class));

        System.out.println("Обновлено:\n" + svc.findById(id).get());
    }

    private void deleteFlow() {
        int id = readInt("ID задачи для удаления: ");
        boolean removed = svc.delete(id);
        System.out.println(removed ? "Удалено." : "Задача не найдена.");
    }

    private void listFlow() {
        if (svc.isEmpty()) {
            System.out.println("Список пуст.");
            return;
        }
        List<Task> all = svc.allSortedByDueDate();
        System.out.println("Задачи (по сроку):");
        for (Task t : all) {
            System.out.println(t);
        }
    }

    private void searchFlow() {
        System.out.println("Поиск. Можно оставлять поля пустыми, если фильтр не нужен");
        String needle = readOptional("Содержит в заголовке/описании: ");
        String prioStr = readOptional("Приоритет (LOW/MEDIUM/HIGH): ");
        String statusStr = readOptional("Статус (TODO/IN_PROGRESS/DONE): ");
        String fromStr = readOptional("Срок с (YYYY-MM-DD): ");
        String toStr = readOptional("Срок по (YYYY-MM-DD): ");

        Task.Priority prio = prioStr.isBlank() ? null : parseEnum(prioStr, Task.Priority.class);
        Task.Status status = statusStr.isBlank() ? null : parseEnum(statusStr, Task.Status.class);
        LocalDate from = fromStr.isBlank() ? null : parseDate(fromStr);
        LocalDate to = toStr.isBlank() ? null : parseDate(toStr);

        var found = svc.search(needle, prio, status, from, to);
        if (found.isEmpty()) {
            System.out.println("Ничего не найдено.");
        } else {
            System.out.println("Найдено:");
            for (Task t : found) {
                System.out.println(t);
            }
        }
    }

    private void changePriorityFlow() {
        int id = readInt("ID задачи: ");
        Task.Priority prio = readEnum("Новый приоритет (LOW/MEDIUM/HIGH): ", Task.Priority.class);
        svc.updatePriority(id, prio);
        System.out.println("Готово.");
    }

    private void changeStatusFlow() {
        int id = readInt("ID задачи: ");
        Task.Status status = readEnum("Новый статус (TODO/IN_PROGRESS/DONE): ", Task.Status.class);
        svc.updateStatus(id, status);
        System.out.println("Готово.");
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scan.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Введите целое число.");
            }
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scan.nextLine();
    }

    private String readNonEmpty(String prompt) {
        while (true) {
            String s = readLine(prompt);
            if (!s.isBlank()) return s.trim();
            System.out.println("Поле не может быть пустым.");
        }
    }

    private String readOptional(String prompt) {
        System.out.print(prompt);
        return scan.nextLine().trim();
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            String s = readNonEmpty(prompt);
            try {
                return parseDate(s);
            } catch (DateTimeParseException e) {
                System.out.println("Неверный формат даты. Используйте YYYY-MM-DD.");
            }
        }
    }

    private LocalDate parseDate(String s) {
        return LocalDate.parse(s.trim());
    }

    private <E extends Enum<E>> E readEnum(String prompt, Class<E> enumClass) {
        while (true) {
            String s = readNonEmpty(prompt);
            try {
                return parseEnum(s, enumClass);
            } catch (IllegalArgumentException e) {
                System.out.println("Недопустимое значение. Допустимые: " + String.join("/", enumNames(enumClass)));
            }
        }
    }

    private <E extends Enum<E>> E parseEnum(String s, Class<E> enumClass) {
        return Enum.valueOf(enumClass, s.trim().toUpperCase());
    }

    private <E extends Enum<E>> String[] enumNames(Class<E> enumClass) {
        return java.util.Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).toArray(String[]::new);
    }
}
