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

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentProfileRepository studentProfileRepository;
    private final ParentStudentRepository parentStudentRepository;

    public void createProfile(User user, School school, int grade, int classNum, int number) {
        studentProfileRepository.save(StudentProfile.of(user, school, grade, classNum, number));
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

    public List<StudentProfile> getProfilesByParent(User parent) {
        return parentStudentRepository.findAllByParent(parent).stream()
                .map(ParentStudent::getStudent)
                .toList();
    }

    public List<StudentProfile> getClassStudents(int grade, int classNum, School school) {
        return studentProfileRepository.findAllByGradeAndClassNumAndSchool(grade, classNum, school);
    }

    public void linkParent(User parent, StudentProfile student) {
        parentStudentRepository.save(ParentStudent.of(parent, student));
    }

    public void updateDetail(StudentProfile student, UpdateStudentRequest request) {
        LocalDate birthDate = parseBirthDate(request.getBirthDate());
        student.updateDetail(birthDate, request.getPhone(), request.getParentPhone(), request.getAddress());
    }

    private LocalDate parseBirthDate(String birthDate) {
        if (birthDate == null) return null;
        try {
            return LocalDate.parse(birthDate);
        } catch (DateTimeParseException e) {
            throw new CustomException(ErrorCode.INVALID_BIRTH_DATE);
        }
    }
}
