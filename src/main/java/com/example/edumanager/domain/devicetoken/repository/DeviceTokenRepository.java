package com.example.edumanager.domain.devicetoken.repository;

import com.example.edumanager.domain.devicetoken.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByToken(String token);

    List<DeviceToken> findAllByUserIdIn(List<Long> userIds);

    void deleteByUserIdAndToken(Long userId, String token);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM DeviceToken d WHERE d.token IN :tokens")
    void deleteAllByTokenIn(@Param("tokens") List<String> tokens);
}
