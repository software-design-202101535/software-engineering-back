package com.example.edumanager.domain.student.entity;

import com.example.edumanager.domain.user.entity.School;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.global.crypto.EncryptedLocalDateConverter;
import com.example.edumanager.global.crypto.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "student_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private School school;

    @Column(nullable = false)
    private int grade;

    @Column(name = "class_num", nullable = false)
    private int classNum;

    @Column(nullable = false)
    private int number;

    @Convert(converter = EncryptedLocalDateConverter.class)
    @Column(name = "birth_date", length = 100)
    private LocalDate birthDate;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 255)
    private String phone;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "parent_phone", length = 255)
    private String parentPhone;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String address;

    public static StudentProfile of(User user, School school, int grade, int classNum, int number) {
        StudentProfile profile = new StudentProfile();
        profile.user = user;
        profile.school = school;
        profile.grade = grade;
        profile.classNum = classNum;
        profile.number = number;
        return profile;
    }

    public void update(int grade, int classNum, int number) {
        this.grade = grade;
        this.classNum = classNum;
        this.number = number;
    }

    public void updateDetail(LocalDate birthDate, String phone, String parentPhone, String address) {
        this.birthDate = birthDate;
        this.phone = phone;
        this.parentPhone = parentPhone;
        this.address = address;
    }
}
