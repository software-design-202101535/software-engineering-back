package com.example.edumanager.domain.counseling.dto;

import com.example.edumanager.domain.counseling.entity.Counseling;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class CounselingResponse {

    private final Long id;
    private final Long studentId;
    private final Long teacherId;
    private final String teacherName;
    private final LocalDate counselingDate;
    private final String content;
    private final String nextPlan;
    private final LocalDate nextDate;
    private final boolean sharedWithTeachers;
    private final LocalDateTime createdAt;

    @Builder
    private CounselingResponse(Long id, Long studentId, Long teacherId, String teacherName,
                                LocalDate counselingDate, String content, String nextPlan,
                                LocalDate nextDate, boolean sharedWithTeachers, LocalDateTime createdAt) {
        this.id = id;
        this.studentId = studentId;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.counselingDate = counselingDate;
        this.content = content;
        this.nextPlan = nextPlan;
        this.nextDate = nextDate;
        this.sharedWithTeachers = sharedWithTeachers;
        this.createdAt = createdAt;
    }

    public static CounselingResponse ofForTest(Long id) {
        return CounselingResponse.builder().id(id).build();
    }

    public static CounselingResponse of(Counseling counseling) {
        return CounselingResponse.builder()
                .id(counseling.getId())
                .studentId(counseling.getStudent().getId())
                .teacherId(counseling.getTeacher().getUser().getId())
                .teacherName(counseling.getTeacher().getUser().getName())
                .counselingDate(counseling.getDate())
                .content(counseling.getContent())
                .nextPlan(counseling.getNextPlan())
                .nextDate(counseling.getNextDate())
                .sharedWithTeachers(counseling.isSharedWithTeachers())
                .createdAt(counseling.getCreatedAt())
                .build();
    }
}
