
package ru.sudachen.calendar.view;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;

import ru.sudachen.calendar.service.EventService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class EventView {

    private final Stage stage;
    private final int userId;

    private final EventService service = new EventService();

    private final TableView<EventService.Event> table =
            new TableView<>();

    private final TextField searchField = new TextField();

    private final DatePicker dateFrom = new DatePicker();
    private final DatePicker dateTo = new DatePicker();

    private final ComboBox<String> categoryFilter =
            new ComboBox<>();

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    public EventView(Stage stage, int userId) {
        this.stage = stage;
        this.userId = userId;
    }

    public void show() {

        Label title = new Label("Управление событиями");
        title.setStyle(
                "-fx-font-size: 24px; -fx-font-weight: bold;"
        );

        TableColumn<EventService.Event, String> nameColumn =
                new TableColumn<>("Название");

        nameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().title())
        );

        TableColumn<EventService.Event, String> startColumn =
                new TableColumn<>("Начало");

        startColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().startTime())
        );

        TableColumn<EventService.Event, String> endColumn =
                new TableColumn<>("Окончание");

        endColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().endTime())
        );

        TableColumn<EventService.Event, String> categoryColumn =
                new TableColumn<>("Категория");

        categoryColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().category())
        );

        table.getColumns().setAll(
                nameColumn,
                startColumn,
                endColumn,
                categoryColumn
        );

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        searchField.setPromptText("Поиск событий...");
        searchField.setPrefWidth(200);

        dateFrom.setPromptText("Дата с");
        dateTo.setPromptText("Дата по");

        categoryFilter.getItems().setAll(
                "Все", "Личное", "Работа", "Учёба", "Другое"
        );
        categoryFilter.setValue("Все");

        Button clearFilters = new Button("Сбросить");

        HBox filters = new HBox(
                10,
                searchField,
                dateFrom,
                dateTo,
                categoryFilter,
                clearFilters
        );

        filters.setAlignment(Pos.CENTER_LEFT);

        searchField.textProperty().addListener(
                (obs, oldValue, newValue) -> refresh()
        );

        dateFrom.valueProperty().addListener(
                (obs, oldValue, newValue) -> refresh()
        );

        dateTo.valueProperty().addListener(
                (obs, oldValue, newValue) -> refresh()
        );

        categoryFilter.setOnAction(e -> refresh());

        clearFilters.setOnAction(e -> {
            searchField.clear();
            dateFrom.setValue(null);
            dateTo.setValue(null);
            categoryFilter.setValue("Все");
            refresh();
        });

        Button add = new Button("Добавить");
        Button edit = new Button("Изменить");
        Button delete = new Button("Удалить");
        Button back = new Button("Назад");

        add.setOnAction(e -> showEditor(null));

        edit.setOnAction(e -> {
            EventService.Event selected =
                    table.getSelectionModel().getSelectedItem();

            if (selected == null) {
                showAlert("Выберите событие для изменения");
                return;
            }

            showEditor(selected);
        });

        delete.setOnAction(e -> {
            EventService.Event selected =
                    table.getSelectionModel().getSelectedItem();

            if (selected == null) {
                showAlert("Выберите событие для удаления");
                return;
            }

            Alert confirmation = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Удалить событие «" + selected.title() + "»?",
                    ButtonType.YES,
                    ButtonType.NO
            );

            confirmation.showAndWait().ifPresent(result -> {
                if (result == ButtonType.YES) {
                    try {
                        service.deleteEvent(userId, selected.id());
                        refresh();
                    } catch (Exception ex) {
                        showAlert("Ошибка удаления: " + ex.getMessage());
                    }
                }
            });
        });

        back.setOnAction(e ->
                new CalendarView(stage, userId).show()
        );

        HBox buttons = new HBox(
                10, add, edit, delete, back
        );
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(
                15, title, filters, table, buttons
        );

        root.setPadding(new Insets(20));

        VBox.setVgrow(
                table,
                javafx.scene.layout.Priority.ALWAYS
        );

        stage.setTitle("Электронный календарь — события");
        stage.setScene(new Scene(root, 1050, 650));

        refresh();
    }

    private void refresh() {
        try {
            String query = searchField.getText()
                    .trim()
                    .toLowerCase(java.util.Locale.ROOT);

            var filtered = service.getEvents(userId)
                    .stream()

                    .filter(event -> {
                        String selectedCategory =
                                categoryFilter.getValue();

                        return selectedCategory == null
                                || selectedCategory.equals("Все")
                                || selectedCategory.equals(
                                event.category()
                        );
                    })

                    .filter(event -> {
                        String title = event.title() == null
                                ? "" : event.title();

                        String description =
                                event.description() == null
                                        ? "" : event.description();

                        String notes = event.notes() == null
                                ? "" : event.notes();

                        String combined = (
                                title + " " +
                                        description + " " +
                                        notes
                        ).toLowerCase(java.util.Locale.ROOT);

                        return combined.contains(query);
                    })

                    .filter(event -> {
                        LocalDate start =
                                LocalDateTime.parse(
                                        event.startTime()
                                ).toLocalDate();

                        LocalDate end =
                                LocalDateTime.parse(
                                        event.endTime()
                                ).toLocalDate();

                        return (
                                dateFrom.getValue() == null
                                        || !end.isBefore(
                                        dateFrom.getValue()
                                )
                        ) && (
                                dateTo.getValue() == null
                                        || !start.isAfter(
                                        dateTo.getValue()
                                )
                        );
                    })

                    .toList();

            table.setItems(
                    FXCollections.observableArrayList(filtered)
            );

        } catch (Exception e) {
            showAlert("Ошибка загрузки событий: " + e.getMessage());
        }
    }

    private void showEditor(EventService.Event event) {

        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setTitle(
                event == null
                        ? "Добавление события"
                        : "Редактирование события"
        );

        TextField name = new TextField();
        TextArea description = new TextArea();
        TextArea notes = new TextArea();

        DatePicker startDate = new DatePicker();
        DatePicker endDate = new DatePicker();

        TextField startTime = new TextField();
        TextField endTime = new TextField();

        ComboBox<String> category = new ComboBox<>();

        category.getItems().setAll(
                "Личное", "Работа", "Учёба", "Другое"
        );

        category.setValue("Личное");

        description.setPrefRowCount(3);
        notes.setPrefRowCount(3);

        startTime.setPromptText("Например: 14:30");
        endTime.setPromptText("Например: 15:30");

        if (event == null) {

            LocalDate today = LocalDate.now();

            startDate.setValue(today);
            endDate.setValue(today);

            startTime.setText("12:00");
            endTime.setText("13:00");

        } else {

            name.setText(event.title());

            description.setText(
                    event.description() == null
                            ? "" : event.description()
            );

            notes.setText(
                    event.notes() == null
                            ? "" : event.notes()
            );

            LocalDateTime start =
                    LocalDateTime.parse(event.startTime());

            LocalDateTime end =
                    LocalDateTime.parse(event.endTime());

            startDate.setValue(start.toLocalDate());
            endDate.setValue(end.toLocalDate());

            startTime.setText(
                    start.toLocalTime().format(TIME_FORMAT)
            );

            endTime.setText(
                    end.toLocalTime().format(TIME_FORMAT)
            );

            category.setValue(
                    event.category() == null
                            ? "Личное"
                            : event.category()
            );
        }

        javafx.scene.layout.GridPane grid =
                new javafx.scene.layout.GridPane();

        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        grid.addRow(0, new Label("Название:"), name);
        grid.addRow(1, new Label("Описание:"), description);
        grid.addRow(2, new Label("Заметки:"), notes);
        grid.addRow(3, new Label("Дата начала:"), startDate);
        grid.addRow(4, new Label("Время начала:"), startTime);
        grid.addRow(5, new Label("Дата окончания:"), endDate);
        grid.addRow(6, new Label("Время окончания:"), endTime);
        grid.addRow(7, new Label("Категория:"), category);

        dialog.getDialogPane().setContent(grid);

        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.OK,
                ButtonType.CANCEL
        );

        // Проверяем данные до закрытия диалога.
        Button saveButton = (Button) dialog
                .getDialogPane()
                .lookupButton(ButtonType.OK);

        saveButton.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                action -> {
                    try {
                        if (startDate.getValue() == null
                                || endDate.getValue() == null) {
                            throw new IllegalArgumentException(
                                    "Укажите даты события"
                            );
                        }

                        String start = LocalDateTime.of(
                                startDate.getValue(),
                                LocalTime.parse(
                                        startTime.getText().trim()
                                )
                        ).toString();

                        String end = LocalDateTime.of(
                                endDate.getValue(),
                                LocalTime.parse(
                                        endTime.getText().trim()
                                )
                        ).toString();

                        if (event == null) {

                            service.addEventWithCategory(
                                    userId,
                                    name.getText(),
                                    description.getText(),
                                    notes.getText(),
                                    start,
                                    end,
                                    category.getValue()
                            );

                        } else {

                            service.updateEvent(
                                    userId,
                                    event.id(),
                                    name.getText(),
                                    description.getText(),
                                    notes.getText(),
                                    start,
                                    end
                            );

                            service.setCategory(
                                    userId,
                                    event.id(),
                                    category.getValue()
                            );
                        }

                        refresh();

                    } catch (Exception ex) {
                        action.consume();

                        showAlert(
                                "Ошибка сохранения: " + ex.getMessage()
                        );
                    }
                }
        );

        dialog.showAndWait();
    }

    private void showAlert(String message) {
        new Alert(
                Alert.AlertType.INFORMATION,
                message
        ).showAndWait();
    }
}
