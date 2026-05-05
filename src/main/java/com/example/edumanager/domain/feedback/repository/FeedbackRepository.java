package com.example.edumanager.domain.feedback.repository;

import com.example.edumanager.domain.feedback.entity.Feedback;
import com.example.edumanager.domain.feedback.entity.FeedbackCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    @Query("SELECT f FROM Feedback f JOIN FETCH f.teacher t JOIN FETCH t.user WHERE f.student.id = :studentId")
    List<Feedback> findAllByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT f FROM Feedback f JOIN FETCH f.teacher t JOIN FETCH t.user WHERE f.student.id = :studentId AND f.category = :category")
    List<Feedback> findAllByStudentIdAndCategory(@Param("studentId") Long studentId, @Param("category") FeedbackCategory category);

    @Query("SELECT f FROM Feedback f JOIN FETCH f.teacher t JOIN FETCH t.user WHERE f.student.id = :studentId AND f.studentVisible = true")
    List<Feedback> findAllByStudentIdAndStudentVisibleTrue(@Param("studentId") Long studentId);

    @Query("SELECT f FROM Feedback f JOIN FETCH f.teacher t JOIN FETCH t.user WHERE f.student.id = :studentId AND f.studentVisible = true AND f.category = :category")
    List<Feedback> findAllByStudentIdAndStudentVisibleTrueAndCategory(@Param("studentId") Long studentId, @Param("category") FeedbackCategory category);

    @Query("SELECT f FROM Feedback f JOIN FETCH f.teacher t JOIN FETCH t.user WHERE f.student.id = :studentId AND f.parentVisible = true")
    List<Feedback> findAllByStudentIdAndParentVisibleTrue(@Param("studentId") Long studentId);

    @Query("SELECT f FROM Feedback f JOIN FETCH f.teacher t JOIN FETCH t.user WHERE f.student.id = :studentId AND f.parentVisible = true AND f.category = :category")
    List<Feedback> findAllByStudentIdAndParentVisibleTrueAndCategory(@Param("studentId") Long studentId, @Param("category") FeedbackCategory category);

    @Query("SELECT f FROM Feedback f JOIN FETCH f.teacher t JOIN FETCH t.user WHERE f.id = :feedbackId AND f.student.id = :studentId")
    Optional<Feedback> findByIdAndStudentId(@Param("feedbackId") Long feedbackId, @Param("studentId") Long studentId);
}
