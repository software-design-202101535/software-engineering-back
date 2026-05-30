package com.example.edumanager.domain.notification.event;

import lombok.Getter;

@Getter
public class GradeBatchProcessedEvent {

    private final Long studentId;

    private GradeBatchProcessedEvent(Long studentId) {
        this.studentId = studentId;
    }

    public static GradeBatchProcessedEvent of(Long studentId) {
        return new GradeBatchProcessedEvent(studentId);
    }
}
