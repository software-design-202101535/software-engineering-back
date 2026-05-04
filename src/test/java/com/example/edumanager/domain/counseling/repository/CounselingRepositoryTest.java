package com.example.edumanager.domain.counseling.repository;

import com.example.edumanager.domain.counseling.entity.Counseling;
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

@DisplayName("CounselingRepository 통합 테스트")
class CounselingRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired CounselingRepository counselingRepository;
    @Autowired EntityManager em;

    @Nested
    @DisplayName("1. findByStudentForTeacherByYear")
    class FindByStudentForTeacherByYear {

        @Test
        @DisplayName("TC-C-1. 본인작성/타인공유만 보임 + year 경계값(직전/다음년 제외) + OR 양쪽 매칭 중복 row 없음 + JOIN FETCH N+1 없음")
        void teacherSeesOwnAndSharedWithBoundaryAndNoDuplicateAndNoNplusOne() {
            StudentProfile student = persistStudent("s@test.com", 1);
            TeacherProfile teacherA = persistTeacher("a@test.com", 1);
            TeacherProfile teacherB = persistTeacher("b@test.com", 2);
            // 2026년 데이터
            persistCounseling(student, teacherA, LocalDate.of(2026, 1, 1), false);   // A 본인 (왼쪽 매칭)
            persistCounseling(student, teacherA, LocalDate.of(2026, 6, 15), true);   // A 본인+공유 (양쪽 매칭 → 1건)
            persistCounseling(student, teacherB, LocalDate.of(2026, 12, 31), true);  // B 공유 (오른쪽 매칭)
            persistCounseling(student, teacherB, LocalDate.of(2026, 3, 3), false);   // B 비공유 → 제외
            // year 경계 노이즈
            persistCounseling(student, teacherA, LocalDate.of(2025, 12, 31), true);  // 직전 연도 → 제외
            persistCounseling(student, teacherA, LocalDate.of(2027, 1, 1), true);    // 다음 연도 → 제외
            em.flush();
            em.clear();

            Statistics stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
            stats.clear();

            List<Counseling> result = counselingRepository.findByStudentForTeacherByYear(
                    student.getId(), teacherA.getUser().getId(), 2026);
            result.forEach(c -> c.getTeacher().getUser().getName()); // LAZY 강제

            assertThat(result)
                    .as("A의 비공유, A의 본인+공유(중복없이 1건), B의 공유 = 3건. 다른 year/B 비공유는 제외")
                    .hasSize(3)
                    .extracting(c -> c.getDate().getYear()).containsOnly(2026);
            assertThat(stats.getPrepareStatementCount())
                    .as("JOIN FETCH 적용 → teacher.user 까지 단일 쿼리")
                    .isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("2. findByStudentForTeacherByYearAndMonth")
    class FindByStudentForTeacherByYearAndMonth {

        @Test
        @DisplayName("TC-C-2. year+month 필터링 + 월 경계값(전월말/다음월초 제외)")
        void filtersByYearAndMonthWithBoundary() {
            StudentProfile student = persistStudent("s@test.com", 1);
            TeacherProfile teacher = persistTeacher("t@test.com", 1);
            persistCounseling(student, teacher, LocalDate.of(2026, 3, 1), false);    // 매치 (시작 경계)
            persistCounseling(student, teacher, LocalDate.of(2026, 3, 31), false);   // 매치 (끝 경계)
            persistCounseling(student, teacher, LocalDate.of(2026, 2, 28), false);   // 전월말 → 제외
            persistCounseling(student, teacher, LocalDate.of(2026, 4, 1), false);    // 다음월 시작 → 제외
            em.flush();
            em.clear();

            List<Counseling> result = counselingRepository.findByStudentForTeacherByYearAndMonth(
                    student.getId(), teacher.getUser().getId(), 2026, 3);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Counseling::getDate)
                    .containsExactlyInAnyOrder(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));
        }
    }

    @Nested
    @DisplayName("3. findByIdAndStudentId")
    class FindByIdAndStudentId {

        @Test
        @DisplayName("TC-C-3. 다른 studentId로 조회 시 empty")
        void emptyWhenStudentIdMismatch() {
            TeacherProfile teacher = persistTeacher("t@test.com", 1);
            StudentProfile studentA = persistStudent("a@test.com", 1);
            StudentProfile studentB = persistStudent("b@test.com", 2);
            Counseling counseling = persistCounseling(studentA, teacher, LocalDate.now(), false);
            em.flush();
            em.clear();

            Optional<Counseling> result = counselingRepository.findByIdAndStudentId(
                    counseling.getId(), studentB.getId());

            assertThat(result).isEmpty();
        }
    }

    private TeacherProfile persistTeacher(String email, int classNum) {
        User user = User.of(email, "encoded", "teacher" + classNum, Role.TEACHER);
        em.persist(user);
        TeacherProfile teacher = TeacherProfile.of(user, School.SUNRIN_HIGH_SCHOOL, 1, classNum);
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

    private Counseling persistCounseling(StudentProfile student, TeacherProfile teacher,
                                          LocalDate date, boolean shared) {
        Counseling counseling = Counseling.of(student, teacher, date, "내용",
                "다음계획", date.plusDays(7), shared);
        em.persist(counseling);
        return counseling;
    }
}
