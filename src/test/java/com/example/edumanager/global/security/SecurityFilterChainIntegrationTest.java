package com.example.edumanager.global.security;

import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.support.AbstractIntegrationTest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("SecurityFilterChain 통합 테스트")
class SecurityFilterChainIntegrationTest extends AbstractIntegrationTest {

    private static final String PROTECTED_ENDPOINT = "/api/students/1";

    @Nested
    @DisplayName("1. permitAll 라우팅 (토큰 없이 통과)")
    class PermitAll {

        @Test
        @DisplayName("TC-1. POST /api/auth/login/email 빈 body → 인증 통과 후 컨트롤러 도달 → 400")
        void loginEndpointBypassesAuth() throws Exception {
            mockMvc.perform(post("/api/auth/login/email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC-2. POST /api/auth/register/teacher 빈 body → 인증 통과 후 컨트롤러 도달 → 400")
        void registerEndpointBypassesAuth() throws Exception {
            mockMvc.perform(post("/api/auth/register/teacher")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC-3. POST /api/auth/refresh 쿠키 없이 → 인증 통과 후 컨트롤러 도달 (4xx)")
        void refreshEndpointBypassesAuth() throws Exception {
            mockMvc.perform(post("/api/auth/refresh"))
                    .andExpect(result -> {
                        int s = result.getResponse().getStatus();
                        if (s < 400 || s >= 500) {
                            throw new AssertionError("expected 4xx but was " + s);
                        }
                    });
        }

        @Test
        @DisplayName("TC-4. GET /v3/api-docs 토큰 없이 → 200 (springdoc 핸들러 도달)")
        void springdocBypassesAuth() throws Exception {
            mockMvc.perform(get("/v3/api-docs"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("2. 정상 토큰 → 인증 통과")
    class ValidToken {

        @Test
        @DisplayName("TC-5. 정상 access token + POST /api/auth/logout → 200")
        void validTokenPassesAuth() throws Exception {
            User user = insertUser("valid@test.com", "password1!", Role.TEACHER);
            String token = issueAccessToken(user.getId());

            mockMvc.perform(authPost("/api/auth/logout", token))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("3. 토큰 검증 실패 (각 ErrorCode 분기)")
    class TokenValidationFailure {

        @Test
        @DisplayName("TC-6. 잘못된 서명 토큰 → 401 + JWT_SIGNATURE (응답 풀필드 검증)")
        void wrongSignature() throws Exception {
            SecretKey wrongKey = Keys.hmacShaKeyFor(
                    "wrong-secret-key-must-be-at-least-256-bits-long-9876543210".getBytes(StandardCharsets.UTF_8));
            String wrongToken = Jwts.builder()
                    .subject("1")
                    .expiration(new Date(System.currentTimeMillis() + 60000))
                    .signWith(wrongKey)
                    .compact();

            mockMvc.perform(get(PROTECTED_ENDPOINT).header(HttpHeaders.AUTHORIZATION, bearer(wrongToken)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.name").value("JWT_SIGNATURE"))
                    .andExpect(jsonPath("$.code").value(401))
                    .andExpect(jsonPath("$.message").value("인증에 실패했습니다. 다시 로그인해주세요."));
        }

        @Test
        @DisplayName("TC-7. Malformed 토큰 → 401 + JWT_MALFORMED")
        void malformedToken() throws Exception {
            mockMvc.perform(get(PROTECTED_ENDPOINT).header(HttpHeaders.AUTHORIZATION, bearer("abc.def")))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.name").value("JWT_MALFORMED"));
        }

        @Test
        @DisplayName("TC-8. 만료된 access token → 401 + JWT_ACCESS_TOKEN_EXPIRED")
        void expiredAccessToken() throws Exception {
            User user = insertUser("expired@test.com", "password1!", Role.TEACHER);
            String expiredToken = issueExpiredAccessToken(user.getId());

            mockMvc.perform(get(PROTECTED_ENDPOINT).header(HttpHeaders.AUTHORIZATION, bearer(expiredToken)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.name").value("JWT_ACCESS_TOKEN_EXPIRED"));
        }

        @Test
        @DisplayName("TC-9. unsigned JWT (alg=none) → 401 + JWT_UNSUPPORTED")
        void unsupportedToken() throws Exception {
            String unsignedHeader = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
            String unsignedPayload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString("{\"sub\":\"1\",\"exp\":9999999999}".getBytes(StandardCharsets.UTF_8));
            String unsignedToken = unsignedHeader + "." + unsignedPayload + ".";

            mockMvc.perform(get(PROTECTED_ENDPOINT).header(HttpHeaders.AUTHORIZATION, bearer(unsignedToken)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.name").value("JWT_UNSUPPORTED"));
        }

        @Test
        @DisplayName("TC-10. Bearer 뒤 빈 토큰 → 401 + JWT_NOT_VALID (IllegalArgumentException 분기)")
        void emptyTokenAfterBearer() throws Exception {
            mockMvc.perform(get(PROTECTED_ENDPOINT).header(HttpHeaders.AUTHORIZATION, "Bearer "))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.name").value("JWT_NOT_VALID"));
        }
    }

    @Nested
    @DisplayName("4. 토큰 검증 통과 후 user 단계 실패")
    class UserResolutionFailure {

        @Test
        @DisplayName("TC-11. 유효 토큰인데 user DB에 없음 → 404 + USER_NOT_FOUND")
        void userNotFound() throws Exception {
            User user = insertUser("ghost@test.com", "password1!", Role.TEACHER);
            String token = issueAccessToken(user.getId());
            userRepository.delete(user);

            mockMvc.perform(get(PROTECTED_ENDPOINT).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.name").value("USER_NOT_FOUND"));
        }

        @Test
        @DisplayName("TC-12. user soft-deleted → 410 + USER_DELETED")
        void userSoftDeleted() throws Exception {
            User user = insertUser("deleted@test.com", "password1!", Role.TEACHER);
            String token = issueAccessToken(user.getId());
            user.delete();
            userRepository.save(user);

            mockMvc.perform(get(PROTECTED_ENDPOINT).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                    .andExpect(status().isGone())
                    .andExpect(jsonPath("$.name").value("USER_DELETED"));
        }
    }

    @Nested
    @DisplayName("5. Authorization 헤더 분기")
    class AuthorizationHeaderBranches {

        @Test
        @DisplayName("TC-13. Authorization 헤더 자체 없음 → 익명요청 → 401 + JWT_ENTRY_POINT (filter null 체크 분기)")
        void noAuthorizationHeader() throws Exception {
            mockMvc.perform(get(PROTECTED_ENDPOINT))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.name").value("JWT_ENTRY_POINT"));
        }

        @Test
        @DisplayName("TC-14. Authorization 헤더는 있지만 Bearer prefix 없음 → 익명요청 → 401 + JWT_ENTRY_POINT (filter !startsWith 분기)")
        void noBearerPrefix() throws Exception {
            mockMvc.perform(get(PROTECTED_ENDPOINT).header(HttpHeaders.AUTHORIZATION, "abc.def"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.name").value("JWT_ENTRY_POINT"));
        }
    }
}
