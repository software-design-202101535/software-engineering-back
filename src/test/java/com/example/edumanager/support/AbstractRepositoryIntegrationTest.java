package com.example.edumanager.support;

import com.example.edumanager.global.config.JpaAuditingConfig;
import com.example.edumanager.global.crypto.EncryptedLocalDateConverter;
import com.example.edumanager.global.crypto.EncryptedStringConverter;
import com.example.edumanager.global.crypto.EncryptionConfig;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({JpaAuditingConfig.class, EncryptionConfig.class,
        EncryptedStringConverter.class, EncryptedLocalDateConverter.class})
public abstract class AbstractRepositoryIntegrationTest {
}
