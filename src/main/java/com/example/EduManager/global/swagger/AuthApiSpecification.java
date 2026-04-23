package com.example.EduManager.global.swagger;

import com.example.EduManager.domain.auth.dto.*;
import com.example.EduManager.global.exception.ErrorResponse;
import com.example.EduManager.global.security.UserDetailsImpl;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "인증 API", description = "회원가입·로그인·토큰 재발급·로그아웃")
public interface AuthApiSpecification {

    @SecurityRequirements(value = {})
    @Operation(summary = "교사 회원가입", description = "담임 학년/반 정보를 포함해 교사 계정을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(
                    responseCode = "400", description = "입력값 오류 / 비밀번호 불일치 / 약관 미동의",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "입력값 오류", value = """
                                            {
                                                "code": 400,
                                                "name": "INVALID_INPUT_VALUE",
                                                "message": "잘못된 입력입니다.",
                                                "errors": {
                                                    "grade": "학년을 입력해주세요.",
                                                    "termsAgreed": "이용약관에 동의해주세요.",
                                                    "privacyAgreed": "개인정보처리방침에 동의해주세요."
                                                }
                                            }
                                            """),
                                    @ExampleObject(name = "비밀번호 불일치", value = """
                                            {
                                                "code": 400,
                                                "name": "PASSWORD_MISMATCH",
                                                "message": "비밀번호가 일치하지 않습니다."
                                            }
                                            """)
                            }
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
    ResponseEntity<Void> registerTeacher(@Valid @RequestBody TeacherRegisterRequest request);

    @SecurityRequirements(value = {})
    @Operation(summary = "학생 회원가입", description = "학년/반/번호 정보를 포함해 학생 계정을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(
                    responseCode = "400", description = "입력값 오류 / 비밀번호 불일치 / 약관 미동의",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "입력값 오류", value = """
                                            {
                                                "code": 400,
                                                "name": "INVALID_INPUT_VALUE",
                                                "message": "잘못된 입력입니다.",
                                                "errors": {
                                                    "number": "번호를 입력해주세요.",
                                                    "termsAgreed": "이용약관에 동의해주세요.",
                                                    "privacyAgreed": "개인정보처리방침에 동의해주세요."
                                                }
                                            }
                                            """),
                                    @ExampleObject(name = "비밀번호 불일치", value = """
                                            {
                                                "code": 400,
                                                "name": "PASSWORD_MISMATCH",
                                                "message": "비밀번호가 일치하지 않습니다."
                                            }
                                            """)
                            }
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
    ResponseEntity<Void> registerStudent(@Valid @RequestBody StudentRegisterRequest request);

    @SecurityRequirements(value = {})
    @Operation(summary = "학부모 회원가입", description = "자녀의 학교와 학번으로 자녀를 확인하고 연결합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(
                    responseCode = "400", description = "입력값 오류 / 비밀번호 불일치 / 약관 미동의",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "입력값 오류", value = """
                                            {
                                                "code": 400,
                                                "name": "INVALID_INPUT_VALUE",
                                                "message": "잘못된 입력입니다.",
                                                "errors": {
                                                    "childSchoolNumber": "자녀의 학번을 입력해주세요.",
                                                    "termsAgreed": "이용약관에 동의해주세요.",
                                                    "privacyAgreed": "개인정보처리방침에 동의해주세요."
                                                }
                                            }
                                            """),
                                    @ExampleObject(name = "비밀번호 불일치", value = """
                                            {
                                                "code": 400,
                                                "name": "PASSWORD_MISMATCH",
                                                "message": "비밀번호가 일치하지 않습니다."
                                            }
                                            """)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "자녀 학생을 찾을 수 없음",
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
    ResponseEntity<Void> registerParent(@Valid @RequestBody ParentRegisterRequest request);

    @SecurityRequirements(value = {})
    @Operation(summary = "교사·학생 로그인", description = "학교 + 사번/학번으로 로그인합니다. Refresh Token은 HttpOnly 쿠키(refreshToken)로 발급됩니다. 학생 로그인 시 studentId가 포함됩니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "로그인 성공 (교사: studentId 없음 / 학생: studentId 포함)",
                    content = @Content(
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = {
                                    @ExampleObject(name = "교사 로그인", value = """
                                            {
                                                "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                                                "userId": 1,
                                                "email": "teacher@school.com",
                                                "name": "김교사",
                                                "role": "TEACHER",
                                                "grade": 2,
                                                "classNum": 3
                                            }
                                            """),
                                    @ExampleObject(name = "학생 로그인", value = """
                                            {
                                                "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                                                "userId": 3,
                                                "email": "student@school.com",
                                                "name": "이학생",
                                                "role": "STUDENT",
                                                "studentId": 5
                                            }
                                            """)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "학교/사번 또는 비밀번호 불일치",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                        "code": 401,
                                        "name": "BAD_CREDENTIALS",
                                        "message": "이메일 또는 비밀번호가 올바르지 않습니다."
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "410", description = "삭제된 계정",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                        "code": 410,
                                        "name": "USER_DELETED",
                                        "message": "삭제된 사용자입니다."
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<LoginResponse> loginBySchool(@Valid @RequestBody SchoolLoginRequest request,
                                                HttpServletResponse response);

    @SecurityRequirements(value = {})
    @Operation(summary = "학부모 로그인", description = "이메일로 로그인합니다. Refresh Token은 HttpOnly 쿠키(refreshToken)로 발급됩니다. 연결된 자녀 목록(studentId, 이름)이 포함됩니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "로그인 성공",
                    content = @Content(
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject("""
                                    {
                                        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                                        "userId": 3,
                                        "email": "parent@email.com",
                                        "name": "박학부모",
                                        "role": "PARENT",
                                        "children": [
                                            { "studentId": 5, "name": "박자녀" },
                                            { "studentId": 7, "name": "박둘째" }
                                        ]
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "이메일 또는 비밀번호 불일치",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                        "code": 401,
                                        "name": "BAD_CREDENTIALS",
                                        "message": "이메일 또는 비밀번호가 올바르지 않습니다."
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "410", description = "삭제된 계정",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                        "code": 410,
                                        "name": "USER_DELETED",
                                        "message": "삭제된 사용자입니다."
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<LoginResponse> loginByEmail(@Valid @RequestBody EmailLoginRequest request,
                                               HttpServletResponse response);

    @SecurityRequirements(value = {})
    @Operation(summary = "Access Token 재발급", description = "HttpOnly 쿠키(refreshToken)로 Access Token을 재발급합니다. 새 Refresh Token도 쿠키로 발급됩니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "재발급 성공",
                    content = @Content(
                            schema = @Schema(implementation = TokenRefreshResponse.class),
                            examples = @ExampleObject("""
                                    {
                                        "accessToken": "eyJhbGciOiJIUzI1NiJ9..."
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "Refresh Token 없음 또는 만료",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "Refresh Token 없음", value = """
                                            {
                                                "code": 401,
                                                "name": "JWT_REFRESH_TOKEN_NOT_FOUND",
                                                "message": "Refresh Token을 찾을 수 없습니다."
                                            }
                                            """),
                                    @ExampleObject(name = "Refresh Token 만료", value = """
                                            {
                                                "code": 401,
                                                "name": "JWT_REFRESH_TOKEN_EXPIRED",
                                                "message": "Refresh Token이 만료되었습니다. 다시 로그인해주세요."
                                            }
                                            """)
                            }
                    )
            )
    })
    ResponseEntity<TokenRefreshResponse> refresh(@CookieValue(name = "refreshToken") String refreshToken,
                                                 HttpServletResponse response);

    @Operation(summary = "로그아웃", description = "Refresh Token을 무효화하고 쿠키를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(
                    responseCode = "401", description = "인증 필요",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                        "code": 401,
                                        "name": "JWT_ENTRY_POINT",
                                        "message": "로그인이 필요합니다."
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<Void> logout(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                HttpServletResponse response);
}
