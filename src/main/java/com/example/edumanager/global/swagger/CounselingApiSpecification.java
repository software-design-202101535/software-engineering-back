package com.example.edumanager.global.swagger;

import com.example.edumanager.domain.counseling.dto.CounselingResponse;
import com.example.edumanager.domain.counseling.dto.CreateCounselingRequest;
import com.example.edumanager.domain.counseling.dto.UpdateCounselingRequest;
import com.example.edumanager.domain.counseling.dto.UpdateCounselingShareRequest;
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

@Tag(name = "상담 API", description = "학생 상담 기록 조회 및 관리")
public interface CounselingApiSpecification {

    @Operation(summary = "상담 목록 조회", description = "연도(필수)/월(선택) 기준으로 상담 기록을 조회합니다. 본인 작성 기록은 공개 여부와 무관하게 전부 반환되며, 타인 기록은 sharedWithTeachers=true인 것만 반환됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CounselingResponse.class),
                            examples = @ExampleObject("""
                                    [
                                      {
                                        "id": 1,
                                        "studentId": 42,
                                        "teacherId": 7,
                                        "teacherName": "김선생",
                                        "counselingDate": "2026-03-15",
                                        "content": "학업 태도 개선에 대해 상담함",
                                        "nextPlan": "다음 상담 때 진도 점검",
                                        "nextDate": "2026-04-10",
                                        "sharedWithTeachers": true,
                                        "createdAt": "2026-03-15T14:30:00"
                                      }
                                    ]
                                    """))),
            @ApiResponse(responseCode = "403", description = "TEACHER가 아닌 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 403, "name": "STUDENT_ACCESS_DENIED", "message": "학생 정보에 접근할 권한이 없습니다."}
                                    """))),
            @ApiResponse(responseCode = "404", description = "학생을 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 404, "name": "STUDENT_NOT_FOUND", "message": "해당 학생을 찾을 수 없습니다."}
                                    """)))
    })
    ResponseEntity<List<CounselingResponse>> getList(
            @PathVariable Long studentId,
            @RequestParam int year,
            @RequestParam(required = false) Integer month,
            @AuthenticationPrincipal UserDetailsImpl userDetails);

    @Operation(summary = "상담 기록 작성", description = "학생 상담 기록을 작성합니다. TEACHER만 호출 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "작성 성공",
                    content = @Content(schema = @Schema(implementation = CounselingResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "id": 1,
                                      "studentId": 42,
                                      "teacherId": 7,
                                      "teacherName": "김선생",
                                      "counselingDate": "2026-03-15",
                                      "content": "학업 태도 개선에 대해 상담함",
                                      "nextPlan": "다음 상담 때 진도 점검",
                                      "nextDate": "2026-04-10",
                                      "sharedWithTeachers": false,
                                      "createdAt": "2026-03-15T14:30:00"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "필수 입력값 누락",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 400, "name": "INVALID_INPUT_VALUE", "message": "잘못된 입력입니다.", "errors": {"counselingDate": "상담 날짜를 입력해주세요."}}
                                    """))),
            @ApiResponse(responseCode = "403", description = "TEACHER가 아닌 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 403, "name": "STUDENT_ACCESS_DENIED", "message": "학생 정보에 접근할 권한이 없습니다."}
                                    """))),
            @ApiResponse(responseCode = "404", description = "학생을 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 404, "name": "STUDENT_NOT_FOUND", "message": "해당 학생을 찾을 수 없습니다."}
                                    """)))
    })
    ResponseEntity<CounselingResponse> create(
            @PathVariable Long studentId,
            @RequestBody @Valid CreateCounselingRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails);

    @Operation(summary = "상담 기록 수정", description = "상담 기록을 수정합니다. 본인 작성분만 수정 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = CounselingResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "id": 1,
                                      "studentId": 42,
                                      "teacherId": 7,
                                      "teacherName": "김선생",
                                      "counselingDate": "2026-03-15",
                                      "content": "수정된 상담 내용",
                                      "nextPlan": "수정된 다음 계획",
                                      "nextDate": "2026-04-15",
                                      "sharedWithTeachers": false,
                                      "createdAt": "2026-03-15T14:30:00"
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "타인 작성분 수정 시도",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 403, "name": "COUNSELING_ACCESS_DENIED", "message": "해당 상담 기록에 접근할 권한이 없습니다."}
                                    """))),
            @ApiResponse(responseCode = "404", description = "상담 기록을 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 404, "name": "COUNSELING_NOT_FOUND", "message": "해당 상담 기록을 찾을 수 없습니다."}
                                    """)))
    })
    ResponseEntity<CounselingResponse> update(
            @PathVariable Long studentId,
            @PathVariable Long counselingId,
            @RequestBody @Valid UpdateCounselingRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails);

    @Operation(summary = "교사 공유 설정 변경", description = "상담 기록의 교사 공유 여부를 변경합니다. 본인 작성분만 변경 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공",
                    content = @Content(schema = @Schema(implementation = CounselingResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "id": 1,
                                      "studentId": 42,
                                      "teacherId": 7,
                                      "teacherName": "김선생",
                                      "counselingDate": "2026-03-15",
                                      "content": "학업 태도 개선에 대해 상담함",
                                      "nextPlan": "다음 상담 때 진도 점검",
                                      "nextDate": "2026-04-10",
                                      "sharedWithTeachers": true,
                                      "createdAt": "2026-03-15T14:30:00"
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "타인 작성분 변경 시도",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 403, "name": "COUNSELING_ACCESS_DENIED", "message": "해당 상담 기록에 접근할 권한이 없습니다."}
                                    """))),
            @ApiResponse(responseCode = "404", description = "상담 기록을 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 404, "name": "COUNSELING_NOT_FOUND", "message": "해당 상담 기록을 찾을 수 없습니다."}
                                    """)))
    })
    ResponseEntity<CounselingResponse> updateShare(
            @PathVariable Long studentId,
            @PathVariable Long counselingId,
            @RequestBody @Valid UpdateCounselingShareRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails);

    @Operation(summary = "상담 기록 삭제", description = "상담 기록을 삭제합니다. 본인 작성분만 삭제 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "타인 작성분 삭제 시도",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 403, "name": "COUNSELING_ACCESS_DENIED", "message": "해당 상담 기록에 접근할 권한이 없습니다."}
                                    """))),
            @ApiResponse(responseCode = "404", description = "상담 기록을 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 404, "name": "COUNSELING_NOT_FOUND", "message": "해당 상담 기록을 찾을 수 없습니다."}
                                    """)))
    })
    ResponseEntity<Void> delete(
            @PathVariable Long studentId,
            @PathVariable Long counselingId,
            @AuthenticationPrincipal UserDetailsImpl userDetails);
}
