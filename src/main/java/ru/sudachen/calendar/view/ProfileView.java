
package ru.sudachen.calendar.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import ru.sudachen.calendar.service.UserService;

public class ProfileView {

    private final Stage stage;
    private final int userId;
    private final UserService service = new UserService();

    public ProfileView(Stage stage, int userId) {
        this.stage = stage;
        this.userId = userId;
    }

    public void show() {
        try {
            UserService.UserProfile profile =
                    service.getProfile(userId);

            Label title = new Label("Мой профиль");
            title.setStyle(
                    "-fx-font-size: 25px; -fx-font-weight: bold;"
            );

            TextField username = new TextField(
                    profile.username()
            );

            TextField email = new TextField(
                    profile.email()
            );

            Button save = new Button("Сохранить изменения");
            Button changePassword = new Button("Изменить пароль");
            Button back = new Button("Назад");

            Label message = new Label();

            save.setOnAction(e -> {
                try {
                    service.updateProfile(
                            userId,
                            username.getText(),
                            email.getText()
                    );

                    message.setText(
                            "Данные успешно сохранены!"
                    );
                    message.setStyle(
                            "-fx-text-fill: green;"
                    );

                } catch (Exception ex) {
                    message.setText(
                            "Ошибка: " + ex.getMessage()
                    );
                    message.setStyle(
                            "-fx-text-fill: red;"
                    );
                }
            });

            changePassword.setOnAction(e ->
                    showPasswordDialog()
            );

            back.setOnAction(e ->
                    new CalendarView(stage, userId).show()
            );

            VBox root = new VBox(
                    15,
                    title,
                    new Label("Логин:"),
                    username,
                    new Label("Email:"),
                    email,
                    save,
                    changePassword,
                    message,
                    back
            );

            root.setAlignment(Pos.TOP_CENTER);
            root.setPadding(new Insets(40));
            root.setStyle(
                    "-fx-background-color: #F5F7FA;"
            );

            username.setMaxWidth(350);
            email.setMaxWidth(350);

            stage.setTitle("Электронный календарь — профиль");
            stage.setScene(new Scene(root, 700, 600));

        } catch (Exception e) {
            new Alert(
                    Alert.AlertType.ERROR,
                    "Ошибка загрузки профиля: " + e.getMessage()
            ).showAndWait();
        }
    }

    private void showPasswordDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Изменение пароля");

        PasswordField oldPassword = new PasswordField();
        oldPassword.setPromptText("Старый пароль");

        PasswordField newPassword = new PasswordField();
        newPassword.setPromptText("Новый пароль");

        PasswordField confirm = new PasswordField();
        confirm.setPromptText("Повторите новый пароль");

        VBox content = new VBox(
                10,
                new Label("Старый пароль:"),
                oldPassword,
                new Label("Новый пароль:"),
                newPassword,
                new Label("Подтверждение:"),
                confirm
        );

        content.setPadding(new Insets(20));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.OK,
                ButtonType.CANCEL
        );

        dialog.showAndWait().ifPresent(result -> {
            if (result != ButtonType.OK) return;

            if (!newPassword.getText()
                    .equals(confirm.getText())) {

                showAlert("Новые пароли не совпадают");
                return;
            }

            try {
                service.changePassword(
                        userId,
                        oldPassword.getText(),
                        newPassword.getText()
                );

                showAlert("Пароль успешно изменён!");

            } catch (Exception e) {
                showAlert("Ошибка: " + e.getMessage());
            }
        });
    }

    private void showAlert(String message) {
        new Alert(
                Alert.AlertType.INFORMATION,
                message
        ).showAndWait();
    }
}
