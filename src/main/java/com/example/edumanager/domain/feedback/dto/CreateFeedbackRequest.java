package com.example.edumanager.domain.feedback.dto;

import com.example.edumanager.domain.feedback.entity.FeedbackCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CreateFeedbackRequest {

    @NotNull(message = "카테고리를 입력해주세요.")
    private FeedbackCategory category;

    @NotNull(message = "날짜를 입력해주세요.")
    private LocalDate date;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;

    private boolean studentVisible;
    private boolean parentVisible;

    public static CreateFeedbackRequest of(FeedbackCategory category, LocalDate date, String content,
                                           boolean studentVisible, boolean parentVisible) {
        CreateFeedbackRequest request = new CreateFeedbackRequest();
        request.category = category;
        request.date = date;
        request.content = content;
        request.studentVisible = studentVisible;
        request.parentVisible = parentVisible;
        return request;
    }
}
