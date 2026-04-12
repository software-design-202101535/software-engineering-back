package com.example.EduManager.domain.student.entity;

import com.example.EduManager.domain.user.entity.User;
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

    @Column(nullable = false)
    private int grade;

    @Column(name = "class_num", nullable = false)
    private int classNum;

    @Column(nullable = false)
    private int number;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(length = 20)
    private String phone;

    @Column(name = "parent_phone", length = 20)
    private String parentPhone;

    @Column(length = 255)
    private String address;

    public static StudentProfile of(User user, int grade, int classNum, int number) {
        StudentProfile profile = new StudentProfile();
        profile.user = user;
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
