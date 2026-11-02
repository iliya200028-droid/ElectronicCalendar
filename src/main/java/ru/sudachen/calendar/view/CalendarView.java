
package ru.sudachen.calendar.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import ru.sudachen.calendar.service.ReminderService;
import ru.sudachen.calendar.controller.NavigationController;

public class CalendarView {

    private final Stage stage;
    private final int userId;

    private static final ReminderService reminderService =
            new ReminderService();

    public CalendarView(Stage stage, int userId) {
        this.stage = stage;
        this.userId = userId;
    }

    public void show() {
        Label title = new Label("Электронный календарь");
        title.setStyle(
                "-fx-font-size: 25px; -fx-font-weight: bold;"
        );

        Label userLabel = new Label(
                "Пользователь ID: " + userId
        );

        NavigationController navigation =
                new NavigationController(stage, userId);
        Button events = new Button("События");
        Button tasks = new Button("Задачи");
        Button profile = new Button("Профиль");
        Button logout = new Button("Выйти");


        events.setOnAction(e ->
                navigation.openEvents()
        );

        tasks.setOnAction(e ->
                navigation.openTasks()
        );

        profile.setOnAction(e ->
                navigation.openProfile()
        );

        logout.setOnAction(e -> {
            reminderService.stop();
            navigation.openLogin();
        });


        HBox menu = new HBox(
                12, events, tasks, profile, logout
        );
        menu.setPadding(new Insets(10));

        CalendarGridView calendar =
                new CalendarGridView(userId);

        VBox header = new VBox(
                10, title, userLabel, menu
        );
        header.setPadding(new Insets(15));

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(calendar.getView());

        stage.setTitle("Электронный календарь");
        stage.setScene(new Scene(root, 1050, 750));
        stage.setMinWidth(800);
        stage.setMinHeight(550);
        reminderService.start(userId);
        stage.setOnCloseRequest(e -> reminderService.stop());
        stage.show();
    }
}
