package com.example.edumanager.global.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

/**
 * {@code encryption.key}(Base64 인코딩된 256-bit 키)로 암복호화기 빈을 만든다.
 * 컨버터들이 이 빈을 주입받아 사용한다.
 */
@Configuration
public class EncryptionConfig {

    @Bean
    public AesGcmCryptor aesGcmCryptor(@Value("${encryption.key}") String base64Key) {
        return new AesGcmCryptor(Base64.getDecoder().decode(base64Key));
    }
}
