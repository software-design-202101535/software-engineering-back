package com.example.edumanager.global.security;

import com.example.edumanager.domain.user.entity.Role;
import com.example.edumanager.domain.user.entity.User;
import com.example.edumanager.domain.user.repository.UserRepository;
import com.example.edumanager.global.exception.ErrorCode;
import com.example.edumanager.global.security.exception.JwtAuthException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final UserRepository userRepository;
    private SecretKey key;
    private final String secret;
    private final long accessTokenExpiry;
    private final long refreshTokenExpiry;

    public JwtTokenProvider(
            UserRepository userRepository,
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiry}") long accessTokenExpiry,
            @Value("${jwt.refresh-token-expiry}") long refreshTokenExpiry) {
        this.userRepository = userRepository;
        this.secret = secret;
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId) {
        long now = new Date().getTime();
        Date validate = new Date(now + this.accessTokenExpiry);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .expiration(validate)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        long now = new Date().getTime();
        Date validate = new Date(now + this.refreshTokenExpiry);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .expiration(validate)
                .signWith(key)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Long userId = Long.valueOf(claims.getSubject());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new JwtAuthException(ErrorCode.USER_NOT_FOUND));

        if (user.isDeleted()) {
            throw new JwtAuthException(ErrorCode.USER_DELETED);
        }

        Role role = user.getRole();

        UserDetailsImpl userDetails = UserDetailsImpl.create(userId, role);

        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }

    public long getRefreshTokenExpiry() {
        return this.refreshTokenExpiry;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(this.key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException e) {
            throw new JwtAuthException(ErrorCode.JWT_SIGNATURE);
        } catch (MalformedJwtException e) {
            throw new JwtAuthException(ErrorCode.JWT_MALFORMED);
        } catch (ExpiredJwtException e) {
            throw new JwtAuthException(ErrorCode.JWT_ACCESS_TOKEN_EXPIRED);
        } catch (UnsupportedJwtException e) {
            throw new JwtAuthException(ErrorCode.JWT_UNSUPPORTED);
        } catch (IllegalArgumentException e) {
            throw new JwtAuthException(ErrorCode.JWT_NOT_VALID);
        }
    }

}
