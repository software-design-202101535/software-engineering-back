package com.example.edumanager.domain.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class ParentRegisterRequest {

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String passwordConfirm;

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    @NotEmpty(message = "자녀의 이메일을 한 명 이상 입력해주세요.")
    private List<@Email(message = "올바른 이메일 형식이 아닙니다.")
                 @NotBlank(message = "자녀의 이메일을 입력해주세요.") String> childEmails;

    @AssertTrue(message = "이용약관에 동의해주세요.")
    private boolean termsAgreed;

    @AssertTrue(message = "개인정보처리방침에 동의해주세요.")
    private boolean privacyAgreed;

    public static ParentRegisterRequest of(String email, String password, String passwordConfirm,
                                            String name, List<String> childEmails) {
        ParentRegisterRequest request = new ParentRegisterRequest();
        request.email = email;
        request.password = password;
        request.passwordConfirm = passwordConfirm;
        request.name = name;
        request.childEmails = childEmails;
        request.termsAgreed = true;
        request.privacyAgreed = true;
        return request;
    }
}
