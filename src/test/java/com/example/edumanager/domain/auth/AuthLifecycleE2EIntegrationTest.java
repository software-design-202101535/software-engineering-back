package com.example.edumanager.domain.auth;

import com.example.edumanager.domain.auth.dto.EmailLoginRequest;
import com.example.edumanager.domain.auth.dto.ParentRegisterRequest;
import com.example.edumanager.domain.auth.dto.StudentRegisterRequest;
import com.example.edumanager.domain.student.entity.ParentStudent;
import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.student.repository.ParentStudentRepository;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.support.AbstractIntegrationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Auth 라이프사이클 E2E 통합 테스트")
class AuthLifecycleE2EIntegrationTest extends AbstractIntegrationTest {

    private static final String PASSWORD = "password1!";
    private static final String SCHOOL = "SUNRIN_HIGH_SCHOOL";
    private static final String REFRESH_COOKIE = "refreshToken";

    @Autowired
    private ParentStudentRepository parentStudentRepository;

    @Nested
    @DisplayName("1. 풀 사이클 (가입 → 로그인 → 보호 API → 로그아웃)")
    class FullCycle {

        @Test
        @DisplayName("TC-1. 학생 가입 → 로그인 → 로그아웃: access/refresh 쿠키 + refresh_tokens row 일관성")
        void studentFullCycle() throws Exception {
            registerStudent("student@test.com", "홍길동", 1, 2, 3);

            MvcResult loginResult = mockMvc.perform(post("/api/auth/login/email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    EmailLoginRequest.of("student@test.com", PASSWORD))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.role").value("STUDENT"))
                    .andReturn();

            Cookie loginCookie = loginResult.getResponse().getCookie(REFRESH_COOKIE);
            assertThat(loginCookie).isNotNull();
            assertThat(loginCookie.getValue()).isNotBlank();
            assertThat(loginCookie.getMaxAge()).isPositive();
            assertThat(refreshTokenRepository.count()).isEqualTo(1);

            String accessToken = readAccessToken(loginResult);

            MvcResult logoutResult = mockMvc.perform(authPost("/api/auth/logout", accessToken))
                    .andExpect(status().isOk())
                    .andReturn();

            Cookie logoutCookie = logoutResult.getResponse().getCookie(REFRESH_COOKIE);
            assertThat(logoutCookie).isNotNull();
            assertThat(logoutCookie.getMaxAge()).isZero();
            assertThat(refreshTokenRepository.count()).isZero();
        }
    }

    @Nested
    @DisplayName("2. Refresh token rotation")
    class RefreshRotation {

        @Test
        @DisplayName("TC-2. /refresh 호출 시 옛 token 즉시 무효 + 새 token 만 유효")
        void rotationInvalidatesOldToken() throws Exception {
            registerStudent("rot@test.com", "회전", 2, 3, 4);
            Cookie rt1 = login("rot@test.com");

            MvcResult first = mockMvc.perform(post("/api/auth/refresh").cookie(rt1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andReturn();
            Cookie rt2 = first.getResponse().getCookie(REFRESH_COOKIE);
            String newAccessToken = readAccessToken(first);
            assertThat(rt2).isNotNull();
            assertThat(rt2.getValue()).isNotEqualTo(rt1.getValue());

            mockMvc.perform(post("/api/auth/refresh").cookie(rt1))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.name").value("JWT_ENTRY_POINT"));

            mockMvc.perform(post("/api/auth/refresh").cookie(rt2))
                    .andExpect(status().isOk());

            mockMvc.perform(authPost("/api/auth/logout", newAccessToken))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("3. 동일 사용자 재로그인")
    class ReloginSingleRow {

        @Test
        @DisplayName("TC-3. 재로그인 시 refresh_tokens row 1개만 유지, 구 token 무효화")
        void reloginKeepsSingleRow() throws Exception {
            String email = "relogin@test.com";
            registerStudent(email, "재로그인", 3, 1, 5);

            Cookie cookie1 = login(email);
            assertThat(refreshTokenRepository.count()).isEqualTo(1);

            Cookie cookie2 = login(email);
            assertThat(refreshTokenRepository.count()).isEqualTo(1);
            assertThat(cookie2.getValue()).isNotEqualTo(cookie1.getValue());

            mockMvc.perform(post("/api/auth/refresh").cookie(cookie1))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.name").value("JWT_ENTRY_POINT"));
        }
    }

    @Nested
    @DisplayName("4. 학부모 가입 → 자녀 링크 + 로그인")
    class ParentRegisterAndLogin {

        @Test
        @DisplayName("TC-4. parent_student row 생성 + BCrypt 해시 저장 + 학부모 로그인 시 children 노출")
        void parentRegisterLinksAndCanLogin() throws Exception {
            String childEmail = "child@test.com";
            String parentEmail = "parent@test.com";

            registerStudent(childEmail, "자녀", 1, 2, 3);
            registerStudent("noise1@test.com", "노이즈1", 2, 3, 4);
            registerStudent("noise2@test.com", "노이즈2", 3, 1, 5);

            User childUser = userRepository.findByEmail(childEmail).orElseThrow();
            StudentProfile childProfile = studentProfileRepository.findByUser(childUser).orElseThrow();

            mockMvc.perform(post("/api/auth/register/parent")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    ParentRegisterRequest.of(parentEmail, PASSWORD, PASSWORD, "엄마", childEmail))))
                    .andExpect(status().isCreated());

            User parentUser = userRepository.findByEmail(parentEmail).orElseThrow();
            List<ParentStudent> links = parentStudentRepository.findAllByParent(parentUser);
            assertThat(links).hasSize(1);
            assertThat(links.get(0).getStudent().getId()).isEqualTo(childProfile.getId());

            assertThat(parentUser.getPassword()).isNotEqualTo(PASSWORD);
            assertThat(passwordEncoder.matches(PASSWORD, parentUser.getPassword())).isTrue();

            mockMvc.perform(post("/api/auth/login/email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    EmailLoginRequest.of(parentEmail, PASSWORD))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("PARENT"))
                    .andExpect(jsonPath("$.children.length()").value(1))
                    .andExpect(jsonPath("$.children[0].studentId").value(childProfile.getId()))
                    .andExpect(jsonPath("$.children[0].name").value("자녀"));
        }
    }

    // ---------- helpers ----------

    private void registerStudent(String email, String name, int grade, int classNum, int number) throws Exception {
        StudentRegisterRequest request = StudentRegisterRequest.of(
                email, PASSWORD, PASSWORD, name, SCHOOL, grade, classNum, number);
        mockMvc.perform(post("/api/auth/register/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    private Cookie login(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                EmailLoginRequest.of(email, PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();
        Cookie cookie = result.getResponse().getCookie(REFRESH_COOKIE);
        if (cookie == null) {
            throw new IllegalStateException("로그인 응답에 refreshToken 쿠키 없음");
        }
        return cookie;
    }

    private String readAccessToken(MvcResult result) throws Exception {
        Map<?, ?> body = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        return (String) body.get("accessToken");
    }
}
