package ru.sudachen.calendar.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.sudachen.calendar.service.TaskService;

import java.time.LocalDate;
import java.util.Locale;

public class TaskView {

    private final Stage stage;
    private final int userId;
    private final TaskService service = new TaskService();

    private final TableView<TaskService.Task> table = new TableView<>();

    private final TextField searchField = new TextField();
    private final ComboBox<String> statusFilter = new ComboBox<>();
    private final ComboBox<String> priorityFilter = new ComboBox<>();
    private final ComboBox<String> categoryFilter = new ComboBox<>();
    private final DatePicker deadlineFrom = new DatePicker();
    private final DatePicker deadlineTo = new DatePicker();

    public TaskView(Stage stage, int userId) {
        this.stage = stage;
        this.userId = userId;
    }

    public void show() {
        Label title = new Label("Мои задачи");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TableColumn<TaskService.Task, String> nameColumn =
                new TableColumn<>("Название");
        nameColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().title()));

        TableColumn<TaskService.Task, String> priorityColumn =
                new TableColumn<>("Приоритет");
        priorityColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getPriorityName()));

        TableColumn<TaskService.Task, String> deadlineColumn =
                new TableColumn<>("Срок");
        deadlineColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().deadline() == null
                                ? "—" : cell.getValue().deadline()));

        TableColumn<TaskService.Task, String> statusColumn =
                new TableColumn<>("Статус");
        statusColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getStatus()));

        TableColumn<TaskService.Task, String> categoryColumn =
                new TableColumn<>("Категория");
        categoryColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(categoryOf(cell.getValue())));

        table.getColumns().setAll(
                nameColumn, priorityColumn, deadlineColumn,
                statusColumn, categoryColumn
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Задачи не найдены"));

        searchField.setPromptText("Поиск задач...");
        searchField.setPrefWidth(190);

        statusFilter.getItems().setAll(
                "Все", "В работе", "Выполнена", "Просрочена"
        );
        priorityFilter.getItems().setAll(
                "Все", "Высокий", "Средний", "Низкий"
        );
        categoryFilter.getItems().setAll(
                "Все", "Личное", "Работа", "Учёба", "Другое"
        );
        statusFilter.setValue("Все");
        priorityFilter.setValue("Все");
        categoryFilter.setValue("Все");

        HBox filters = new HBox(
                10,
                new Label("Поиск:"), searchField,
                new Label("Статус:"), statusFilter,
                new Label("Приоритет:"), priorityFilter,
                new Label("Категория:"), categoryFilter
        );
        filters.setAlignment(Pos.CENTER_LEFT);

        deadlineFrom.setPromptText("Дата с");
        deadlineTo.setPromptText("Дата по");
        Button clearDates = new Button("Сбросить даты");
        clearDates.setOnAction(e -> {
            deadlineFrom.setValue(null);
            deadlineTo.setValue(null);
            refresh();
        });

        HBox dateFilters = new HBox(
                10,
                new Label("Срок выполнения:"),
                deadlineFrom, deadlineTo, clearDates
        );
        dateFilters.setAlignment(Pos.CENTER_LEFT);

        VBox filtersPanel = new VBox(8, filters, dateFilters);

        searchField.textProperty().addListener((obs, oldValue, newValue) -> refresh());
        statusFilter.setOnAction(e -> refresh());
        priorityFilter.setOnAction(e -> refresh());
        categoryFilter.setOnAction(e -> refresh());
        deadlineFrom.valueProperty().addListener((obs, oldValue, newValue) -> refresh());
        deadlineTo.valueProperty().addListener((obs, oldValue, newValue) -> refresh());

        Button add = new Button("Добавить");
        Button edit = new Button("Редактировать");
        Button changeStatus = new Button("Изменить статус");
        Button delete = new Button("Удалить");
        Button back = new Button("Назад");

        add.setOnAction(e -> showEditor(null));

        edit.setOnAction(e -> {
            TaskService.Task selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Сначала выберите задачу");
                return;
            }
            showEditor(selected);
        });

        changeStatus.setOnAction(e -> {
            TaskService.Task selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Сначала выберите задачу");
                return;
            }
            try {
                service.setCompleted(userId, selected.id(), !selected.completed());
                refresh();
            } catch (Exception ex) {
                showAlert("Не удалось изменить статус: " + ex.getMessage());
            }
        });

        delete.setOnAction(e -> {
            TaskService.Task selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Сначала выберите задачу");
                return;
            }

            Alert confirmation = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Удалить задачу «" + selected.title() + "»?",
                    ButtonType.YES,
                    ButtonType.NO
            );
            confirmation.showAndWait().ifPresent(answer -> {
                if (answer == ButtonType.YES) {
                    try {
                        service.deleteTask(userId, selected.id());
                        refresh();
                    } catch (Exception ex) {
                        showAlert("Не удалось удалить задачу: " + ex.getMessage());
                    }
                }
            });
        });

        back.setOnAction(e -> new CalendarView(stage, userId).show());

        HBox buttons = new HBox(10, add, edit, changeStatus, delete, back);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(15, title, filtersPanel, table, buttons);
        root.setPadding(new Insets(20));
        VBox.setVgrow(table, Priority.ALWAYS);

        stage.setTitle("Электронный календарь — задачи");
        stage.setScene(new Scene(root, 1120, 670));
        stage.show();
        refresh();
    }

    private void refresh() {
        try {
            String query = searchField.getText()
                    .trim().toLowerCase(Locale.ROOT);

            var filteredTasks = service.getTasks(userId).stream()
                    .filter(task -> {
                        String title = task.title() == null ? "" : task.title();
                        String description = task.description() == null
                                ? "" : task.description();
                        return (title + " " + description)
                                .toLowerCase(Locale.ROOT).contains(query);
                    })
                    .filter(task -> "Все".equals(statusFilter.getValue())
                            || statusFilter.getValue() == null
                            || task.getStatus().equals(statusFilter.getValue()))
                    .filter(task -> "Все".equals(priorityFilter.getValue())
                            || priorityFilter.getValue() == null
                            || task.getPriorityName().equals(priorityFilter.getValue()))
                    .filter(task -> "Все".equals(categoryFilter.getValue())
                            || categoryFilter.getValue() == null
                            || categoryOf(task).equals(categoryFilter.getValue()))
                    .filter(task -> {
                        LocalDate from = deadlineFrom.getValue();
                        LocalDate to = deadlineTo.getValue();
                        if (from == null && to == null) {
                            return true;
                        }
                        String deadline = task.deadline();
                        if (deadline == null || deadline.isBlank()) {
                            return false;
                        }
                        LocalDate date = LocalDate.parse(deadline);
                        return (from == null || !date.isBefore(from))
                                && (to == null || !date.isAfter(to));
                    })
                    .toList();

            table.setItems(FXCollections.observableArrayList(filteredTasks));
        } catch (Exception ex) {
            showAlert("Не удалось загрузить задачи: " + ex.getMessage());
        }
    }

    private String categoryOf(TaskService.Task task) {
        return task.category() == null || task.category().isBlank()
                ? "Личное" : task.category();
    }

    private void showEditor(TaskService.Task task) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(task == null ? "Новая задача" : "Редактирование задачи");

        TextField name = new TextField();
        TextArea description = new TextArea();
        description.setPrefRowCount(3);

        ComboBox<String> priority = new ComboBox<>();
        priority.getItems().setAll("Низкий", "Средний", "Высокий");
        priority.setValue("Средний");

        DatePicker deadline = new DatePicker(LocalDate.now());

        ComboBox<String> category = new ComboBox<>();
        category.getItems().setAll("Личное", "Работа", "Учёба", "Другое");
        category.setValue("Личное");

        if (task != null) {
            name.setText(task.title());
            description.setText(task.description() == null ? "" : task.description());
            priority.setValue(task.getPriorityName());
            deadline.setValue(task.deadline() == null || task.deadline().isBlank()
                    ? null : LocalDate.parse(task.deadline()));
            category.setValue(categoryOf(task));
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Название:"), name);
        grid.addRow(1, new Label("Описание:"), description);
        grid.addRow(2, new Label("Приоритет:"), priority);
        grid.addRow(3, new Label("Срок выполнения:"), deadline);
        grid.addRow(4, new Label("Категория:"), category);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.OK, ButtonType.CANCEL
        );

        Button saveButton = (Button) dialog.getDialogPane()
                .lookupButton(ButtonType.OK);

        saveButton.addEventFilter(ActionEvent.ACTION, action -> {
            try {
                if (name.getText().isBlank()) {
                    throw new IllegalArgumentException("Укажите название задачи");
                }

                int priorityValue = switch (priority.getValue()) {
                    case "Высокий" -> 2;
                    case "Средний" -> 1;
                    case "Низкий" -> 0;
                    default -> throw new IllegalArgumentException("Выберите приоритет");
                };

                String deadlineValue = deadline.getValue() == null
                        ? null : deadline.getValue().toString();

                if (task == null) {
                    service.addTaskWithCategory(
                            userId,
                            name.getText(),
                            description.getText(),
                            priorityValue,
                            deadlineValue,
                            category.getValue()
                    );
                } else {
                    service.updateTask(
                            userId,
                            task.id(),
                            name.getText(),
                            description.getText(),
                            priorityValue,
                            deadlineValue
                    );
                    service.setCategory(userId, task.id(), category.getValue());
                }

                refresh();
            } catch (Exception ex) {
                action.consume();
                showAlert("Ошибка сохранения задачи: " + ex.getMessage());
            }
        });

        dialog.showAndWait();
    }

    private void showAlert(String message) {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }
}
