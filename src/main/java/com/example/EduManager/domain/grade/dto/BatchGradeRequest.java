package com.example.EduManager.domain.grade.dto;

import com.example.EduManager.domain.grade.entity.ExamType;
import com.example.EduManager.domain.grade.entity.Subject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class BatchGradeRequest {

    @NotBlank(message = "학기를 입력해주세요.")
    private String semester;

    @NotNull(message = "시험 유형을 입력해주세요.")
    private ExamType examType;

    @Valid
    private List<CreateItem> create = List.of();

    @Valid
    private List<UpdateItem> update = List.of();

    private List<Long> delete = List.of();

    public static BatchGradeRequest of(String semester, ExamType examType,
                                       List<CreateItem> create, List<UpdateItem> update, List<Long> delete) {
        BatchGradeRequest req = new BatchGradeRequest();
        req.semester = semester;
        req.examType = examType;
        req.create = create;
        req.update = update;
        req.delete = delete;
        return req;
    }

    @Getter
    public static class CreateItem {

        @NotNull(message = "과목을 입력해주세요.")
        private Subject subject;

        @Min(value = 0, message = "점수는 0 이상이어야 합니다.")
        @Max(value = 100, message = "점수는 100 이하여야 합니다.")
        private Integer score;

        public static CreateItem of(Subject subject, Integer score) {
            CreateItem item = new CreateItem();
            item.subject = subject;
            item.score = score;
            return item;
        }
    }

    @Getter
    public static class UpdateItem {

        @NotNull(message = "성적 id를 입력해주세요.")
        private Long id;

        @NotNull(message = "과목을 입력해주세요.")
        private Subject subject;

        @Min(value = 0, message = "점수는 0 이상이어야 합니다.")
        @Max(value = 100, message = "점수는 100 이하여야 합니다.")
        private Integer score;

        public static UpdateItem of(Long id, Subject subject, Integer score) {
            UpdateItem item = new UpdateItem();
            item.id = id;
            item.subject = subject;
            item.score = score;
            return item;
        }
    }
}
