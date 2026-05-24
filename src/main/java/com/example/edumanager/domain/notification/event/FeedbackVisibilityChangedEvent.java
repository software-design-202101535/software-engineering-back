package com.example.edumanager.domain.notification.event;

import lombok.Getter;

@Getter
public class FeedbackVisibilityChangedEvent {

    private final Long feedbackId;
    private final Long studentId;
    private final boolean newlyVisibleToStudent;
    private final boolean newlyVisibleToParent;
    private final String categoryName;

    private FeedbackVisibilityChangedEvent(Long feedbackId, Long studentId,
                                           boolean newlyVisibleToStudent, boolean newlyVisibleToParent,
                                           String categoryName) {
        this.feedbackId = feedbackId;
        this.studentId = studentId;
        this.newlyVisibleToStudent = newlyVisibleToStudent;
        this.newlyVisibleToParent = newlyVisibleToParent;
        this.categoryName = categoryName;
    }

    public static FeedbackVisibilityChangedEvent of(Long feedbackId, Long studentId,
                                                    boolean newlyVisibleToStudent, boolean newlyVisibleToParent,
                                                    String categoryName) {
        return new FeedbackVisibilityChangedEvent(feedbackId, studentId,
                newlyVisibleToStudent, newlyVisibleToParent, categoryName);
    }

    public boolean hasRecipient() {
        return newlyVisibleToStudent || newlyVisibleToParent;
    }
}
