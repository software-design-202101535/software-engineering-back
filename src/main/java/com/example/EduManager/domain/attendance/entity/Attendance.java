package com.example.EduManager.domain.attendance.entity;

import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.teacher.entity.TeacherProfile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance",
        uniqueConstraints = @UniqueConstraint(name = "uq_attendance_student_date", columnNames = {"student_id", "attendance_date"}))
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    @Column(length = 255)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private TeacherProfile createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Attendance of(StudentProfile student, LocalDate date, AttendanceStatus status, String note, TeacherProfile createdBy) {
        Attendance attendance = new Attendance();
        attendance.student = student;
        attendance.date = date;
        attendance.status = status;
        attendance.note = note;
        attendance.createdBy = createdBy;
        return attendance;
    }

    public void update(LocalDate date, AttendanceStatus status, String note) {
        this.date = date;
        this.status = status;
        this.note = note;
    }
}
