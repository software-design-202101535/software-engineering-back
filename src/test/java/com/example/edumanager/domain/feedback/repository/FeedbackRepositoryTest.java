package com.example.edumanager.domain.feedback.repository;

import com.example.edumanager.domain.feedback.entity.Feedback;
import com.example.edumanager.domain.feedback.entity.FeedbackCategory;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.teacher.entity.TeacherProfile;
import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.domain.user.entity.School;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.support.AbstractRepositoryIntegrationTest;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FeedbackRepository 통합 테스트")
class FeedbackRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired FeedbackRepository feedbackRepository;
    @Autowired EntityManager em;

    @Nested
    @DisplayName("1. findAllByStudentId")
    class FindAllByStudentId {

        @Test
        @DisplayName("TC-F-1. 다른 student의 피드백은 제외하고 해당 student 것만 반환 + JOIN FETCH로 N+1 없음")
        void onlyReturnsTargetStudentFeedbacksAndNoNplusOne() {
            TeacherProfile teacher = persistTeacher("teacher@test.com");
            StudentProfile studentA = persistStudent("a@test.com", 1);
            StudentProfile studentB = persistStudent("b@test.com", 2);
            persistFeedback(studentA, teacher, FeedbackCategory.GRADE, true, true);
            persistFeedback(studentA, teacher, FeedbackCategory.BEHAVIOR, true, true);
            persistFeedback(studentB, teacher, FeedbackCategory.GRADE, true, true);
            em.flush();
            em.clear();

            Statistics stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
            stats.clear();

            List<Feedback> result = feedbackRepository.findAllByStudentId(studentA.getId());
            result.forEach(f -> f.getTeacher().getUser().getName()); // LAZY 강제 — JOIN FETCH면 추가쿼리 없음

            assertThat(result).hasSize(2);
            assertThat(result).allMatch(f -> f.getStudent().getId().equals(studentA.getId()));
            assertThat(stats.getPrepareStatementCount())
                    .as("JOIN FETCH 적용 → teacher.user 까지 단일 쿼리로 로딩")
                    .isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("2. findAllByStudentIdAndCategory")
    class FindAllByStudentIdAndCategory {

        @Test
        @DisplayName("TC-F-2. category가 일치하는 피드백만 반환 (다른 student 노이즈 제외)")
        void onlyReturnsCategoryMatched() {
            TeacherProfile teacher = persistTeacher("teacher@test.com");
            StudentProfile student = persistStudent("s@test.com", 1);
            StudentProfile other = persistStudent("o@test.com", 2);
            persistFeedback(student, teacher, FeedbackCategory.GRADE, true, true);
            persistFeedback(student, teacher, FeedbackCategory.BEHAVIOR, true, true);
            persistFeedback(other, teacher, FeedbackCategory.GRADE, true, true); // 노이즈
            em.flush();
            em.clear();

            List<Feedback> result = feedbackRepository.findAllByStudentIdAndCategory(
                    student.getId(), FeedbackCategory.GRADE);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCategory()).isEqualTo(FeedbackCategory.GRADE);
            assertThat(result.get(0).getStudent().getId()).isEqualTo(student.getId());
        }
    }

    @Nested
    @DisplayName("3. findAllByStudentIdAndStudentVisibleTrue")
    class FindAllByStudentIdAndStudentVisibleTrue {

        @Test
        @DisplayName("TC-F-3. studentVisible=true인 피드백만 반환 (다른 student의 visible=true 노이즈 제외)")
        void onlyReturnsStudentVisible() {
            TeacherProfile teacher = persistTeacher("teacher@test.com");
            StudentProfile student = persistStudent("s@test.com", 1);
            StudentProfile other = persistStudent("o@test.com", 2);
            persistFeedback(student, teacher, FeedbackCategory.GRADE, true, false);
            persistFeedback(student, teacher, FeedbackCategory.GRADE, false, true);
            persistFeedback(other, teacher, FeedbackCategory.GRADE, true, true); // 다른 student 노이즈
            em.flush();
            em.clear();

            List<Feedback> result = feedbackRepository.findAllByStudentIdAndStudentVisibleTrue(student.getId());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStudent().getId()).isEqualTo(student.getId());
            assertThat(result.get(0).isStudentVisible()).isTrue();
        }
    }

    @Nested
    @DisplayName("4. findAllByStudentIdAndStudentVisibleTrueAndCategory")
    class FindAllByStudentIdAndStudentVisibleTrueAndCategory {

        @Test
        @DisplayName("TC-F-4. visible=true이고 category 일치하는 것만 반환 (다른 student 노이즈 제외)")
        void onlyReturnsVisibleAndCategoryMatched() {
            TeacherProfile teacher = persistTeacher("teacher@test.com");
            StudentProfile student = persistStudent("s@test.com", 1);
            StudentProfile other = persistStudent("o@test.com", 2);
            persistFeedback(student, teacher, FeedbackCategory.GRADE, true, true);
            persistFeedback(student, teacher, FeedbackCategory.BEHAVIOR, true, true);
            persistFeedback(student, teacher, FeedbackCategory.GRADE, false, true);
            persistFeedback(other, teacher, FeedbackCategory.GRADE, true, true); // 다른 student 노이즈
            em.flush();
            em.clear();

            List<Feedback> result = feedbackRepository.findAllByStudentIdAndStudentVisibleTrueAndCategory(
                    student.getId(), FeedbackCategory.GRADE);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStudent().getId()).isEqualTo(student.getId());
            assertThat(result.get(0).isStudentVisible()).isTrue();
            assertThat(result.get(0).getCategory()).isEqualTo(FeedbackCategory.GRADE);
        }
    }

    @Nested
    @DisplayName("5. findAllByStudentIdAndParentVisibleTrue")
    class FindAllByStudentIdAndParentVisibleTrue {

        @Test
        @DisplayName("TC-F-5. parentVisible=true인 피드백만 반환 (다른 student 노이즈 제외)")
        void onlyReturnsParentVisible() {
            TeacherProfile teacher = persistTeacher("teacher@test.com");
            StudentProfile student = persistStudent("s@test.com", 1);
            StudentProfile other = persistStudent("o@test.com", 2);
            persistFeedback(student, teacher, FeedbackCategory.GRADE, false, true);
            persistFeedback(student, teacher, FeedbackCategory.GRADE, true, false);
            persistFeedback(other, teacher, FeedbackCategory.GRADE, true, true); // 다른 student 노이즈
            em.flush();
            em.clear();

            List<Feedback> result = feedbackRepository.findAllByStudentIdAndParentVisibleTrue(student.getId());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStudent().getId()).isEqualTo(student.getId());
            assertThat(result.get(0).isParentVisible()).isTrue();
        }
    }

    @Nested
    @DisplayName("6. findAllByStudentIdAndParentVisibleTrueAndCategory")
    class FindAllByStudentIdAndParentVisibleTrueAndCategory {

        @Test
        @DisplayName("TC-F-6. parentVisible=true이고 category 일치하는 것만 반환")
        void onlyReturnsParentVisibleAndCategoryMatched() {
            TeacherProfile teacher = persistTeacher("teacher@test.com");
            StudentProfile student = persistStudent("s@test.com", 1);
            persistFeedback(student, teacher, FeedbackCategory.GRADE, true, true);
            persistFeedback(student, teacher, FeedbackCategory.BEHAVIOR, true, true);  // category 다름
            persistFeedback(student, teacher, FeedbackCategory.GRADE, true, false);    // parentVisible 다름
            em.flush();
            em.clear();

            List<Feedback> result = feedbackRepository.findAllByStudentIdAndParentVisibleTrueAndCategory(
                    student.getId(), FeedbackCategory.GRADE);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).isParentVisible()).isTrue();
            assertThat(result.get(0).getCategory()).isEqualTo(FeedbackCategory.GRADE);
        }
    }

    @Nested
    @DisplayName("7. findByIdAndStudentId")
    class FindByIdAndStudentId {

        @Test
        @DisplayName("TC-F-7. 다른 studentId로 조회 시 empty (studentId 우회 방지)")
        void emptyWhenStudentIdMismatch() {
            TeacherProfile teacher = persistTeacher("teacher@test.com");
            StudentProfile studentA = persistStudent("a@test.com", 1);
            StudentProfile studentB = persistStudent("b@test.com", 2);
            Feedback feedback = persistFeedback(studentA, teacher, FeedbackCategory.GRADE, true, true);
            em.flush();
            em.clear();

            Optional<Feedback> result = feedbackRepository.findByIdAndStudentId(
                    feedback.getId(), studentB.getId());

            assertThat(result).isEmpty();
        }
    }

    private TeacherProfile persistTeacher(String email) {
        User user = User.of(email, "encoded", "teacher", Role.TEACHER);
        em.persist(user);
        TeacherProfile teacher = TeacherProfile.of(user, School.SUNRIN_HIGH_SCHOOL, 1, 1);
        em.persist(teacher);
        return teacher;
    }

    private StudentProfile persistStudent(String email, int number) {
        User user = User.of(email, "encoded", "student" + number, Role.STUDENT);
        em.persist(user);
        StudentProfile student = StudentProfile.of(user, School.SUNRIN_HIGH_SCHOOL, 1, 1, number);
        em.persist(student);
        return student;
    }

    private Feedback persistFeedback(StudentProfile student, TeacherProfile teacher,
                                      FeedbackCategory category, boolean studentVisible, boolean parentVisible) {
        Feedback feedback = Feedback.of(student, teacher, category, LocalDate.now(),
                "내용", studentVisible, parentVisible);
        em.persist(feedback);
        return feedback;
    }
}
