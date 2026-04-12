package com.example.EduManager.domain.counseling.dto;

import lombok.Getter;

@Getter
public class UpdateCounselingShareRequest {

    private boolean sharedWithTeachers;

    public static UpdateCounselingShareRequest of(boolean sharedWithTeachers) {
        UpdateCounselingShareRequest request = new UpdateCounselingShareRequest();
        request.sharedWithTeachers = sharedWithTeachers;
        return request;
    }
}
