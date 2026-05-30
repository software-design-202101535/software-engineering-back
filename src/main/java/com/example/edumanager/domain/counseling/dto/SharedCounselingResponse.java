package com.example.edumanager.domain.counseling.dto;

import com.example.edumanager.domain.counseling.entity.Counseling;
import com.example.edumanager.domain.student.entity.StudentProfile;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class SharedCounselingResponse {

    private final Long id;
    private final Long studentId;
    private final String studentName;
    private final int grade;
    private final int classNum;
    private final int number;
    private final Long teacherId;
    private final String teacherName;
    private final LocalDate counselingDate;
    private final String content;
    private final String nextPlan;
    private final LocalDate nextDate;
    private final boolean sharedWithTeachers;
    private final LocalDateTime createdAt;

    @Builder
    private SharedCounselingResponse(Long id, Long studentId, String studentName, int grade, int classNum,
                                     int number, Long teacherId, String teacherName, LocalDate counselingDate,
                                     String content, String nextPlan, LocalDate nextDate,
                                     boolean sharedWithTeachers, LocalDateTime createdAt) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.grade = grade;
        this.classNum = classNum;
        this.number = number;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.counselingDate = counselingDate;
        this.content = content;
        this.nextPlan = nextPlan;
        this.nextDate = nextDate;
        this.sharedWithTeachers = sharedWithTeachers;
        this.createdAt = createdAt;
    }

    public static SharedCounselingResponse ofForTest(Long id) {
        return SharedCounselingResponse.builder().id(id).build();
    }

    public static SharedCounselingResponse of(Counseling counseling) {
        StudentProfile student = counseling.getStudent();
        return SharedCounselingResponse.builder()
                .id(counseling.getId())
                .studentId(student.getId())
                .studentName(student.getUser().getName())
                .grade(student.getGrade())
                .classNum(student.getClassNum())
                .number(student.getNumber())
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
