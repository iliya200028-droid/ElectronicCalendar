
package ru.sudachen.calendar.service;

import ru.sudachen.calendar.database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventService {

    public record Event(
            int id,
            String title,
            String description,
            String notes,
            String startTime,
            String endTime,
            String category
    ) {}

    public void addEvent(
            int userId,
            String title,
            String description,
            String notes,
            String startTime,
            String endTime
    ) throws SQLException {

        validate(title, startTime, endTime);

        String sql = """
        SELECT id, title, description, notes,
               start_time, end_time, category
        FROM events
        WHERE user_id = ?
        ORDER BY start_time
        """;

        try (Connection connection =
                     DatabaseManager.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, title.trim());
            ps.setString(3, description);
            ps.setString(4, notes);
            ps.setString(5, startTime);
            ps.setString(6, endTime);
            ps.executeUpdate();
        }
    }

    public List<Event> getEvents(int userId)
            throws SQLException {

        List<Event> events = new ArrayList<>();

        String sql = """
                SELECT id, title, description, notes,
                       start_time, end_time, category
                FROM events
                WHERE user_id = ?
                ORDER BY start_time
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(new Event(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("notes"),
                            rs.getString("start_time"),
                            rs.getString("end_time"),
                            rs.getString("category")
                    ));
                }
            }
        }

        return events;
    }

    public void updateEvent(
            int userId,
            int eventId,
            String title,
            String description,
            String notes,
            String startTime,
            String endTime
    ) throws SQLException {

        validate(title, startTime, endTime);

        String sql = """
                UPDATE events
                SET title = ?, description = ?, notes = ?,
                    start_time = ?, end_time = ?
                WHERE id = ? AND user_id = ?
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setString(1, title.trim());
            ps.setString(2, description);
            ps.setString(3, notes);
            ps.setString(4, startTime);
            ps.setString(5, endTime);
            ps.setInt(6, eventId);
            ps.setInt(7, userId);
            ps.executeUpdate();
        }
    }

    public void deleteEvent(int userId, int eventId)
            throws SQLException {

        String sql = """
                DELETE FROM events
                WHERE id = ? AND user_id = ?
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    private void validate(
            String title, String start, String end) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Название события обязательно"
            );
        }

        try {
            var startDate = java.time.LocalDateTime.parse(start);
            var endDate = java.time.LocalDateTime.parse(end);

            if (!endDate.isAfter(startDate)) {
                throw new IllegalArgumentException(
                        "Окончание должно быть позже начала"
                );
            }
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Некорректная дата или время"
            );
        }
    }

    public void setCategory(
            int userId, int eventId, String category
    ) throws SQLException {

        String sql = """
            UPDATE events SET category = ?
            WHERE id = ? AND user_id = ?
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setString(1, category);
            ps.setInt(2, eventId);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    public void addEventWithCategory(
            int userId,
            String title,
            String description,
            String notes,
            String startTime,
            String endTime,
            String category
    ) throws SQLException {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Название события обязательно"
            );
        }

        if (!java.time.LocalDateTime.parse(endTime)
                .isAfter(java.time.LocalDateTime.parse(startTime))) {
            throw new IllegalArgumentException(
                    "Окончание должно быть позже начала"
            );
        }

        String sql = """
            INSERT INTO events
            (user_id, title, description, notes,
             start_time, end_time, category)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection connection =
                     DatabaseManager.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, title.trim());
            ps.setString(3, description);
            ps.setString(4, notes);
            ps.setString(5, startTime);
            ps.setString(6, endTime);
            ps.setString(7, category);

            ps.executeUpdate();
        }
    }

}
