package com.example.EduManager.domain.feedback.dto;

import lombok.Getter;

@Getter
public class UpdateFeedbackVisibilityRequest {

    private boolean studentVisible;
    private boolean parentVisible;

    public static UpdateFeedbackVisibilityRequest of(boolean studentVisible, boolean parentVisible) {
        UpdateFeedbackVisibilityRequest request = new UpdateFeedbackVisibilityRequest();
        request.studentVisible = studentVisible;
        request.parentVisible = parentVisible;
        return request;
    }
}
