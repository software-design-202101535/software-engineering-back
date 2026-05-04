package com.example.edumanager.domain.grade.repository;

import com.example.edumanager.domain.grade.entity.ExamType;
import com.example.edumanager.domain.grade.entity.Grade;
import com.example.edumanager.domain.grade.entity.Subject;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.domain.user.entity.School;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.support.AbstractRepositoryIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GradeRepository 통합 테스트")
class GradeRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired GradeRepository gradeRepository;
    @Autowired EntityManager em;

    @Nested
    @DisplayName("1. findExistingSubjects")
    class FindExistingSubjects {

        @Test
        @DisplayName("TC-G-1. 저장된 subject 중 조회 subject와 겹치는 것만 반환")
        void returnsOnlyMatchingSubjects() {
            StudentProfile student = persistStudent("s@test.com", 1);
            persistGrade(student, "1-1", Subject.KOREAN, ExamType.MIDTERM, 90);
            persistGrade(student, "1-1", Subject.MATH, ExamType.MIDTERM, 80);
            em.flush();
            em.clear();

            List<Subject> result = gradeRepository.findExistingSubjects(
                    student, "1-1", ExamType.MIDTERM, List.of(Subject.KOREAN, Subject.ENGLISH));

            assertThat(result).containsExactly(Subject.KOREAN);
        }
    }

    @Nested
    @DisplayName("2. findConflictingSubjects")
    class FindConflictingSubjects {

        @Test
        @DisplayName("TC-G-2. excludeIds에 있는 grade는 충돌에서 제외 (자기 자신 update 시나리오)")
        void excludesGradesByExcludeIds() {
            StudentProfile student = persistStudent("s@test.com", 1);
            Grade koreanGrade = persistGrade(student, "1-1", Subject.KOREAN, ExamType.MIDTERM, 90);
            persistGrade(student, "1-1", Subject.MATH, ExamType.MIDTERM, 80);
            em.flush();
            em.clear();

            List<Subject> result = gradeRepository.findConflictingSubjects(
                    student, "1-1", ExamType.MIDTERM,
                    List.of(Subject.KOREAN, Subject.MATH),
                    List.of(koreanGrade.getId()));

            assertThat(result).containsExactly(Subject.MATH);
        }

        @Test
        @DisplayName("TC-G-3. excludeIds 빈 리스트 → 모든 매칭 subject 반환 (NOT IN () SQL이 깨지지 않음)")
        void handlesEmptyExcludeIds() {
            StudentProfile student = persistStudent("s@test.com", 1);
            persistGrade(student, "1-1", Subject.KOREAN, ExamType.MIDTERM, 90);
            persistGrade(student, "1-1", Subject.MATH, ExamType.MIDTERM, 80);
            em.flush();
            em.clear();

            List<Subject> result = gradeRepository.findConflictingSubjects(
                    student, "1-1", ExamType.MIDTERM,
                    List.of(Subject.KOREAN, Subject.MATH),
                    List.of());

            assertThat(result)
                    .as("excludeIds가 빈 리스트일 때 매칭되는 모든 subject가 conflict")
                    .containsExactlyInAnyOrder(Subject.KOREAN, Subject.MATH);
        }
    }

    @Nested
    @DisplayName("3. findAllByStudentAndSemesterAndExamType")
    class FindAllByStudentAndSemesterAndExamType {

        @Test
        @DisplayName("TC-G-4. student/semester/examType 모두 일치하는 grade만 반환")
        void filtersByAllThreeKeys() {
            StudentProfile s1 = persistStudent("s1@test.com", 1);
            StudentProfile s2 = persistStudent("s2@test.com", 2);
            persistGrade(s1, "1-1", Subject.KOREAN, ExamType.MIDTERM, 90);   // 매치
            persistGrade(s1, "1-1", Subject.MATH,   ExamType.MIDTERM, 80);   // 매치
            persistGrade(s1, "1-2", Subject.KOREAN, ExamType.MIDTERM, 70);   // semester 다름
            persistGrade(s1, "1-1", Subject.KOREAN, ExamType.FINAL,   60);   // examType 다름
            persistGrade(s2, "1-1", Subject.KOREAN, ExamType.MIDTERM, 50);   // student 다름
            em.flush();
            em.clear();

            List<Grade> result = gradeRepository.findAllByStudentAndSemesterAndExamType(s1, "1-1", ExamType.MIDTERM);

            assertThat(result).extracting(Grade::getSubject)
                    .containsExactlyInAnyOrder(Subject.KOREAN, Subject.MATH);
        }
    }

    @Nested
    @DisplayName("4. 유니크 제약 (student_id, semester, subject, exam_type)")
    class UniqueConstraint {

        @Test
        @DisplayName("TC-G-5. 동일 student+semester+subject+examType 중복 INSERT는 uq_grades 위반")
        void rejectsDuplicate() {
            StudentProfile student = persistStudent("s@test.com", 1);
            gradeRepository.saveAndFlush(Grade.of(student, "1-1", Subject.KOREAN, 90, ExamType.MIDTERM));

            assertThatThrownBy(() -> gradeRepository.saveAndFlush(
                    Grade.of(student, "1-1", Subject.KOREAN, 80, ExamType.MIDTERM)))
                    .as("uq_grades 제약 위반")
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    private StudentProfile persistStudent(String email, int number) {
        User user = User.of(email, "encoded", "student" + number, Role.STUDENT);
        em.persist(user);
        StudentProfile student = StudentProfile.of(user, School.SUNRIN_HIGH_SCHOOL, 1, 1, number);
        em.persist(student);
        return student;
    }

    private Grade persistGrade(StudentProfile student, String semester, Subject subject,
                                ExamType examType, int score) {
        Grade grade = Grade.of(student, semester, subject, score, examType);
        em.persist(grade);
        return grade;
    }
}
