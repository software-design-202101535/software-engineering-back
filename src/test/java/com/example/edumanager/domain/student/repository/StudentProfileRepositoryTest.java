package com.example.edumanager.domain.student.repository;

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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StudentProfileRepository 통합 테스트")
class StudentProfileRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired StudentProfileRepository studentProfileRepository;
    @Autowired EntityManager em;

    @Nested
    @DisplayName("1. findAllByGradeAndClassNumAndSchool")
    class FindAllByGradeAndClassNumAndSchool {

        @Test
        @DisplayName("TC-S-1. 같은 school+grade+classNum 학생만 number 오름차순으로 반환 (다른 반/학년/학교 number=1 노이즈도 끼어들지 않음)")
        void filtersBySchoolGradeClassNumAndOrdersByNumberAsc() {
            // 1학년 1반 학생 3명 (number 3, 1, 2 순서로 저장)
            persistStudent("s3@test.com", School.SUNRIN_HIGH_SCHOOL, 1, 1, 3);
            persistStudent("s1@test.com", School.SUNRIN_HIGH_SCHOOL, 1, 1, 1);
            persistStudent("s2@test.com", School.SUNRIN_HIGH_SCHOOL, 1, 1, 2);
            // 다른 반/학년/학교 number=1 노이즈 (정렬에 끼어들면 안 됨)
            persistStudent("other-class@test.com", School.SUNRIN_HIGH_SCHOOL, 1, 2, 1);
            persistStudent("other-grade@test.com", School.SUNRIN_HIGH_SCHOOL, 2, 1, 1);
            persistStudent("other-school@test.com", School.SEOUL_HIGH_SCHOOL, 1, 1, 1);
            em.flush();
            em.clear();

            List<StudentProfile> result = studentProfileRepository.findAllByGradeAndClassNumAndSchool(
                    1, 1, School.SUNRIN_HIGH_SCHOOL);

            assertThat(result).hasSize(3);
            assertThat(result).extracting(StudentProfile::getNumber).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("2. findByUser")
    class FindByUser {

        @Test
        @DisplayName("TC-S-2. 매핑된 user → student 반환 / 미매핑 user → empty")
        void returnsStudentOrEmpty() {
            User mappedUser = User.of("u1@test.com", "encoded", "n1", Role.STUDENT);
            em.persist(mappedUser);
            StudentProfile student = StudentProfile.of(mappedUser, School.SUNRIN_HIGH_SCHOOL, 1, 1, 1);
            em.persist(student);

            User unmappedUser = User.of("u2@test.com", "encoded", "n2", Role.STUDENT);
            em.persist(unmappedUser);
            em.flush();
            em.clear();

            assertThat(studentProfileRepository.findByUser(mappedUser))
                    .isPresent()
                    .get()
                    .extracting(StudentProfile::getNumber).isEqualTo(1);
            assertThat(studentProfileRepository.findByUser(unmappedUser)).isEmpty();
        }
    }

    private void persistStudent(String email, School school, int grade, int classNum, int number) {
        User user = User.of(email, "encoded", "student", Role.STUDENT);
        em.persist(user);
        StudentProfile student = StudentProfile.of(user, school, grade, classNum, number);
        em.persist(student);
    }
}
