package com.example.edumanager.global.swagger;

import com.example.edumanager.domain.counseling.dto.SharedCounselingResponse;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "상담 API", description = "학생 상담 기록 조회 및 관리")
public interface SharedCounselingApiSpecification {

    @Operation(summary = "공유 상담 목록 조회",
            description = "다른 교사가 공유한(sharedWithTeachers=true) 상담을 학년/반별로 모아 조회합니다. "
                    + "요청 교사와 같은 학교 학생의 상담만 반환하며, 본인이 작성한 상담은 제외됩니다. "
                    + "연도(필수)/월(선택)과 학년/반/학생 이름(부분일치)으로 필터링하며, counselingDate 내림차순으로 정렬됩니다. "
                    + "TEACHER만 호출 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = SharedCounselingResponse.class),
                            examples = @ExampleObject("""
                                    [
                                      {
                                        "id": 101,
                                        "studentId": 12,
                                        "studentName": "김영희",
                                        "grade": 1,
                                        "classNum": 4,
                                        "number": 12,
                                        "teacherId": 5,
                                        "teacherName": "김교사",
                                        "counselingDate": "2026-05-20",
                                        "content": "수업 태도 관련 상담 내용 ...",
                                        "nextPlan": "다음 상담 계획 ...",
                                        "nextDate": "2026-06-10",
                                        "sharedWithTeachers": true,
                                        "createdAt": "2026-05-20T14:30:00"
                                      }
                                    ]
                                    """))),
            @ApiResponse(responseCode = "400", description = "필수 파라미터(year) 누락",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 400, "name": "MISSING_PARAMETER", "message": "요청에 필요한 입력이 누락되었습니다."}
                                    """))),
            @ApiResponse(responseCode = "403", description = "TEACHER가 아닌 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 403, "name": "STUDENT_ACCESS_DENIED", "message": "학생 정보에 접근할 권한이 없습니다."}
                                    """)))
    })
    ResponseEntity<List<SharedCounselingResponse>> getSharedList(
            @RequestParam int year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer grade,
            @RequestParam(required = false) Integer classNum,
            @RequestParam(required = false) String name,
            @AuthenticationPrincipal UserDetailsImpl userDetails);
}
