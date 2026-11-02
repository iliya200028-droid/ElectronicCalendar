
package ru.sudachen.calendar.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class DatabaseManager {


    public static Connection getConnection()
            throws SQLException {
        try {
            String appData = System.getenv("APPDATA");

            java.nio.file.Path dataFolder;

            if (appData != null && !appData.isBlank()) {
                dataFolder = java.nio.file.Path.of(
                        appData, "ElectronicCalendar"
                );
            } else {
                dataFolder = java.nio.file.Path.of(
                        System.getProperty("user.home"),
                        ".electronic-calendar"
                );
            }

            java.nio.file.Files.createDirectories(dataFolder);

            java.nio.file.Path database =
                    dataFolder.resolve("calendar.db");

            java.nio.file.Path oldDatabase =
                    java.nio.file.Path.of("calendar.db")
                            .toAbsolutePath();

            if (!java.nio.file.Files.exists(database)
                    && java.nio.file.Files.exists(oldDatabase)) {

                java.nio.file.Files.copy(
                        oldDatabase, database
                );
            }

            return DriverManager.getConnection(
                    "jdbc:sqlite:" + database.toAbsolutePath()
            );

        } catch (java.io.IOException e) {
            throw new SQLException(
                    "Ошибка подготовки базы данных", e
            );
        }
    }


    public static void initializeDatabase() {

        String usersSql = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    email TEXT NOT NULL UNIQUE
                )
                """;

        String eventsSql = """
                CREATE TABLE IF NOT EXISTS events (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT,
                    notes TEXT,
                    start_time TEXT NOT NULL,
                    end_time TEXT NOT NULL,
                    FOREIGN KEY (user_id)
                        REFERENCES users(id)
                )
                """;

        String tasksSql = """
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT,
                    priority INTEGER DEFAULT 0,
                    deadline TEXT,
                    completed INTEGER DEFAULT 0,
                    FOREIGN KEY (user_id)
                        REFERENCES users(id)
                )
                """;

        try (Connection connection = getConnection();
             Statement statement =
                     connection.createStatement()) {

            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute(usersSql);
            statement.execute(eventsSql);
            statement.execute(tasksSql);

            System.out.println(
                    "Database initialized successfully!"
            );

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to initialize database", e
            );
        }
    }


    public static void addCategoryColumns() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            addColumnIfMissing(statement, "events");
            addColumnIfMissing(statement, "tasks");
            try (ResultSet rs = statement.executeQuery(
                    "PRAGMA table_info(events)")) {

                while (rs.next()) {
                    System.out.println(
                            "EVENT COLUMN: " + rs.getString("name")
                    );
                }
            }

            System.out.println("Categories initialized!");

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Category migration failed", e
            );
        }
    }

    private static void addColumnIfMissing(
            Statement statement,
            String table
    ) throws SQLException {

        boolean exists = false;

        try (ResultSet rs = statement.executeQuery(
                "PRAGMA table_info(" + table + ")")) {

            while (rs.next()) {
                if ("category".equalsIgnoreCase(
                        rs.getString("name"))) {
                    exists = true;
                    break;
                }
            }
        }

        if (!exists) {
            statement.executeUpdate(
                    "ALTER TABLE " + table +
                            " ADD COLUMN category TEXT DEFAULT 'Личное'"
            );
        }
    }


}
