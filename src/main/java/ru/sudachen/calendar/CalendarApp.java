
package ru.sudachen.calendar;

import javafx.application.Application;
import javafx.stage.Stage;
import ru.sudachen.calendar.database.DatabaseManager;
import ru.sudachen.calendar.view.AuthView;

public class CalendarApp extends Application {

    @Override
    public void start(Stage stage) {
        DatabaseManager.initializeDatabase();
        DatabaseManager.addCategoryColumns();

        AuthView authView = new AuthView(stage);
        authView.showLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
