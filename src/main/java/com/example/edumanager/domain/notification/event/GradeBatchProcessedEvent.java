package com.example.edumanager.domain.notification.event;

import lombok.Getter;

@Getter
public class GradeBatchProcessedEvent {

    private final Long studentId;
    private final int count;

    private GradeBatchProcessedEvent(Long studentId, int count) {
        this.studentId = studentId;
        this.count = count;
    }

    public static GradeBatchProcessedEvent of(Long studentId, int count) {
        return new GradeBatchProcessedEvent(studentId, count);
    }
}
