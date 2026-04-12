package com.example.EduManager.domain.feedback.dto;

import com.example.EduManager.domain.feedback.entity.Feedback;
import com.example.EduManager.domain.feedback.entity.FeedbackCategory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class FeedbackResponse {

    private final Long id;
    private final FeedbackCategory category;
    private final LocalDate date;
    private final String content;
    private final boolean studentVisible;
    private final boolean parentVisible;
    private final Long authorId;
    private final String authorName;

    @Builder
    private FeedbackResponse(Long id, FeedbackCategory category, LocalDate date, String content,
                              boolean studentVisible, boolean parentVisible, Long authorId, String authorName) {
        this.id = id;
        this.category = category;
        this.date = date;
        this.content = content;
        this.studentVisible = studentVisible;
        this.parentVisible = parentVisible;
        this.authorId = authorId;
        this.authorName = authorName;
    }

    public static FeedbackResponse of(Feedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .category(feedback.getCategory())
                .date(feedback.getDate())
                .content(feedback.getContent())
                .studentVisible(feedback.isStudentVisible())
                .parentVisible(feedback.isParentVisible())
                .authorId(feedback.getTeacher().getId())
                .authorName(feedback.getTeacher().getName())
                .build();
    }
}
