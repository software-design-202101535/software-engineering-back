package com.example.edumanager.global.swagger;

import com.example.edumanager.domain.analytics.dto.ClassSummaryResponse;
import com.example.edumanager.domain.analytics.dto.StudentRankResponse;
import com.example.edumanager.domain.grade.entity.Subject;
import com.example.edumanager.domain.user.entity.School;
import com.example.edumanager.global.exception.ErrorResponse;
import com.example.edumanager.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "분석 API", description = "반/학년 성적 통계 및 학생 석차 조회 (분석 DB)")
public interface AnalyticsApiSpecification {

    @Operation(summary = "반/학년 과목별 통계 조회",
            description = "classNum 생략 시 학년 전체, subject 생략 시 전 과목을 반환합니다. 모든 인증 사용자가 조회할 수 있습니다. " +
                    "데이터는 매일 1회 배치로 갱신되며, 집계 전이면 subjects 가 빈 배열입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ClassSummaryResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "school": "SUNRIN_HIGH_SCHOOL",
                                      "grade": 3,
                                      "classNum": 2,
                                      "semester": "2025-1",
                                      "updatedAt": "2026-05-31T04:30:00",
                                      "subjects": [
                                        {
                                          "subject": "MATH",
                                          "avgScore": 79.1,
                                          "maxScore": 98,
                                          "minScore": 45,
                                          "studentCount": 28,
                                          "distribution": {"A": 3, "B": 10, "C": 8, "D": 5, "F": 2}
                                        }
                                      ]
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "잘못된 enum/파라미터",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 400, "name": "TYPE_MISMATCH", "message": "입력 형식이 올바르지 않습니다."}
                                    """))),
            @ApiResponse(responseCode = "401", description = "미인증",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 401, "name": "JWT_ENTRY_POINT", "message": "로그인이 필요합니다."}
                                    """)))
    })
    ResponseEntity<ClassSummaryResponse> getClassSummary(@RequestParam School school,
                                                         @RequestParam int grade,
                                                         @RequestParam(required = false) Integer classNum,
                                                         @RequestParam String semester,
                                                         @RequestParam(required = false) Subject subject);

    @Operation(summary = "학생 석차 조회",
            description = "반/전교 × 과목별/종합 석차를 한 번에 반환합니다. 본인 학생, 연결된 학부모, 교사만 조회할 수 있습니다. " +
                    "집계 전이면 class/grade 가 null 입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = StudentRankResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "studentId": 12,
                                      "semester": "2025-1",
                                      "updatedAt": "2026-05-31T04:30:00",
                                      "class": {
                                        "overall": {"rank": 3, "totalCount": 28, "percentile": 89.3, "avgScore": 85.3},
                                        "subjects": [
                                          {"subject": "MATH", "rank": 5, "totalCount": 28, "percentile": 82.1, "avgScore": 88.0, "gradeLevel": "B"}
                                        ]
                                      },
                                      "grade": {
                                        "overall": {"rank": 12, "totalCount": 210, "percentile": 94.3, "avgScore": 85.3},
                                        "subjects": []
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 403, "name": "STUDENT_ACCESS_DENIED", "message": "학생 정보에 접근할 권한이 없습니다."}
                                    """))),
            @ApiResponse(responseCode = "404", description = "학생을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 404, "name": "STUDENT_NOT_FOUND", "message": "해당 학생을 찾을 수 없습니다."}
                                    """)))
    })
    ResponseEntity<StudentRankResponse> getStudentRanks(@PathVariable Long studentId,
                                                        @RequestParam String semester,
                                                        @AuthenticationPrincipal UserDetailsImpl userDetails);
}
