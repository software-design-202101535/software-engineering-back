package com.example.EduManager.facade;

import com.example.EduManager.domain.auth.dto.*;
import com.example.EduManager.domain.auth.service.AuthService;
import com.example.EduManager.domain.student.entity.StudentProfile;
import com.example.EduManager.domain.student.service.StudentService;
import com.example.EduManager.domain.teacher.service.TeacherService;
import com.example.EduManager.domain.user.entity.Role;
import com.example.EduManager.domain.user.entity.User;
import com.example.EduManager.domain.user.service.UserService;
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
    public UserResponse registerTeacher(TeacherRegisterRequest request) {
        User user = userService.registerSchoolUser(
                request.getEmail(), request.getPassword(), request.getName(),
                Role.TEACHER, request.getSchool(), request.getSchoolNumber());
        teacherService.createProfile(user, request.getGrade(), request.getClassNum());
        return UserResponse.of(user);
    }

    @Transactional
    public UserResponse registerStudent(StudentRegisterRequest request) {
        User user = userService.registerSchoolUser(
                request.getEmail(), request.getPassword(), request.getName(),
                Role.STUDENT, request.getSchool(), request.getSchoolNumber());
        studentService.createProfile(user, request.getGrade(), request.getClassNum(), request.getNumber());
        return UserResponse.of(user);
    }

    @Transactional
    public UserResponse registerParent(ParentRegisterRequest request) {
        User parent = userService.registerParentUser(
                request.getEmail(), request.getPassword(), request.getName());
        User childUser = userService.getStudentBySchoolAndSchoolNumber(
                request.getChildSchool(), request.getChildSchoolNumber());
        StudentProfile childProfile = studentService.getProfileByUser(childUser);
        studentService.linkParent(parent, childProfile);
        return UserResponse.of(parent);
    }

    @Transactional
    public LoginResponse loginBySchool(SchoolLoginRequest request) {
        User user = userService.getBySchoolAndSchoolNumber(request.getSchool(), request.getSchoolNumber());
        return buildLoginResponse(user, request.getPassword());
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
            default -> LoginResponse.ofTeacher(user, tokens);
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
