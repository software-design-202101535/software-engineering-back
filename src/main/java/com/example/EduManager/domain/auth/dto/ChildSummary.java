package com.example.EduManager.domain.auth.dto;

import com.example.EduManager.domain.student.entity.StudentProfile;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ChildSummary {

    private final Long studentId;
    private final String name;

    @Builder
    private ChildSummary(Long studentId, String name) {
        this.studentId = studentId;
        this.name = name;
    }

    public static ChildSummary of(StudentProfile profile) {
        return ChildSummary.builder()
                .studentId(profile.getId())
                .name(profile.getUser().getName())
                .build();
    }
}
