package com.example.edumanager.global.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

/**
 * String 필드를 DB 저장 시 AES-GCM 으로 암호화, 조회 시 복호화한다.
 * 적용할 필드에 {@code @Convert(converter = EncryptedStringConverter.class)} 를 명시한다.
 */
@Component
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private final AesGcmCryptor cryptor;

    public EncryptedStringConverter(AesGcmCryptor cryptor) {
        this.cryptor = cryptor;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return cryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return cryptor.decrypt(dbData);
    }
}
