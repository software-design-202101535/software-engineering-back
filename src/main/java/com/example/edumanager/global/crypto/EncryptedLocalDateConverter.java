package com.example.edumanager.global.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * LocalDate 필드를 ISO-8601 문자열로 직렬화한 뒤 AES-GCM 으로 암호화한다.
 * 암호화하면 컬럼이 문자열이 되므로 날짜 범위 쿼리에 쓰지 않는 필드에만 적용한다.
 */
@Component
@Converter
public class EncryptedLocalDateConverter implements AttributeConverter<LocalDate, String> {

    private final AesGcmCryptor cryptor;

    public EncryptedLocalDateConverter(AesGcmCryptor cryptor) {
        this.cryptor = cryptor;
    }

    @Override
    public String convertToDatabaseColumn(LocalDate attribute) {
        if (attribute == null) {
            return null;
        }
        return cryptor.encrypt(attribute.toString());
    }

    @Override
    public LocalDate convertToEntityAttribute(String dbData) {
        String decrypted = cryptor.decrypt(dbData);
        return decrypted == null ? null : LocalDate.parse(decrypted);
    }
}
