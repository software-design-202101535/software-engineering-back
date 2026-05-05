package com.example.edumanager.support;

import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.global.security.UserDetailsImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("통합테스트 인프라 스모크")
class IntegrationSmokeTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("TC-S-1. 인증필요 경로 토큰 없으면 401 + JWT_ENTRY_POINT (SecurityFilterChain → EntryPoint → ErrorResponse 직렬화 라우팅 OK)")
    void securityFilterChainProtectsAuthenticatedEndpoint() throws Exception {
        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.name").value("JWT_ENTRY_POINT"));
    }

    @Test
    @DisplayName("TC-S-2. issueAccessToken 토큰으로 SecurityContext 인증 가능 (Commit 3~5 의 인증 전제 보장)")
    void issueAccessTokenHelperEnablesAuthentication() {
        User user = insertUser("smoke@test.com", "password1!", Role.TEACHER);

        String token = issueAccessToken(user.getId());
        Authentication auth = jwtTokenProvider.getAuthentication(token);
        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();

        assertAll(
                () -> assertThat(principal.getUserId()).isEqualTo(user.getId()),
                () -> assertThat(principal.getRole()).isEqualTo(Role.TEACHER)
        );
    }
}
