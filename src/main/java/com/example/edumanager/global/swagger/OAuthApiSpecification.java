package com.example.edumanager.global.swagger;

import com.example.edumanager.domain.auth.dto.LoginResponse;
import com.example.edumanager.domain.oauth.dto.OAuthLoginRequest;
import com.example.edumanager.domain.oauth.dto.OAuthLoginResponse;
import com.example.edumanager.domain.oauth.dto.OAuthRegisterRequest;
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

@Tag(name = "OAuth 인증 API", description = "카카오 OAuth 로그인/회원가입 (프론트가 카카오 콜백 직접 받음)")
public interface OAuthApiSpecification {

    @SecurityRequirements(value = {})
    @Operation(summary = "카카오 로그인/식별",
            description = "프론트가 카카오 콜백에서 받은 code 를 전달. 기존 유저면 토큰 발급, 신규면 tempToken(5분 TTL) 발급.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "기존 유저 토큰 발급 또는 신규 유저 tempToken 발급",
                    content = @Content(
                            schema = @Schema(implementation = OAuthLoginResponse.class),
                            examples = {
                                    @ExampleObject(name = "기존 유저", value = """
                                            {
                                                "isNewUser": false,
                                                "loginData": {
                                                    "accessToken": "eyJ...",
                                                    "userId": 1,
                                                    "email": "user@kakao.com",
                                                    "name": "홍길동",
                                                    "role": "TEACHER",
                                                    "grade": 1,
                                                    "classNum": 3
                                                }
                                            }
                                            """),
                                    @ExampleObject(name = "신규 유저", value = """
                                            {
                                                "isNewUser": true,
                                                "tempToken": "eyJ...",
                                                "email": "user@kakao.com",
                                                "name": "홍길동"
                                            }
                                            """)
                            }
                    )
            ),
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
    ResponseEntity<OAuthLoginResponse> kakaoLogin(@Valid @RequestBody OAuthLoginRequest request,
                                                   HttpServletResponse response);

    @SecurityRequirements(value = {})
    @Operation(summary = "OAuth 회원가입 완료 (신규 유저)",
            description = "tempToken + 역할별 추가 정보로 가입 완료. refreshToken 은 HttpOnly 쿠키.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "가입 + 토큰 발급",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "tempToken 무효 / 역할 정보 누락 / 이메일 누락",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "tempToken 무효", value = """
                                            {
                                                "code": 400,
                                                "name": "INVALID_TEMP_TOKEN",
                                                "message": "유효하지 않거나 만료된 임시 토큰입니다."
                                            }
                                            """),
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
    ResponseEntity<LoginResponse> kakaoRegister(@Valid @RequestBody OAuthRegisterRequest request,
                                                 HttpServletResponse response);
}
