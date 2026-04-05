package com.example.EduManager.domain.student.service;

import com.example.EduManager.domain.student.dto.UpdateStudentRequest;
import com.example.EduManager.domain.student.entity.ParentStudent;
import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.student.repository.ParentStudentRepository;
import com.example.EduManager.domain.student.repository.StudentProfileRepository;
import com.example.EduManager.domain.user.entity.School;
import com.example.EduManager.domain.user.entity.User;
import com.example.EduManager.global.exception.CustomException;
import com.example.EduManager.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentProfileRepository studentProfileRepository;
    private final ParentStudentRepository parentStudentRepository;

    public void createProfile(User user, int grade, int classNum, int number) {
        studentProfileRepository.save(StudentProfile.of(user, grade, classNum, number));
    }

    public StudentProfile getProfileByUser(User user) {
        return studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public StudentProfile getById(Long studentId) {
        return studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));
    }

    public List<User> getParentsByStudentId(Long studentId) {
        StudentProfile student = getById(studentId);
        return parentStudentRepository.findAllByStudent(student).stream()
                .map(ParentStudent::getParent)
                .toList();
    }

    public List<StudentProfile> getClassStudents(int grade, int classNum, School school) {
        return studentProfileRepository.findAllByGradeAndClassNumAndUserSchool(grade, classNum, school);
    }

    public void linkParent(User parent, StudentProfile student) {
        parentStudentRepository.save(ParentStudent.of(parent, student));
    }

    public void updateDetail(StudentProfile student, UpdateStudentRequest request) {
        student.updateDetail(request.getBirthDate(), request.getPhone(),
                request.getParentPhone(), request.getAddress());
    }
}
