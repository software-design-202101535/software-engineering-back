package com.example.EduManager.facade;

import com.example.EduManager.domain.auth.dto.*;
import com.example.EduManager.domain.auth.service.AuthService;
import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.student.service.StudentService;
import com.example.EduManager.domain.teacher.service.TeacherService;
import com.example.EduManager.domain.user.entity.Role;
import com.example.EduManager.domain.user.entity.School;
import com.example.EduManager.domain.user.entity.User;
import com.example.EduManager.domain.user.service.UserService;
import com.example.EduManager.global.exception.CustomException;
import com.example.EduManager.global.exception.ErrorCode;
import com.example.EduManager.global.util.EnumConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AuthFacade {

    private final UserService userService;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final AuthService authService;

    @Transactional
    public void registerTeacher(TeacherRegisterRequest request) {
        validatePasswordConfirm(request.getPassword(), request.getPasswordConfirm());
        School school = EnumConverter.stringToEnum(request.getSchool(), School.class, ErrorCode.INVALID_SCHOOL);
        User user = userService.registerSchoolUser(
                request.getEmail(), request.getPassword(), request.getName(), Role.TEACHER);
        teacherService.createProfile(user, school, request.getGrade(), request.getClassNum());
    }

    @Transactional
    public void registerStudent(StudentRegisterRequest request) {
        validatePasswordConfirm(request.getPassword(), request.getPasswordConfirm());
        School school = EnumConverter.stringToEnum(request.getSchool(), School.class, ErrorCode.INVALID_SCHOOL);
        User user = userService.registerSchoolUser(
                request.getEmail(), request.getPassword(), request.getName(), Role.STUDENT);
        studentService.createProfile(user, school, request.getGrade(), request.getClassNum(), request.getNumber());
    }

    @Transactional
    public void registerParent(ParentRegisterRequest request) {
        validatePasswordConfirm(request.getPassword(), request.getPasswordConfirm());
        User parent = userService.registerParentUser(
                request.getEmail(), request.getPassword(), request.getName());
        User childUser = userService.getStudentByEmail(request.getChildEmail());
        StudentProfile childProfile = studentService.getProfileByUser(childUser);
        studentService.linkParent(parent, childProfile);
    }

    private void validatePasswordConfirm(String password, String passwordConfirm) {
        if (!password.equals(passwordConfirm)) {
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }
    }

    @Transactional
    public LoginResponse loginByEmail(EmailLoginRequest request) {
        User user = userService.getByEmail(request.getEmail());
        return buildLoginResponse(user, request.getPassword());
    }

    private LoginResponse buildLoginResponse(User user, String rawPassword) {
        AuthTokens tokens = authService.authenticate(user, rawPassword);
        return switch (user.getRole()) {
            case STUDENT -> LoginResponse.ofStudent(user, tokens,
                    studentService.getProfileByUser(user).getId());
            case PARENT -> LoginResponse.ofParent(user, tokens,
                    studentService.getProfilesByParent(user).stream().map(ChildSummary::of).toList());
            default -> LoginResponse.ofTeacher(user, tokens,
                    teacherService.getProfileByUserId(user.getId()));
        };
    }

    @Transactional
    public RefreshResult refresh(String refreshToken) {
        return authService.refresh(refreshToken);
    }

    @Transactional
    public void logout(Long userId) {
        User user = userService.getById(userId);
        authService.logout(user);
    }
}
