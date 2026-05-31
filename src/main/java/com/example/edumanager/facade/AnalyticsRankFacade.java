package com.example.edumanager.facade;

import com.example.edumanager.domain.analytics.dto.StudentRankResponse;
import com.example.edumanager.domain.analytics.service.AnalyticsService;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.student.service.StudentService;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.domain.user.service.UserService;
import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import com.example.edumanager.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 학생 석차 조회 — 존재 검증 + 역할별 접근제어(본인/연결 학부모/교사) 후 분석 결과를 반환한다.
 */
@Component
@RequiredArgsConstructor
public class AnalyticsRankFacade {

    private final AnalyticsService analyticsService;
    private final StudentService studentService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public StudentRankResponse getStudentRanks(Long studentId, String semester, UserDetailsImpl userDetails) {
        studentService.getById(studentId);
        verifyAccess(studentId, userDetails);
        return analyticsService.getStudentRanks(studentId, semester);
    }

    private void verifyAccess(Long studentId, UserDetailsImpl userDetails) {
        switch (userDetails.getRole()) {
            case TEACHER -> { /* 교사는 전체 열람 가능 */ }
            case STUDENT -> verifyStudentSelf(userDetails.getUserId(), studentId);
            case PARENT -> verifyParentChild(userDetails.getUserId(), studentId);
        }
    }

    private void verifyStudentSelf(Long userId, Long studentId) {
        User user = userService.getById(userId);
        StudentProfile profile = studentService.getProfileByUser(user);
        if (!profile.getId().equals(studentId)) {
            throw new CustomException(ErrorCode.STUDENT_ACCESS_DENIED);
        }
    }

    private void verifyParentChild(Long userId, Long studentId) {
        boolean isLinked = studentService.getParentsByStudentId(studentId)
                .stream().anyMatch(parent -> parent.getId().equals(userId));
        if (!isLinked) {
            throw new CustomException(ErrorCode.STUDENT_ACCESS_DENIED);
        }
    }
}
