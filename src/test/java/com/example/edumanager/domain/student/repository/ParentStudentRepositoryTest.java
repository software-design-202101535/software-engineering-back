package com.example.edumanager.domain.student.repository;

import com.example.edumanager.domain.student.entity.ParentStudent;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.domain.user.entity.School;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.support.AbstractRepositoryIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ParentStudentRepository 통합 테스트")
class ParentStudentRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired ParentStudentRepository parentStudentRepository;
    @Autowired EntityManager em;

    @Test
    @DisplayName("TC-P-1. 양방향 매핑 + 빈 결과 + (parent_id, student_id) 유니크 제약")
    void bidirectionalMappingAndEmptyResultAndUniqueConstraint() {
        User parent1 = persistUser("p1@test.com", Role.PARENT);
        User parent2 = persistUser("p2@test.com", Role.PARENT);
        StudentProfile student1 = persistStudent("s1@test.com", 1);
        StudentProfile student2 = persistStudent("s2@test.com", 2);
        StudentProfile lonely = persistStudent("lonely@test.com", 99); // 어떤 부모와도 미연결

        em.persist(ParentStudent.of(parent1, student1));
        em.persist(ParentStudent.of(parent1, student2));
        em.persist(ParentStudent.of(parent2, student1));
        em.flush();
        em.clear();

        // 양방향 매핑
        assertThat(parentStudentRepository.existsByStudent(student1)).isTrue();
        assertThat(parentStudentRepository.findAllByStudent(student1)).hasSize(2);
        assertThat(parentStudentRepository.findAllByParent(parent1)).hasSize(2);

        // 빈 결과 (null 아닌 빈 리스트, exists=false)
        assertThat(parentStudentRepository.existsByStudent(lonely)).isFalse();
        assertThat(parentStudentRepository.findAllByStudent(lonely)).isEmpty();

        // 유니크 제약
        assertThatThrownBy(() -> parentStudentRepository.saveAndFlush(ParentStudent.of(parent1, student1)))
                .as("(parent_id, student_id) 중복 INSERT는 유니크 제약 위반")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private User persistUser(String email, Role role) {
        User user = User.of(email, "encoded", "name", role);
        em.persist(user);
        return user;
    }

    private StudentProfile persistStudent(String email, int number) {
        User user = persistUser(email, Role.STUDENT);
        StudentProfile student = StudentProfile.of(user, School.SUNRIN_HIGH_SCHOOL, 1, 1, number);
        em.persist(student);
        return student;
    }
}
