package com.example.edumanager.global.crypto;

import com.example.edumanager.global.exception.CustomException;
import com.example.edumanager.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AesGcmCryptor 단위 테스트")
class AesGcmCryptorTest {

    private final AesGcmCryptor cryptor =
            new AesGcmCryptor("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));

    static Stream<String> plainTexts() {
        return Stream.of(
                "홍길동 서울특별시 강남구 테헤란로 123",   // 한글
                "Hello, World! 010-1234-5678",            // 영문+숫자
                "",                                        // 빈 문자열
                "a".repeat(1000)                           // 긴 문자열
        );
    }

    @DisplayName("TC-1. 암호화 후 복호화하면 원문이 복원된다")
    @ParameterizedTest
    @MethodSource("plainTexts")
    void encryptThenDecryptRestoresOriginal(String plainText) {
        String encrypted = cryptor.encrypt(plainText);

        assertThat(encrypted).isNotEqualTo(plainText);
        assertThat(cryptor.decrypt(encrypted)).isEqualTo(plainText);
    }

    @DisplayName("TC-2. 같은 평문도 매번 다른 암호문이 되고(랜덤 IV) 둘 다 같은 평문으로 복호화된다")
    @Test
    void sameTextProducesDifferentCipherText() {
        String plainText = "동일한 평문";

        String first = cryptor.encrypt(plainText);
        String second = cryptor.encrypt(plainText);

        assertThat(first).isNotEqualTo(second);
        assertThat(cryptor.decrypt(first)).isEqualTo(plainText);
        assertThat(cryptor.decrypt(second)).isEqualTo(plainText);
    }

    @DisplayName("TC-3. null 은 암복호화 모두 null 을 반환한다")
    @Test
    void nullReturnsNull() {
        assertThat(cryptor.encrypt(null)).isNull();
        assertThat(cryptor.decrypt(null)).isNull();
    }

    @DisplayName("TC-4. 변조된 암호문은 복호화 시 ENCRYPTION_ERROR 예외가 발생한다")
    @Test
    void tamperedCipherTextThrows() {
        String encrypted = cryptor.encrypt("민감정보");
        byte[] raw = Base64.getDecoder().decode(encrypted);
        raw[raw.length - 1] ^= 0x01;                       // 마지막 바이트(인증 태그) 변조
        String tampered = Base64.getEncoder().encodeToString(raw);

        assertThatThrownBy(() -> cryptor.decrypt(tampered))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENCRYPTION_ERROR);
    }
}
