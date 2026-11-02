
package ru.sudachen.calendar.controller;

import javafx.stage.Stage;
import ru.sudachen.calendar.view.*;

public class NavigationController {

    private final Stage stage;
    private final int userId;

    public NavigationController(Stage stage, int userId) {
        this.stage = stage;
        this.userId = userId;
    }

    public void openCalendar() {
        new CalendarView(stage, userId).show();
    }

    public void openEvents() {
        new EventView(stage, userId).show();
    }

    public void openTasks() {
        new TaskView(stage, userId).show();
    }

    public void openProfile() {
        new ProfileView(stage, userId).show();
    }

    public void openLogin() {
        new AuthView(stage).showLogin();
    }
}
