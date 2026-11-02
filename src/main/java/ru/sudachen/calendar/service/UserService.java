
package ru.sudachen.calendar.service;

import ru.sudachen.calendar.database.DatabaseManager;
import ru.sudachen.calendar.security.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {

    public void register(
            String username,
            String email,
            String password) throws SQLException {

        if (username == null || username.isBlank()
                || email == null || email.isBlank()
                || password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "Enter username, email and password (min 8 characters)"
            );
        }

        String sql = """
                INSERT INTO users
                (username, email, password_hash)
                VALUES (?, ?, ?)
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username.trim());
            statement.setString(2, email.trim());
            statement.setString(
                    3, PasswordUtils.hashPassword(password)
            );

            statement.executeUpdate();
        }
    }

    public int login(
            String username,
            String password) throws SQLException {

        String sql = """
                SELECT id, password_hash
                FROM users
                WHERE username = ?
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()
                        && PasswordUtils.verifyPassword(
                        password,
                        result.getString("password_hash"))) {

                    return result.getInt("id");
                }
            }
        }

        return -1;
    }

    public record UserProfile(
            int id,
            String username,
            String email
    ) {}

    public UserProfile getProfile(int userId)
            throws SQLException {

        String sql = """
                SELECT id, username, email
                FROM users
                WHERE id = ?
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserProfile(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("email")
                    );
                }
            }
        }

        throw new SQLException("Пользователь не найден");
    }

    public void updateProfile(
            int userId,
            String username,
            String email
    ) throws SQLException {

        if (username == null || username.isBlank()
                || email == null || email.isBlank()
                || !email.contains("@")) {
            throw new IllegalArgumentException(
                    "Введите корректный логин и email"
            );
        }

        String sql = """
                UPDATE users
                SET username = ?, email = ?
                WHERE id = ?
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ps.setString(2, email.trim());
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    public void changePassword(
            int userId,
            String oldPassword,
            String newPassword
    ) throws SQLException {

        if (newPassword == null
                || newPassword.length() < 8) {
            throw new IllegalArgumentException(
                    "Новый пароль должен содержать минимум 8 символов"
            );
        }

        String selectSql = """
                SELECT password_hash
                FROM users
                WHERE id = ?
                """;

        String storedHash;

        try (Connection connection =
                     DatabaseManager.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(selectSql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException(
                            "Пользователь не найден"
                    );
                }

                storedHash = rs.getString("password_hash");
            }
        }

        if (!PasswordUtils.verifyPassword(
                oldPassword, storedHash)) {
            throw new IllegalArgumentException(
                    "Старый пароль указан неверно"
            );
        }

        String updateSql = """
                UPDATE users
                SET password_hash = ?
                WHERE id = ?
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(updateSql)) {

            ps.setString(
                    1, PasswordUtils.hashPassword(newPassword)
            );
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

}
