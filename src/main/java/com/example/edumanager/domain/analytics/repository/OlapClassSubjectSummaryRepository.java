package com.example.edumanager.domain.analytics.repository;

import com.example.edumanager.domain.analytics.entity.OlapClassSubjectSummary;
import com.example.edumanager.domain.grade.entity.Subject;
import com.example.edumanager.domain.user.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OlapClassSubjectSummaryRepository extends JpaRepository<OlapClassSubjectSummary, Long> {

    /**
     * 대시보드 조회. classNum 이 null 이면 학년 전체(class_num IS NULL) 행을, subject 가 null 이면 전 과목을 반환한다.
     */
    @Query("SELECT s FROM OlapClassSubjectSummary s " +
            "WHERE s.school = :school AND s.grade = :grade " +
            "AND ((:classNum IS NULL AND s.classNum IS NULL) OR s.classNum = :classNum) " +
            "AND s.semester = :semester " +
            "AND (:subject IS NULL OR s.subject = :subject) " +
            "ORDER BY s.subject ASC")
    List<OlapClassSubjectSummary> findForDashboard(@Param("school") School school,
                                                   @Param("grade") int grade,
                                                   @Param("classNum") Integer classNum,
                                                   @Param("semester") String semester,
                                                   @Param("subject") Subject subject);
}
