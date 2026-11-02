
package ru.sudachen.calendar.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceTest {

    @Test
    void completedTaskShouldHaveCompletedStatus() {

        TaskService.Task task = new TaskService.Task(
                1,
                "Подготовить диплом",
                "Описание",
                2,
                "2026-11-01",
                true,
                "Учёба"
        );

        assertEquals("Выполнена", task.getStatus());
    }

    @Test
    void OverdueTaskShouldBeDetected() {

        TaskService.Task task = new TaskService.Task(
                2,
                "Сдать отчёт",
                "Описание",
                1,
                "2020-01-01",
                false,
                "Работа"
        );

        assertTrue(task.isOverdue());
        assertEquals("Просрочена", task.getStatus());
    }

    @Test
    void futureTaskShouldNotBeOverdue() {

        TaskService.Task task = new TaskService.Task(
                3,
                "Будущая задача",
                "Описание",
                0,
                "2099-12-31",
                false,
                "Личное"
        );

        assertFalse(task.isOverdue());
        assertEquals("В работе", task.getStatus());
    }
}
