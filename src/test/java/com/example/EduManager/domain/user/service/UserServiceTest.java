package com.example.EduManager.domain.user.service;

import com.example.EduManager.domain.user.entity.Role;
import com.example.EduManager.domain.user.entity.User;
import com.example.EduManager.domain.user.repository.UserRepository;
import com.example.EduManager.global.exception.CustomException;
import com.example.EduManager.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    @Mock User user;

    @Nested
    @DisplayName("1. registerSchoolUser()")
    class RegisterSchoolUser {

        @Test
        @DisplayName("TC-1-1. 성공 → save 호출")
        void success() {
            when(userRepository.existsByEmail("a@test.com")).thenReturn(false);
            when(passwordEncoder.encode("pass")).thenReturn("encoded");
            when(userRepository.save(any())).thenReturn(user);

            User result = userService.registerSchoolUser("a@test.com", "pass", "홍길동", Role.TEACHER);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertAll(
                    () -> assertEquals("a@test.com", captor.getValue().getEmail()),
                    () -> assertEquals("홍길동", captor.getValue().getName()),
                    () -> assertEquals(Role.TEACHER, captor.getValue().getRole()),
                    () -> assertEquals("encoded", captor.getValue().getPassword()),
                    () -> assertEquals(user, result)
            );
        }

        @Test
        @DisplayName("TC-1-2. 이메일 중복 → DUPLICATED_USER, save never")
        void duplicatedEmail() {
            when(userRepository.existsByEmail("a@test.com")).thenReturn(true);

            CustomException ex = assertThrows(CustomException.class,
                    () -> userService.registerSchoolUser("a@test.com", "pass", "홍길동", Role.TEACHER));

            assertAll(
                    () -> assertEquals(ErrorCode.DUPLICATED_USER, ex.getErrorCode()),
                    () -> verify(userRepository, never()).save(any())
            );
        }
    }

    @Nested
    @DisplayName("2. registerParentUser()")
    class RegisterParentUser {

        @Test
        @DisplayName("TC-2-1. 성공 → save 호출")
        void success() {
            when(userRepository.existsByEmail("p@test.com")).thenReturn(false);
            when(passwordEncoder.encode("pass")).thenReturn("encoded");
            when(userRepository.save(any())).thenReturn(user);

            User result = userService.registerParentUser("p@test.com", "pass", "김부모");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertAll(
                    () -> assertEquals("p@test.com", captor.getValue().getEmail()),
                    () -> assertEquals("김부모", captor.getValue().getName()),
                    () -> assertEquals(Role.PARENT, captor.getValue().getRole()),
                    () -> assertEquals("encoded", captor.getValue().getPassword()),
                    () -> assertEquals(user, result)
            );
        }

        @Test
        @DisplayName("TC-2-2. 이메일 중복 → DUPLICATED_USER, save never")
        void duplicatedEmail() {
            when(userRepository.existsByEmail("p@test.com")).thenReturn(true);

            CustomException ex = assertThrows(CustomException.class,
                    () -> userService.registerParentUser("p@test.com", "pass", "김부모"));

            assertAll(
                    () -> assertEquals(ErrorCode.DUPLICATED_USER, ex.getErrorCode()),
                    () -> verify(userRepository, never()).save(any())
            );
        }
    }

    @Nested
    @DisplayName("3. getById()")
    class GetById {

        @Test
        @DisplayName("TC-3-1. 성공 → User 반환")
        void success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            assertEquals(user, userService.getById(1L));
        }

        @Test
        @DisplayName("TC-3-2. 없음 → USER_NOT_FOUND")
        void notFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            CustomException ex = assertThrows(CustomException.class,
                    () -> userService.getById(1L));

            assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("4. getByEmail()")
    class GetByEmail {

        @Test
        @DisplayName("TC-4-1. 성공 → User 반환")
        void success() {
            when(userRepository.findByEmail("a@test.com")).thenReturn(Optional.of(user));

            assertEquals(user, userService.getByEmail("a@test.com"));
        }

        @Test
        @DisplayName("TC-4-2. 없음 → BAD_CREDENTIALS")
        void notFound() {
            when(userRepository.findByEmail("a@test.com")).thenReturn(Optional.empty());

            CustomException ex = assertThrows(CustomException.class,
                    () -> userService.getByEmail("a@test.com"));

            assertEquals(ErrorCode.BAD_CREDENTIALS, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("5. getStudentByEmail()")
    class GetStudentByEmail {

        @Test
        @DisplayName("TC-5-1. STUDENT → User 반환")
        void success() {
            when(userRepository.findByEmail("s@test.com")).thenReturn(Optional.of(user));
            when(user.getRole()).thenReturn(Role.STUDENT);

            assertEquals(user, userService.getStudentByEmail("s@test.com"));
        }

        @Test
        @DisplayName("TC-5-2. 없음 → USER_NOT_FOUND")
        void notFound() {
            when(userRepository.findByEmail("s@test.com")).thenReturn(Optional.empty());

            CustomException ex = assertThrows(CustomException.class,
                    () -> userService.getStudentByEmail("s@test.com"));

            assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        }

        @ParameterizedTest(name = "{0}")
        @EnumSource(value = Role.class, names = {"TEACHER", "PARENT", "ADMIN"})
        @DisplayName("TC-5-3. STUDENT 아닌 Role → USER_NOT_FOUND")
        void nonStudentRole(Role role) {
            when(userRepository.findByEmail("s@test.com")).thenReturn(Optional.of(user));
            when(user.getRole()).thenReturn(role);

            CustomException ex = assertThrows(CustomException.class,
                    () -> userService.getStudentByEmail("s@test.com"));

            assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("6. updateName()")
    class UpdateName {

        @Test
        @DisplayName("TC-6-1. 성공 → user.updateName() 호출")
        void success() {
            userService.updateName(user, "새이름");

            verify(user).updateName("새이름");
        }
    }
}
