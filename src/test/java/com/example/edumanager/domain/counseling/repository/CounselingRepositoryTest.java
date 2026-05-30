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

    @Nested
    @DisplayName("4. findSharedForTeacher")
    class FindSharedForTeacher {

        private static final School SCHOOL = School.SUNRIN_HIGH_SCHOOL;

        @Test
        @DisplayName("TC-S-1. 같은학교+공유+본인제외만 반환 (비공유/타학교/본인작성 제외) + JOIN FETCH N+1 없음")
        void returnsOnlySharedSameSchoolExcludingOwnWithoutNplusOne() {
            TeacherProfile requester = persistTeacher("req@test.com", SCHOOL, 1, 1);
            TeacherProfile otherSameSchool = persistTeacher("other@test.com", SCHOOL, 1, 2);
            TeacherProfile otherSchoolTeacher = persistTeacher("diff@test.com", School.SEOUL_HIGH_SCHOOL, 1, 1);
            StudentProfile student = persistStudent("s@test.com", "김영희", SCHOOL, 1, 1, 1);
            StudentProfile otherSchoolStudent = persistStudent("os@test.com", "박철수", School.SEOUL_HIGH_SCHOOL, 1, 1, 1);

            Counseling included = persistCounseling(student, otherSameSchool, LocalDate.of(2026, 5, 20), true);
            persistCounseling(student, requester, LocalDate.of(2026, 5, 21), true);                 // 본인 작성 → 제외
            persistCounseling(student, otherSameSchool, LocalDate.of(2026, 5, 22), false);          // 비공유 → 제외
            persistCounseling(otherSchoolStudent, otherSchoolTeacher, LocalDate.of(2026, 5, 23), true); // 타학교 → 제외
            em.flush();
            em.clear();

            Statistics stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
            stats.clear();

            List<Counseling> result = counselingRepository.findSharedForTeacher(
                    SCHOOL, requester.getUser().getId(), 2026, null, null, null, null);
            result.forEach(c -> {
                c.getStudent().getUser().getName();
                c.getTeacher().getUser().getName();
            });

            assertThat(result).extracting(Counseling::getId).containsExactly(included.getId());
            assertThat(stats.getPrepareStatementCount())
                    .as("JOIN FETCH student.user/teacher.user → 단일 쿼리")
                    .isEqualTo(1);
        }

        @Test
        @DisplayName("TC-S-2. year 경계 + month(null=연도 전체, 특정월) 필터")
        void filtersByYearAndMonth() {
            TeacherProfile requester = persistTeacher("req@test.com", SCHOOL, 1, 1);
            TeacherProfile other = persistTeacher("other@test.com", SCHOOL, 1, 2);
            StudentProfile student = persistStudent("s@test.com", "김영희", SCHOOL, 1, 1, 1);
            persistCounseling(student, other, LocalDate.of(2026, 3, 15), true);
            persistCounseling(student, other, LocalDate.of(2026, 7, 1), true);
            persistCounseling(student, other, LocalDate.of(2025, 12, 31), true);  // 직전 연도 → 제외
            persistCounseling(student, other, LocalDate.of(2027, 1, 1), true);    // 다음 연도 → 제외
            em.flush();
            em.clear();

            Long requesterId = requester.getUser().getId();
            assertThat(counselingRepository.findSharedForTeacher(SCHOOL, requesterId, 2026, null, null, null, null))
                    .as("month 생략 → 2026 전체")
                    .hasSize(2);
            List<Counseling> march = counselingRepository.findSharedForTeacher(SCHOOL, requesterId, 2026, 3, null, null, null);
            assertThat(march).extracting(Counseling::getDate).containsExactly(LocalDate.of(2026, 3, 15));
        }

        @Test
        @DisplayName("TC-S-3. grade/classNum 필터 (다른 학년·반 제외)")
        void filtersByGradeAndClassNum() {
            TeacherProfile requester = persistTeacher("req@test.com", SCHOOL, 1, 1);
            TeacherProfile other = persistTeacher("other@test.com", SCHOOL, 1, 9);
            StudentProfile g1c1 = persistStudent("a@test.com", "학생A", SCHOOL, 1, 1, 1);
            StudentProfile g1c2 = persistStudent("b@test.com", "학생B", SCHOOL, 1, 2, 1);
            StudentProfile g2c1 = persistStudent("c@test.com", "학생C", SCHOOL, 2, 1, 1);
            persistCounseling(g1c1, other, LocalDate.of(2026, 5, 1), true);
            persistCounseling(g1c2, other, LocalDate.of(2026, 5, 1), true);
            persistCounseling(g2c1, other, LocalDate.of(2026, 5, 1), true);
            em.flush();
            em.clear();

            Long requesterId = requester.getUser().getId();
            assertThat(counselingRepository.findSharedForTeacher(SCHOOL, requesterId, 2026, null, 1, null, null))
                    .as("grade=1 → g1c1, g1c2")
                    .hasSize(2);
            assertThat(counselingRepository.findSharedForTeacher(SCHOOL, requesterId, 2026, null, 1, 1, null))
                    .as("grade=1 & classNum=1 → g1c1")
                    .extracting(c -> c.getStudent().getId())
                    .containsExactly(g1c1.getId());
        }

        @Test
        @DisplayName("TC-S-4. name 부분일치 필터")
        void filtersByNamePartialMatch() {
            TeacherProfile requester = persistTeacher("req@test.com", SCHOOL, 1, 1);
            TeacherProfile other = persistTeacher("other@test.com", SCHOOL, 1, 2);
            StudentProfile kimYoungHee = persistStudent("a@test.com", "김영희", SCHOOL, 1, 1, 1);
            StudentProfile parkYoungSu = persistStudent("b@test.com", "박영수", SCHOOL, 1, 1, 2);
            StudentProfile kimChulSu = persistStudent("c@test.com", "김철수", SCHOOL, 1, 1, 3);
            persistCounseling(kimYoungHee, other, LocalDate.of(2026, 5, 1), true);
            persistCounseling(parkYoungSu, other, LocalDate.of(2026, 5, 1), true);
            persistCounseling(kimChulSu, other, LocalDate.of(2026, 5, 1), true);
            em.flush();
            em.clear();

            Long requesterId = requester.getUser().getId();
            assertThat(counselingRepository.findSharedForTeacher(SCHOOL, requesterId, 2026, null, null, null, "영"))
                    .as("'영' 포함 → 김영희, 박영수")
                    .extracting(c -> c.getStudent().getId())
                    .containsExactlyInAnyOrder(kimYoungHee.getId(), parkYoungSu.getId());
            assertThat(counselingRepository.findSharedForTeacher(SCHOOL, requesterId, 2026, null, null, null, "김영"))
                    .as("'김영' 포함 → 김영희")
                    .extracting(c -> c.getStudent().getId())
                    .containsExactly(kimYoungHee.getId());
        }

        @Test
        @DisplayName("TC-S-5. counselingDate DESC, id DESC 정렬 (동일 날짜 tie-break)")
        void ordersByCounselingDateDescThenIdDesc() {
            TeacherProfile requester = persistTeacher("req@test.com", SCHOOL, 1, 1);
            TeacherProfile other = persistTeacher("other@test.com", SCHOOL, 1, 2);
            StudentProfile student = persistStudent("s@test.com", "김영희", SCHOOL, 1, 1, 1);
            Counseling older = persistCounseling(student, other, LocalDate.of(2026, 5, 10), true);
            Counseling sameDayFirst = persistCounseling(student, other, LocalDate.of(2026, 5, 20), true);
            Counseling sameDayLast = persistCounseling(student, other, LocalDate.of(2026, 5, 20), true);
            em.flush();
            em.clear();

            List<Counseling> result = counselingRepository.findSharedForTeacher(
                    SCHOOL, requester.getUser().getId(), 2026, null, null, null, null);

            assertThat(result).extracting(Counseling::getId)
                    .containsExactly(sameDayLast.getId(), sameDayFirst.getId(), older.getId());
        }

        private TeacherProfile persistTeacher(String email, School school, int grade, int classNum) {
            User user = User.of(email, "encoded", "teacher", Role.TEACHER);
            em.persist(user);
            TeacherProfile teacher = TeacherProfile.of(user, school, grade, classNum);
            em.persist(teacher);
            return teacher;
        }

        private StudentProfile persistStudent(String email, String name, School school,
                                              int grade, int classNum, int number) {
            User user = User.of(email, "encoded", name, Role.STUDENT);
            em.persist(user);
            StudentProfile student = StudentProfile.of(user, school, grade, classNum, number);
            em.persist(student);
            return student;
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
