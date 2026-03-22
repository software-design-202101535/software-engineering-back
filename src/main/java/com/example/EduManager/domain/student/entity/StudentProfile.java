package com.example.EduManager.domain.student.entity;

import com.example.EduManager.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private Byte grade;

    @Column(name = "class_num", nullable = false)
    private Byte classNum;

    @Column(nullable = false)
    private Byte number;

    @Column(columnDefinition = "TEXT")
    private String specialNotes;

    public static StudentProfile of(User user, Byte grade, Byte classNum, Byte number) {
        StudentProfile profile = new StudentProfile();
        profile.user = user;
        profile.grade = grade;
        profile.classNum = classNum;
        profile.number = number;
        return profile;
    }

    public void update(Byte grade, Byte classNum, Byte number, String specialNotes) {
        this.grade = grade;
        this.classNum = classNum;
        this.number = number;
        this.specialNotes = specialNotes;
    }
}
