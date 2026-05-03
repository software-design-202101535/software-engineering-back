package com.example.edumanager.global.swagger;

import com.example.edumanager.domain.grade.dto.BatchGradeRequest;
import com.example.edumanager.domain.grade.dto.GradeResponse;
import com.example.edumanager.domain.grade.entity.ExamType;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "성적 API", description = "성적 조회 및 일괄 등록/수정/삭제")
public interface GradeApiSpecification {

    @Operation(summary = "성적 조회", description = "학생의 특정 학기/시험유형 성적 목록을 조회합니다. ADMIN·담임교사·본인 학생·연결된 학부모만 접근 가능합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = GradeResponse.class),
                            examples = @ExampleObject("""
                                    [
                                      {
                                        "id": 1,
                                        "subject": "MATH",
                                        "score": 95,
                                        "grade": "A"
                                      },
                                      {
                                        "id": 2,
                                        "subject": "ENGLISH",
                                        "score": null,
                                        "grade": null
                                      }
                                    ]
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400", description = "필수 파라미터 누락",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "code": 400,
                                      "name": "MISSING_PARAMETER",
                                      "message": "필수 요청 인자가 누락되었습니다."
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "403", description = "접근 권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "code": 403,
                                      "name": "GRADE_ACCESS_DENIED",
                                      "message": "해당 성적에 접근할 권한이 없습니다."
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "학생 없음",
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
    ResponseEntity<List<GradeResponse>> getGrades(
            @PathVariable Long studentId,
            @RequestParam String semester,
            @RequestParam ExamType examType,
            @AuthenticationPrincipal UserDetailsImpl userDetails);

    @Operation(summary = "성적 일괄 처리", description = """
            성적 추가/수정/삭제를 하나의 요청으로 처리합니다. ADMIN·담임교사만 가능합니다.

            PUT 방식으로 동작하므로 프론트엔드는 유지할 항목을 포함한 전체 상태를 전송해야 합니다.
            - create: 새로 추가할 성적 목록
            - update: 수정할 성적 목록 (id 필수, 기존 값도 변경 없이 그대로 전송)
            - delete: 삭제할 성적 id 목록
            score는 null 허용 — null로 전송하면 점수 없음 상태로 저장됩니다.
            """)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "처리 성공 — 해당 학기/시험유형의 최종 성적 목록 반환",
                    content = @Content(
                            schema = @Schema(implementation = GradeResponse.class),
                            examples = @ExampleObject("""
                                    [
                                      {
                                        "id": 1,
                                        "subject": "MATH",
                                        "score": 90,
                                        "grade": "A"
                                      },
                                      {
                                        "id": 4,
                                        "subject": "SCIENCE",
                                        "score": 88,
                                        "grade": "B"
                                      }
                                    ]
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400", description = "유효성 검증 실패",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "code": 400,
                                      "name": "VALIDATION_FAILED",
                                      "message": "유효성 검증에 실패했습니다.",
                                      "errors": {
                                        "create[0].score": "점수는 100 이하여야 합니다."
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "403", description = "담임교사 또는 관리자가 아닌 경우",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "code": 403,
                                      "name": "GRADE_ACCESS_DENIED",
                                      "message": "해당 성적에 접근할 권한이 없습니다."
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "update/delete에 존재하지 않는 id 포함",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "code": 404,
                                      "name": "GRADE_NOT_FOUND",
                                      "message": "해당 성적을 찾을 수 없습니다."
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "409", description = "create에 이미 등록된 과목 포함",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "code": 409,
                                      "name": "GRADE_ALREADY_EXISTS",
                                      "message": "이미 등록된 성적입니다."
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<List<GradeResponse>> batchProcess(
            @PathVariable Long studentId,
            @Valid @RequestBody(required = true) BatchGradeRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails);
}
