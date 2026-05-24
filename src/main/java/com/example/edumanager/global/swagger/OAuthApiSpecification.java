package com.example.edumanager.global.swagger;

import com.example.edumanager.domain.auth.dto.LoginResponse;
import com.example.edumanager.domain.oauth.dto.OAuthCompleteRequest;
import com.example.edumanager.domain.oauth.dto.OAuthTokenRequest;
import com.example.edumanager.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "OAuth 인증 API", description = "카카오 OAuth 로그인/회원가입")
public interface OAuthApiSpecification {

    @SecurityRequirements(value = {})
    @Operation(summary = "카카오 인가 페이지로 리다이렉트", description = "카카오 로그인 페이지로 302 리다이렉트합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "카카오 인가 페이지로 리다이렉트")
    })
    ResponseEntity<Void> authorizeKakao();

    @SecurityRequirements(value = {})
    @Operation(summary = "카카오 콜백 처리",
            description = "카카오가 호출하는 콜백. 단기 authCode(30초 TTL, 1회용)를 발급하여 프론트의 /oauth/result 로 302 리다이렉트합니다. 기존 유저는 ?authCode=xxx, 신규 유저는 ?authCode=xxx&needsInfo=true&email=...&name=... 로 전달.")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "프론트로 리다이렉트"),
            @ApiResponse(
                    responseCode = "502", description = "카카오 통신 실패",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                        "code": 502,
                                        "name": "OAUTH_PROVIDER_ERROR",
                                        "message": "OAuth 제공자와의 통신에 실패했습니다."
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<Void> callbackKakao(@RequestParam("code") String code);

    @SecurityRequirements(value = {})
    @Operation(summary = "OAuth 토큰 교환 (기존 유저)",
            description = "콜백에서 받은 authCode 를 access/refresh 토큰으로 교환합니다. refreshToken 은 HttpOnly 쿠키로 발급.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "토큰 발급 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "authCode 무효/만료 또는 신규 유저(complete 필요)",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "authCode 무효", value = """
                                            {
                                                "code": 400,
                                                "name": "OAUTH_INVALID_AUTHCODE",
                                                "message": "유효하지 않거나 만료된 인증 코드입니다."
                                            }
                                            """),
                                    @ExampleObject(name = "신규 유저", value = """
                                            {
                                                "code": 400,
                                                "name": "OAUTH_PENDING_NOT_EXISTING_USER",
                                                "message": "추가 정보 입력이 필요한 사용자입니다."
                                            }
                                            """)
                            }
                    )
            )
    })
    ResponseEntity<LoginResponse> token(@Valid @RequestBody OAuthTokenRequest request,
                                        HttpServletResponse response);

    @SecurityRequirements(value = {})
    @Operation(summary = "OAuth 회원가입 완료 (신규 유저)",
            description = "역할(TEACHER/STUDENT/PARENT)과 역할별 추가 정보를 입력하여 가입을 완료합니다. refreshToken 은 HttpOnly 쿠키로 발급.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "가입 + 토큰 발급 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "입력 오류 / 약관 미동의 / 역할 정보 누락 / authCode 무효 / 기존 유저",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "역할 정보 누락", value = """
                                            {
                                                "code": 400,
                                                "name": "OAUTH_ROLE_INFO_REQUIRED",
                                                "message": "선택한 역할에 필요한 정보가 누락되었습니다."
                                            }
                                            """),
                                    @ExampleObject(name = "이메일 누락", value = """
                                            {
                                                "code": 400,
                                                "name": "OAUTH_EMAIL_REQUIRED",
                                                "message": "이메일을 입력해주세요."
                                            }
                                            """),
                                    @ExampleObject(name = "기존 유저", value = """
                                            {
                                                "code": 400,
                                                "name": "OAUTH_PENDING_NOT_NEW_USER",
                                                "message": "이미 가입된 사용자입니다. 토큰 교환을 사용해주세요."
                                            }
                                            """)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "자녀 학생을 찾을 수 없음 (PARENT 역할)",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                        "code": 404,
                                        "name": "USER_NOT_FOUND",
                                        "message": "해당 유저를 찾을 수 없습니다."
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "409", description = "이메일 중복",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                        "code": 409,
                                        "name": "DUPLICATED_USER",
                                        "message": "이미 존재하는 유저입니다."
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<LoginResponse> complete(@Valid @RequestBody OAuthCompleteRequest request,
                                            HttpServletResponse response);
}
