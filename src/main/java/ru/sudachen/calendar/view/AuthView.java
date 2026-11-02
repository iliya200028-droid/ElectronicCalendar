
package ru.sudachen.calendar.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.sudachen.calendar.service.UserService;

public class AuthView {

    private final Stage stage;
    private final UserService userService = new UserService();

    public AuthView(Stage stage) {
        this.stage = stage;
    }

    public void showLogin() {
        Label title = new Label("Вход в календарь");
        title.setStyle("-fx-font-size: 25px; -fx-font-weight: bold;");

        TextField username = new TextField();
        username.setPromptText("Имя пользователя");
        username.setMaxWidth(300);

        PasswordField password = new PasswordField();
        password.setPromptText("Пароль");
        password.setMaxWidth(300);

        Label message = new Label();
        message.setWrapText(true);

        Button loginButton = new Button("Войти");
        loginButton.setPrefWidth(300);

        Button registerButton = new Button("Создать аккаунт");
        registerButton.setPrefWidth(300);

        loginButton.setOnAction(e -> {
            try {
                int userId = userService.login(
                        username.getText().trim(),
                        password.getText()
                );

                if (userId > 0) {
                    new CalendarView(stage, userId).show();
                } else {
                    message.setText("Неверный логин или пароль");
                }
            } catch (Exception ex) {
                message.setText("Ошибка входа: " + ex.getMessage());
            }
        });

        registerButton.setOnAction(e -> showRegistration());

        VBox root = createLayout(
                title, username, password,
                loginButton, registerButton, message
        );

        stage.setTitle("Электронный календарь — вход");
        stage.setScene(new Scene(root, 500, 450));
        stage.show();
    }

    public void showRegistration() {
        Label title = new Label("Регистрация");
        title.setStyle("-fx-font-size: 25px; -fx-font-weight: bold;");

        TextField username = new TextField();
        username.setPromptText("Имя пользователя");
        username.setMaxWidth(300);

        TextField email = new TextField();
        email.setPromptText("Email");
        email.setMaxWidth(300);

        PasswordField password = new PasswordField();
        password.setPromptText("Пароль (минимум 8 символов)");
        password.setMaxWidth(300);

        PasswordField confirm = new PasswordField();
        confirm.setPromptText("Повторите пароль");
        confirm.setMaxWidth(300);

        Label message = new Label();
        message.setWrapText(true);

        Button saveButton = new Button("Зарегистрироваться");
        saveButton.setPrefWidth(300);

        Button backButton = new Button("Назад ко входу");
        backButton.setPrefWidth(300);

        saveButton.setOnAction(e -> {
            if (!password.getText().equals(confirm.getText())) {
                message.setText("Пароли не совпадают");
                return;
            }

            try {
                userService.register(
                        username.getText(),
                        email.getText(),
                        password.getText()
                );

                Alert alert = new Alert(
                        Alert.AlertType.INFORMATION,
                        "Регистрация прошла успешно!"
                );
                alert.showAndWait();
                showLogin();

            } catch (Exception ex) {
                message.setText(
                        "Ошибка регистрации: " + ex.getMessage()
                );
            }
        });

        backButton.setOnAction(e -> showLogin());

        VBox root = createLayout(
                title, username, email, password,
                confirm, saveButton, backButton, message
        );

        stage.setTitle("Электронный календарь — регистрация");
        stage.setScene(new Scene(root, 500, 550));
        stage.show();
    }

    private VBox createLayout(javafx.scene.Node... nodes) {
        VBox root = new VBox(15, nodes);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #F5F7FA;");
        return root;
    }
}
