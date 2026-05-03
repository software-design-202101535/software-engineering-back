package com.example.edumanager.domain.teacher.entity;

import com.example.edumanager.domain.user.entity.School;
import com.example.edumanager.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "teacher_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeacherProfile {

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

    private TeacherProfile(User user, School school, int grade, int classNum) {
        this.user = user;
        this.school = school;
        this.grade = grade;
        this.classNum = classNum;
    }

    public static TeacherProfile of(User user, School school, int grade, int classNum) {
        return new TeacherProfile(user, school, grade, classNum);
    }

    public void update(int grade, int classNum) {
        this.grade = grade;
        this.classNum = classNum;
    }
}
