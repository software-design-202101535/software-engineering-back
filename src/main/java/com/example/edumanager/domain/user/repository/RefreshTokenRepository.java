package com.example.edumanager.domain.user.repository;

import com.example.edumanager.domain.user.entity.RefreshToken;
import com.example.edumanager.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("delete from RefreshToken r where r.user = :user")
    void deleteByUser(@Param("user") User user);
}
