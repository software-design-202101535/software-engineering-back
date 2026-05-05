package com.example.edumanager.support;

import com.example.edumanager.domain.student.entity.StudentProfile;
import com.example.edumanager.domain.student.repository.StudentProfileRepository;
import com.example.edumanager.domain.teacher.entity.TeacherProfile;
import com.example.edumanager.domain.teacher.repository.TeacherProfileRepository;
import com.example.edumanager.domain.user.entity.RefreshToken;
import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.domain.user.entity.School;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.domain.user.repository.RefreshTokenRepository;
import com.example.edumanager.domain.user.repository.UserRepository;
import com.example.edumanager.global.security.JwtTokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected JdbcTemplate jdbcTemplate;
    @Autowired protected JwtTokenProvider jwtTokenProvider;
    @Autowired protected UserRepository userRepository;
    @Autowired protected RefreshTokenRepository refreshTokenRepository;
    @Autowired protected StudentProfileRepository studentProfileRepository;
    @Autowired protected TeacherProfileRepository teacherProfileRepository;
    @Autowired protected PasswordEncoder passwordEncoder;

    @PersistenceContext private EntityManager em;

    @Value("${jwt.secret}") private String jwtSecret;

    @AfterEach
    void truncateAllTables() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        em.getMetamodel().getEntities().stream()
                .map(e -> e.getJavaType().getAnnotation(jakarta.persistence.Table.class))
                .filter(Objects::nonNull)
                .map(jakarta.persistence.Table::name)
                .distinct()
                .forEach(t -> jdbcTemplate.execute("TRUNCATE TABLE " + t));
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    // ---------- 사용자/프로필 헬퍼 ----------

    protected User insertUser(String email, String rawPassword, String name, Role role) {
        return userRepository.save(User.of(email, passwordEncoder.encode(rawPassword), name, role));
    }

    protected User insertUser(String email, String rawPassword, Role role) {
        return insertUser(email, rawPassword, "테스트_" + role.name(), role);
    }

    protected StudentProfile insertStudent(String email, School school, int grade, int classNum, int number) {
        User user = insertUser(email, "password1!", "학생_" + email, Role.STUDENT);
        return studentProfileRepository.save(StudentProfile.of(user, school, grade, classNum, number));
    }

    protected TeacherProfile insertHomeroomTeacher(String email, School school, int grade, int classNum) {
        User user = insertUser(email, "password1!", "교사_" + email, Role.TEACHER);
        return teacherProfileRepository.save(TeacherProfile.of(user, school, grade, classNum));
    }

    // ---------- JWT 헬퍼 ----------

    protected String issueAccessToken(Long userId) {
        return jwtTokenProvider.createAccessToken(userId);
    }

    // JwtTokenProvider.createAccessToken 우회 — 시간 조작 없이 ExpiredJwtException 분기만 직접 트리거
    protected String issueExpiredAccessToken(Long userId) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(key)
                .compact();
    }

    protected String issueAndStoreRefreshToken(User user) {
        String token = jwtTokenProvider.createRefreshToken(user.getId());
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpiry() / 1000);
        refreshTokenRepository.save(RefreshToken.of(user, token, expiresAt));
        return token;
    }

    protected String issueExpiredRefreshToken(User user) {
        String token = jwtTokenProvider.createRefreshToken(user.getId());
        refreshTokenRepository.save(RefreshToken.of(user, token, LocalDateTime.now().minusDays(1)));
        return token;
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }

    // ---------- MockMvc 인증 단축 헬퍼 ----------

    protected MockHttpServletRequestBuilder authGet(String url, String accessToken) {
        return get(url).header(HttpHeaders.AUTHORIZATION, bearer(accessToken));
    }

    protected MockHttpServletRequestBuilder authPost(String url, String accessToken) {
        return post(url).header(HttpHeaders.AUTHORIZATION, bearer(accessToken));
    }

    protected MockHttpServletRequestBuilder authPostJson(String url, String accessToken, Object body) throws Exception {
        return post(url)
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }
}
