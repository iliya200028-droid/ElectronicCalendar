
package ru.sudachen.calendar.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import ru.sudachen.calendar.service.EventService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CalendarGridView {

    private final int userId;
    private final EventService eventService = new EventService();

    private LocalDate selectedDate = LocalDate.now();
    private String mode = "Месяц";

    private final BorderPane root = new BorderPane();

    public CalendarGridView(int userId) {
        this.userId = userId;
        refresh();
    }

    public BorderPane getView() {
        return root;
    }

    private void refresh() {
        Button previous = new Button("◀");
        Button next = new Button("▶");
        Button today = new Button("Сегодня");

        ComboBox<String> modeBox = new ComboBox<>();
        modeBox.getItems().addAll("День", "Неделя", "Месяц");
        modeBox.setValue(mode);

        Label dateLabel = new Label(
                selectedDate.format(
                        DateTimeFormatter.ofPattern("dd.MM.yyyy")
                )
        );

        dateLabel.setStyle(
                "-fx-font-size: 18px; -fx-font-weight: bold;"
        );

        previous.setOnAction(e -> {
            selectedDate = switch (mode) {
                case "День" -> selectedDate.minusDays(1);
                case "Неделя" -> selectedDate.minusWeeks(1);
                default -> selectedDate.minusMonths(1);
            };
            refresh();
        });

        next.setOnAction(e -> {
            selectedDate = switch (mode) {
                case "День" -> selectedDate.plusDays(1);
                case "Неделя" -> selectedDate.plusWeeks(1);
                default -> selectedDate.plusMonths(1);
            };
            refresh();
        });

        today.setOnAction(e -> {
            selectedDate = LocalDate.now();
            refresh();
        });

        modeBox.setOnAction(e -> {
            mode = modeBox.getValue();
            refresh();
        });

        HBox navigation = new HBox(
                12, previous, dateLabel, next, today,
                new Label("Режим:"), modeBox
        );
        navigation.setAlignment(Pos.CENTER);
        navigation.setPadding(new Insets(15));

        root.setTop(navigation);

        try {
            List<EventService.Event> events =
                    eventService.getEvents(userId);

            switch (mode) {
                case "День" -> showDay(events);
                case "Неделя" -> showWeek(events);
                default -> showMonth(events);
            }

        } catch (Exception e) {
            root.setCenter(new Label(
                    "Ошибка загрузки календаря: " + e.getMessage()
            ));
        }
    }

    private void showMonth(List<EventService.Event> events) {
        YearMonth month = YearMonth.from(selectedDate);

        GridPane grid = new GridPane();
        grid.setHgap(4);
        grid.setVgap(4);
        grid.setPadding(new Insets(10));

        String[] days = {
                "Пн", "Вт", "Ср", "Чт",
                "Пт", "Сб", "Вс"
        };

        for (int i = 0; i < 7; i++) {
            Label label = new Label(days[i]);
            label.setStyle("-fx-font-weight: bold;");
            grid.add(label, i, 0);
        }

        int offset = month.atDay(1)
                .getDayOfWeek().getValue() - 1;

        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            LocalDate date = month.atDay(day);

            VBox cell = createDayCell(date, events);
            int index = offset + day - 1;

            grid.add(
                    cell,
                    index % 7,
                    index / 7 + 1
            );
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        root.setCenter(scroll);
    }

    private void showWeek(List<EventService.Event> events) {
        LocalDate monday = selectedDate.minusDays(
                selectedDate.getDayOfWeek().getValue() - 1
        );

        HBox week = new HBox(8);
        week.setPadding(new Insets(15));

        for (int i = 0; i < 7; i++) {
            VBox cell = createDayCell(
                    monday.plusDays(i), events
            );
            HBox.setHgrow(cell, Priority.ALWAYS);
            week.getChildren().add(cell);
        }

        root.setCenter(new ScrollPane(week));
    }

    private void showDay(List<EventService.Event> events) {
        VBox day = createDayCell(selectedDate, events);
        day.setMinWidth(500);
        root.setCenter(new ScrollPane(day));
    }

    private VBox createDayCell(
            LocalDate date,
            List<EventService.Event> events) {

        Label dayLabel = new Label(
                date.format(
                        DateTimeFormatter.ofPattern("dd.MM")
                )
        );
        dayLabel.setStyle("-fx-font-weight: bold;");

        VBox cell = new VBox(5, dayLabel);
        cell.setPadding(new Insets(8));
        cell.setMinWidth(115);
        cell.setMinHeight(95);

        String background = date.equals(LocalDate.now())
                ? "#DCEBFF"
                : "#FFFFFF";

        cell.setStyle(
                "-fx-background-color: " + background + ";" +
                        "-fx-border-color: #CCCCCC;" +
                        "-fx-border-radius: 5;"
        );

        for (EventService.Event event : events) {
            LocalDate start = LocalDateTime.parse(
                    event.startTime()
            ).toLocalDate();

            LocalDate end = LocalDateTime.parse(
                    event.endTime()
            ).toLocalDate();

            if (!date.isBefore(start)
                    && !date.isAfter(end)) {

                Label eventLabel = new Label(
                        "• " + event.title()
                );

                eventLabel.setWrapText(true);
                eventLabel.setStyle(
                        "-fx-text-fill: #1769AA;" +
                                "-fx-font-size: 11px;"
                );

                cell.getChildren().add(eventLabel);
            }
        }

        return cell;
    }
}
