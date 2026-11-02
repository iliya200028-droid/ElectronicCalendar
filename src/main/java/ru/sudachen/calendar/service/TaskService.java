
package ru.sudachen.calendar.service;

import ru.sudachen.calendar.database.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskService {

    public record Task(
            int id,
            String title,
            String description,
            int priority,
            String deadline,
            boolean completed,
            String category
    ) {
        public boolean isOverdue() {
            return !completed
                    && deadline != null
                    && !deadline.isBlank()
                    && LocalDate.parse(deadline)
                    .isBefore(LocalDate.now());
        }

        public String getStatus() {
            if (completed) return "Выполнена";
            if (isOverdue()) return "Просрочена";
            return "В работе";
        }

        public String getPriorityName() {
            return switch (priority) {
                case 3 -> "Высокий";
                case 2 -> "Средний";
                default -> "Низкий";
            };
        }
    }

    public void addTask(
            int userId,
            String title,
            String description,
            int priority,
            String deadline
    ) throws SQLException {

        validate(title, priority, deadline);

        String sql = """
                INSERT INTO tasks
                (user_id, title, description,
                 priority, deadline, completed)
                VALUES (?, ?, ?, ?, ?, 0)
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, title.trim());
            ps.setString(3, description);
            ps.setInt(4, priority);
            ps.setString(5, deadline);
            ps.executeUpdate();
        }
    }

    public List<Task> getTasks(int userId)
            throws SQLException {

        List<Task> tasks = new ArrayList<>();

        String sql = """
        SELECT id, title, description,
               priority, deadline, completed, category
        FROM tasks
        WHERE user_id = ?
        ORDER BY completed ASC,
                 deadline ASC,
                 priority DESC
        """;

        try (Connection connection =
                     DatabaseManager.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(new Task(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("priority"),
                            rs.getString("deadline"),
                            rs.getInt("completed") == 1,
                            rs.getString("category")
                    ));
                }
            }
        }

        return tasks;
    }

    public void updateTask(
            int userId,
            int taskId,
            String title,
            String description,
            int priority,
            String deadline
    ) throws SQLException {

        validate(title, priority, deadline);

        String sql = """
                UPDATE tasks
                SET title = ?, description = ?,
                    priority = ?, deadline = ?
                WHERE id = ? AND user_id = ?
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setString(1, title.trim());
            ps.setString(2, description);
            ps.setInt(3, priority);
            ps.setString(4, deadline);
            ps.setInt(5, taskId);
            ps.setInt(6, userId);
            ps.executeUpdate();
        }
    }

    public void setCompleted(
            int userId, int taskId, boolean completed)
            throws SQLException {

        String sql = """
                UPDATE tasks SET completed = ?
                WHERE id = ? AND user_id = ?
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(1, completed ? 1 : 0);
            ps.setInt(2, taskId);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    public void deleteTask(int userId, int taskId)
            throws SQLException {

        String sql = """
                DELETE FROM tasks
                WHERE id = ? AND user_id = ?
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(1, taskId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    private void validate(
            String title, int priority, String deadline) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Введите название задачи"
            );
        }

        if (priority < 1 || priority > 3) {
            throw new IllegalArgumentException(
                    "Некорректный приоритет"
            );
        }

        if (deadline != null && !deadline.isBlank()) {
            LocalDate.parse(deadline);
        }
    }

    public void setCategory(
            int userId, int taskId, String category
    ) throws SQLException {

        String sql = """
            UPDATE tasks SET category = ?
            WHERE id = ? AND user_id = ?
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setString(1, category);
            ps.setInt(2, taskId);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    public void addTaskWithCategory(
            int userId,
            String title,
            String description,
            int priority,
            String deadline,
            String category
    ) throws SQLException {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Название задачи обязательно"
            );
        }

        if (priority < 0 || priority > 2) {
            throw new IllegalArgumentException(
                    "Некорректный приоритет"
            );
        }

        if (deadline != null && !deadline.isBlank()) {
            java.time.LocalDate.parse(deadline);
        }

        String sql = """
            INSERT INTO tasks
            (user_id, title, description,
             priority, deadline, completed, category)
            VALUES (?, ?, ?, ?, ?, 0, ?)
            """;

        try (Connection connection =
                     DatabaseManager.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, title.trim());
            ps.setString(3, description);
            ps.setInt(4, priority);
            ps.setString(5, deadline);
            ps.setString(6, category);

            ps.executeUpdate();
        }
    }

}
