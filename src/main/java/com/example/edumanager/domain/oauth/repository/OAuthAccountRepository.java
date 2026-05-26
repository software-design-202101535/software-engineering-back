package com.example.edumanager.domain.oauth.repository;

import com.example.edumanager.domain.oauth.entity.OAuthAccount;
import com.example.edumanager.domain.oauth.entity.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {

    Optional<OAuthAccount> findByProviderAndOauthId(OAuthProvider provider, String oauthId);

    @Query("SELECT a.user.id FROM OAuthAccount a WHERE a.provider = :provider AND a.oauthId = :oauthId")
    Optional<Long> findUserIdByProviderAndOauthId(@Param("provider") OAuthProvider provider,
                                                    @Param("oauthId") String oauthId);
}
