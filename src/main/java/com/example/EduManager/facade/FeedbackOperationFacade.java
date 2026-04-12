package com.example.EduManager.facade;

import com.example.EduManager.domain.feedback.dto.CreateFeedbackRequest;
import com.example.EduManager.domain.feedback.dto.FeedbackResponse;
import com.example.EduManager.domain.feedback.dto.UpdateFeedbackRequest;
import com.example.EduManager.domain.feedback.dto.UpdateFeedbackVisibilityRequest;
import com.example.EduManager.domain.feedback.entity.Feedback;
import com.example.EduManager.domain.feedback.entity.FeedbackCategory;
import com.example.EduManager.domain.feedback.service.FeedbackService;
import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.student.service.StudentService;
import com.example.EduManager.domain.user.entity.Role;
import com.example.EduManager.domain.user.entity.User;
import com.example.EduManager.domain.user.service.UserService;
import com.example.EduManager.global.exception.CustomException;
import com.example.EduManager.global.exception.ErrorCode;
import com.example.EduManager.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FeedbackOperationFacade {

    private final FeedbackService feedbackService;
    private final StudentService studentService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getList(Long studentId, FeedbackCategory category, UserDetailsImpl userDetails) {
        studentService.getById(studentId);
        List<Feedback> feedbacks = switch (userDetails.getRole()) {
            case TEACHER, ADMIN -> feedbackService.findAll(studentId, category);
            case STUDENT -> {
                verifyStudentSelf(userDetails.getUserId(), studentId);
                yield feedbackService.findStudentVisible(studentId, category);
            }
            case PARENT -> {
                verifyParentChild(userDetails.getUserId(), studentId);
                yield feedbackService.findParentVisible(studentId, category);
            }
        };
        return feedbacks.stream().map(FeedbackResponse::of).toList();
    }

    @Transactional
    public FeedbackResponse create(Long studentId, CreateFeedbackRequest request, UserDetailsImpl userDetails) {
        checkTeacherOrAdmin(userDetails.getRole());
        StudentProfile student = studentService.getById(studentId);
        User teacher = userService.getById(userDetails.getUserId());
        return FeedbackResponse.of(feedbackService.save(student, teacher, request));
    }

    @Transactional
    public FeedbackResponse update(Long studentId, Long feedbackId, UpdateFeedbackRequest request, UserDetailsImpl userDetails) {
        checkTeacherOrAdmin(userDetails.getRole());
        studentService.getById(studentId);
        Feedback feedback = feedbackService.getByIdAndStudentId(feedbackId, studentId);
        checkAuthor(feedback, userDetails);
        return FeedbackResponse.of(feedbackService.update(feedback, request));
    }

    @Transactional
    public FeedbackResponse updateVisibility(Long studentId, Long feedbackId, UpdateFeedbackVisibilityRequest request, UserDetailsImpl userDetails) {
        checkTeacherOrAdmin(userDetails.getRole());
        studentService.getById(studentId);
        Feedback feedback = feedbackService.getByIdAndStudentId(feedbackId, studentId);
        checkAuthor(feedback, userDetails);
        return FeedbackResponse.of(feedbackService.updateVisibility(feedback, request));
    }

    @Transactional
    public void delete(Long studentId, Long feedbackId, UserDetailsImpl userDetails) {
        checkTeacherOrAdmin(userDetails.getRole());
        studentService.getById(studentId);
        Feedback feedback = feedbackService.getByIdAndStudentId(feedbackId, studentId);
        checkAuthor(feedback, userDetails);
        feedbackService.delete(feedback);
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
        if (!isLinked) throw new CustomException(ErrorCode.STUDENT_ACCESS_DENIED);
    }

    private void checkTeacherOrAdmin(Role role) {
        if (role != Role.TEACHER && role != Role.ADMIN) {
            throw new CustomException(ErrorCode.STUDENT_ACCESS_DENIED);
        }
    }

    private void checkAuthor(Feedback feedback, UserDetailsImpl userDetails) {
        if (userDetails.getRole() == Role.ADMIN) return;
        if (!feedback.getTeacher().getId().equals(userDetails.getUserId())) {
            throw new CustomException(ErrorCode.FEEDBACK_ACCESS_DENIED);
        }
    }
}
