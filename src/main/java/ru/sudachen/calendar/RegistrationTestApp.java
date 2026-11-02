
package ru.sudachen.calendar;

import ru.sudachen.calendar.database.DatabaseManager;
import ru.sudachen.calendar.service.UserService;

public class RegistrationTestApp {

    public static void main(String[] args) {

        DatabaseManager.initializeDatabase();

        UserService service = new UserService();

        try {
            String username = "testuser";
            String email = "testuser@example.com";
            String password = "TestPassword123";

            service.register(username, email, password);
            System.out.println("Registration successful!");

            int userId = service.login(username, password);

            System.out.println("Login user ID: " + userId);

            int wrongLogin = service.login(
                    username, "WrongPassword"
            );

            System.out.println(
                    "Wrong password result: " + wrongLogin
            );

        } catch (Exception e) {
            System.out.println(
                    "Error: " + e.getMessage()
            );
        }
    }
}
