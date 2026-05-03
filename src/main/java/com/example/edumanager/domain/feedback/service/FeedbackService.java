package com.example.edumanager.domain.feedback.service;

import com.example.edumanager.domain.feedback.dto.CreateFeedbackRequest;
import com.example.edumanager.domain.feedback.dto.UpdateFeedbackRequest;
import com.example.edumanager.domain.feedback.dto.UpdateFeedbackVisibilityRequest;
import com.example.edumanager.domain.feedback.entity.Feedback;
import com.example.edumanager.domain.feedback.entity.FeedbackCategory;
import com.example.edumanager.domain.feedback.repository.FeedbackRepository;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.teacher.entity.TeacherProfile;
import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    public List<Feedback> findAll(Long studentId) {
        return feedbackRepository.findAllByStudentId(studentId);
    }

    public List<Feedback> findAllByCategory(Long studentId, FeedbackCategory category) {
        return feedbackRepository.findAllByStudentIdAndCategory(studentId, category);
    }

    public List<Feedback> findStudentVisible(Long studentId) {
        return feedbackRepository.findAllByStudentIdAndStudentVisibleTrue(studentId);
    }

    public List<Feedback> findStudentVisibleByCategory(Long studentId, FeedbackCategory category) {
        return feedbackRepository.findAllByStudentIdAndStudentVisibleTrueAndCategory(studentId, category);
    }

    public List<Feedback> findParentVisible(Long studentId) {
        return feedbackRepository.findAllByStudentIdAndParentVisibleTrue(studentId);
    }

    public List<Feedback> findParentVisibleByCategory(Long studentId, FeedbackCategory category) {
        return feedbackRepository.findAllByStudentIdAndParentVisibleTrueAndCategory(studentId, category);
    }

    public Feedback save(StudentProfile student, TeacherProfile teacher, CreateFeedbackRequest request) {
        return feedbackRepository.save(
                Feedback.of(student, teacher, request.getCategory(), request.getDate(),
                        request.getContent(), request.isStudentVisible(), request.isParentVisible())
        );
    }

    public Feedback getByIdAndStudentId(Long feedbackId, Long studentId) {
        return feedbackRepository.findByIdAndStudentId(feedbackId, studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.FEEDBACK_NOT_FOUND));
    }

    public Feedback update(Feedback feedback, UpdateFeedbackRequest request) {
        feedback.update(request.getCategory(), request.getDate(), request.getContent(),
                request.isStudentVisible(), request.isParentVisible());
        return feedback;
    }

    public Feedback updateVisibility(Feedback feedback, UpdateFeedbackVisibilityRequest request) {
        feedback.updateVisibility(request.isStudentVisible(), request.isParentVisible());
        return feedback;
    }

    public void delete(Feedback feedback) {
        feedbackRepository.delete(feedback);
    }
}
