
package ru.sudachen.calendar.service;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Alert;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class ReminderService {

    private final EventService eventService =
            new EventService();

    private final Set<Integer> notifiedEvents =
            new HashSet<>();

    private Timeline timeline;
    private int activeUserId = -1;

    public void start(int userId) {

        // Не запускаем напоминания повторно
        // для того же пользователя.
        if (timeline != null && activeUserId == userId) {
            return;
        }

        stop();

        activeUserId = userId;

        timeline = new Timeline(
                new KeyFrame(
                        Duration.seconds(30),
                        e -> checkEvents(userId)
                )
        );

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // Проверяем события сразу после входа.
        checkEvents(userId);
    }

    private void checkEvents(int userId) {

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime limit = now.plusMinutes(15);

            for (EventService.Event event :
                    eventService.getEvents(userId)) {

                LocalDateTime start =
                        LocalDateTime.parse(
                                event.startTime()
                        );

                boolean upcoming =
                        !start.isBefore(now)
                                && !start.isAfter(limit);

                if (upcoming
                        && notifiedEvents.add(event.id())) {

                    Alert alert = new Alert(
                            Alert.AlertType.INFORMATION
                    );

                    alert.setTitle("Напоминание");
                    alert.setHeaderText(
                            "Скоро начнётся событие!"
                    );

                    alert.setContentText(
                            event.title()
                                    + "\nНачало: "
                                    + start
                    );

                    alert.show();
                }
            }

        } catch (Exception e) {
            System.err.println(
                    "Reminder error: " + e.getMessage()
            );
        }
    }

    public void stop() {

        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }

        activeUserId = -1;
        notifiedEvents.clear();
    }
}
