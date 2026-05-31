package com.example.edumanager.global.crypto;

import com.example.edumanager.domain.counseling.entity.Counseling;
import com.example.edumanager.domain.feedback.entity.Feedback;
import com.example.edumanager.domain.feedback.entity.FeedbackCategory;
import com.example.edumanager.domain.student.entity.NoteCategory;
import com.example.edumanager.domain.student.entity.StudentNote;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.teacher.entity.TeacherProfile;
import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.domain.user.entity.School;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.support.AbstractRepositoryIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("암호화 컨버터 통합 테스트")
class EncryptionConverterIntegrationTest extends AbstractRepositoryIntegrationTest {

    @Autowired EntityManager em;

    @Test
    @DisplayName("TC-1. 문자열 PII(phone/parentPhone/address)는 평문 복원되고 DB 원본은 암호문이다")
    void stringPiiEncryptedAtRestAndDecryptedOnLoad() {
        StudentProfile student = persistStudentWithDetail(
                LocalDate.of(2008, 3, 14), "010-1111-2222", "010-3333-4444", "서울특별시 강남구 테헤란로 123");
        Long id = student.getId();
        em.flush();
        em.clear();

        StudentProfile loaded = em.find(StudentProfile.class, id);
        assertThat(loaded.getPhone()).isEqualTo("010-1111-2222");
        assertThat(loaded.getParentPhone()).isEqualTo("010-3333-4444");
        assertThat(loaded.getAddress()).isEqualTo("서울특별시 강남구 테헤란로 123");

        Object[] raw = (Object[]) em.createNativeQuery(
                        "SELECT phone, parent_phone, address FROM student_profiles WHERE id = :id")
                .setParameter("id", id)
                .getSingleResult();
        assertThat((String) raw[0]).isNotEqualTo("010-1111-2222");
        assertThat((String) raw[1]).isNotEqualTo("010-3333-4444");
        assertThat((String) raw[2]).isNotEqualTo("서울특별시 강남구 테헤란로 123");
    }

    @Test
    @DisplayName("TC-2. birthDate(LocalDate)는 평문 복원되고 DB 원본은 평문 날짜가 아니다")
    void localDateEncryptedAtRestAndDecryptedOnLoad() {
        StudentProfile student = persistStudentWithDetail(
                LocalDate.of(2008, 3, 14), "010-0000-0000", "010-0000-0001", "주소");
        Long id = student.getId();
        em.flush();
        em.clear();

        StudentProfile loaded = em.find(StudentProfile.class, id);
        assertThat(loaded.getBirthDate()).isEqualTo(LocalDate.of(2008, 3, 14));

        String raw = (String) em.createNativeQuery(
                        "SELECT birth_date FROM student_profiles WHERE id = :id")
                .setParameter("id", id)
                .getSingleResult();
        assertThat(raw).isNotEqualTo("2008-03-14");
    }

    @Test
    @DisplayName("TC-3. content류(상담 content/nextPlan, 특기사항, 피드백)도 평문 복원되고 DB 원본은 암호문이다")
    void contentFieldsEncryptedAcrossEntities() {
        StudentProfile student = persistStudentWithDetail(null, null, null, null);
        TeacherProfile teacher = persistTeacher();

        Counseling counseling = Counseling.of(student, teacher, LocalDate.of(2026, 5, 1),
                "상담 내용 비밀", "다음 계획 비밀", LocalDate.of(2026, 6, 1), false);
        StudentNote note = StudentNote.of(student, NoteCategory.SPECIAL, "특기사항 비밀", LocalDate.of(2026, 5, 1), teacher);
        Feedback feedback = Feedback.of(student, teacher, FeedbackCategory.BEHAVIOR, LocalDate.of(2026, 5, 1),
                "피드백 비밀", true, true);
        em.persist(counseling);
        em.persist(note);
        em.persist(feedback);
        em.flush();
        em.clear();

        assertThat(em.find(Counseling.class, counseling.getId()).getContent()).isEqualTo("상담 내용 비밀");
        assertThat(em.find(Counseling.class, counseling.getId()).getNextPlan()).isEqualTo("다음 계획 비밀");
        assertThat(em.find(StudentNote.class, note.getId()).getContent()).isEqualTo("특기사항 비밀");
        assertThat(em.find(Feedback.class, feedback.getId()).getContent()).isEqualTo("피드백 비밀");

        assertThat(rawColumn("counselings", "content", counseling.getId())).isNotEqualTo("상담 내용 비밀");
        assertThat(rawColumn("counselings", "next_plan", counseling.getId())).isNotEqualTo("다음 계획 비밀");
        assertThat(rawColumn("student_notes", "content", note.getId())).isNotEqualTo("특기사항 비밀");
        assertThat(rawColumn("feedbacks", "content", feedback.getId())).isNotEqualTo("피드백 비밀");
    }

    @Test
    @DisplayName("TC-4. null 값은 암호화 없이 null 로 저장/복원된다")
    void nullValuesPreserved() {
        StudentProfile student = persistStudentWithDetail(null, null, null, null);
        Long id = student.getId();
        em.flush();
        em.clear();

        StudentProfile loaded = em.find(StudentProfile.class, id);
        assertThat(loaded.getBirthDate()).isNull();
        assertThat(loaded.getPhone()).isNull();
        assertThat(loaded.getParentPhone()).isNull();
        assertThat(loaded.getAddress()).isNull();
    }

    // ---------- 헬퍼 ----------

    private StudentProfile persistStudentWithDetail(LocalDate birthDate, String phone, String parentPhone, String address) {
        User user = User.of("s" + System.nanoTime() + "@test.com", "pw", "학생", Role.STUDENT);
        em.persist(user);
        StudentProfile student = StudentProfile.of(user, School.SUNRIN_HIGH_SCHOOL, 1, 1, 1);
        student.updateDetail(birthDate, phone, parentPhone, address);
        em.persist(student);
        return student;
    }

    private TeacherProfile persistTeacher() {
        User user = User.of("t" + System.nanoTime() + "@test.com", "pw", "교사", Role.TEACHER);
        em.persist(user);
        TeacherProfile teacher = TeacherProfile.of(user, School.SUNRIN_HIGH_SCHOOL, 1, 1);
        em.persist(teacher);
        return teacher;
    }

    private String rawColumn(String table, String column, Long id) {
        return (String) em.createNativeQuery(
                        "SELECT " + column + " FROM " + table + " WHERE id = :id")
                .setParameter("id", id)
                .getSingleResult();
    }
}
