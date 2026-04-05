package com.example.EduManager.domain.student.dto;

import com.example.EduManager.domain.student.entity.NoteCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UpdateNoteRequest {

    @NotNull(message = "카테고리를 입력해주세요.")
    private NoteCategory category;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;

    @NotNull(message = "날짜를 입력해주세요.")
    private LocalDate date;

    public static UpdateNoteRequest of(NoteCategory category, String content, LocalDate date) {
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.category = category;
        request.content = content;
        request.date = date;
        return request;
    }
}
