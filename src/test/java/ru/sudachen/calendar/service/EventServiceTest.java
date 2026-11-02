
package ru.sudachen.calendar.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventServiceTest {

    private final EventService service =
            new EventService();

    @Test
    void emptyEventTitleShouldBeRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.addEventWithCategory(
                        1,
                        "",
                        "Описание",
                        "Заметки",
                        "2026-12-01T10:00",
                        "2026-12-01T11:00",
                        "Учёба"
                )
        );
    }

    @Test
    void eventEndBeforeStartShouldBeRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.addEventWithCategory(
                        1,
                        "Занятие",
                        "Описание",
                        "",
                        "2026-12-01T15:00",
                        "2026-12-01T14:00",
                        "Учёба"
                )
        );
    }

    @Test
    void eventEndEqualToStartShouldBeRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.addEventWithCategory(
                        1,
                        "Встреча",
                        "",
                        "",
                        "2026-12-01T12:00",
                        "2026-12-01T12:00",
                        "Работа"
                )
        );
    }
}
