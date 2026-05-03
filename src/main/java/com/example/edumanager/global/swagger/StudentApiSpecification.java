package com.example.edumanager.global.swagger;

import com.example.edumanager.domain.student.dto.StudentDetailResponse;
import com.example.edumanager.domain.student.dto.StudentSummaryResponse;
import com.example.edumanager.domain.student.dto.UpdateStudentRequest;
import com.example.edumanager.global.exception.ErrorResponse;
import com.example.edumanager.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "학생 API", description = "학생 목록 조회, 상세 조회, 기본 정보 수정")
public interface StudentApiSpecification {

    @Operation(
            summary = "담당 학급 학생 목록 조회",
            description = """
                    로그인한 교사와 같은 학교·학년·반 학생 목록을 번호 오름차순으로 반환합니다.
                    TEACHER 역할만 호출 가능합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = StudentSummaryResponse.class),
                            examples = @ExampleObject("""
                                    [
                                      {
                                        "id": 1,
                                        "name": "홍길동",
                                        "grade": 2,
                                        "classNum": 3,
                                        "number": 1
                                      }
                                    ]
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "403", description = "TEACHER 역할이 아닌 경우",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "code": 403,
                                      "name": "STUDENT_ACCESS_DENIED",
                                      "message": "학생 정보에 접근할 권한이 없습니다."
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "교사 프로필을 찾을 수 없는 경우",
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
            )
    })
    ResponseEntity<List<StudentSummaryResponse>> getClassStudents(
            @AuthenticationPrincipal UserDetailsImpl userDetails);

    @Operation(
            summary = "학생 상세 조회",
            description = """
                    학생의 기본 정보(이름, 학년, 반, 번호, 생년월일, 연락처, 주소)를 반환합니다.
                    담임교사 또는 ADMIN만 호출 가능합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = StudentDetailResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "id": 1,
                                      "name": "김민준",
                                      "grade": 1,
                                      "classNum": 1,
                                      "number": 1,
                                      "birthDate": "2009-05-12",
                                      "phone": "010-1111-2222",
                                      "parentPhone": "010-3333-4444",
                                      "address": "서울시 종로구 창경궁로 123"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "403", description = "담임교사가 아닌 경우",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "code": 403,
                                      "name": "STUDENT_ACCESS_DENIED",
                                      "message": "학생 정보에 접근할 권한이 없습니다."
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "학생을 찾을 수 없는 경우",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "code": 404,
                                      "name": "STUDENT_NOT_FOUND",
                                      "message": "해당 학생을 찾을 수 없습니다."
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<StudentDetailResponse> getStudentDetail(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails);

    @Operation(
            summary = "학생 기본 정보 수정",
            description = """
                    학생의 이름, 생년월일, 연락처, 주소를 수정합니다.
                    학년·반·번호는 수정할 수 없습니다 (학적 변경은 별도 프로세스).
                    담임교사 또는 ADMIN만 호출 가능합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "수정 성공",
                    content = @Content(
                            schema = @Schema(implementation = StudentDetailResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "id": 1,
                                      "name": "김민준",
                                      "grade": 1,
                                      "classNum": 1,
                                      "number": 1,
                                      "birthDate": "2009-05-12",
                                      "phone": "010-1111-2222",
                                      "parentPhone": "010-3333-4444",
                                      "address": "서울시 종로구 창경궁로 123"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400", description = "이름이 비어있는 경우",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "code": 400,
                                      "name": "INVALID_INPUT_VALUE",
                                      "message": "잘못된 입력입니다.",
                                      "errors": {
                                        "name": "이름을 입력해주세요."
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "403", description = "담임교사가 아닌 경우",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "code": 403,
                                      "name": "STUDENT_ACCESS_DENIED",
                                      "message": "학생 정보에 접근할 권한이 없습니다."
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "학생을 찾을 수 없는 경우",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "code": 404,
                                      "name": "STUDENT_NOT_FOUND",
                                      "message": "해당 학생을 찾을 수 없습니다."
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<StudentDetailResponse> updateStudentDetail(
            @PathVariable Long studentId,
            @RequestBody @Valid UpdateStudentRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails);
}
