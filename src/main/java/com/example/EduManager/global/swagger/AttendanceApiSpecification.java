package com.example.EduManager.global.swagger;

import com.example.EduManager.domain.attendance.dto.AttendanceResponse;
import com.example.EduManager.domain.attendance.dto.CreateAttendanceRequest;
import com.example.EduManager.domain.attendance.dto.UpdateAttendanceRequest;
import com.example.EduManager.global.exception.ErrorResponse;
import com.example.EduManager.global.security.UserDetailsImpl;
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

@Tag(name = "출결 API", description = "학생 출결 기록 조회 및 관리")
public interface AttendanceApiSpecification {

    @Operation(summary = "출결 목록 조회", description = "특정 학생의 연월별 출결 목록을 반환합니다. 담임교사 또는 ADMIN만 호출 가능합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = AttendanceResponse.class),
                            examples = @ExampleObject("""
                                    [
                                      {
                                        "id": 1,
                                        "studentId": 1,
                                        "date": "2025-03-02",
                                        "status": "ABSENT",
                                        "reason": "개인 사정"
                                      }
                                    ]
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "403", description = "담임교사가 아닌 경우",
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
    ResponseEntity<List<AttendanceResponse>> getList(
            @PathVariable Long studentId,
            @RequestParam int year,
            @RequestParam int month,
            @AuthenticationPrincipal UserDetailsImpl userDetails);

    @Operation(summary = "출결 기록 추가", description = "특정 학생의 출결을 기록합니다. 담임교사 또는 ADMIN만 호출 가능합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201", description = "기록 성공",
                    content = @Content(
                            schema = @Schema(implementation = AttendanceResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "id": 1,
                                      "studentId": 1,
                                      "date": "2025-03-02",
                                      "status": "ABSENT",
                                      "reason": "개인 사정"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "필수 입력값 누락",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 400, "name": "INVALID_INPUT_VALUE", "message": "잘못된 입력입니다.", "errors": {"date": "날짜를 입력해주세요."}}
                                    """))),
            @ApiResponse(responseCode = "403", description = "담임교사가 아닌 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 403, "name": "STUDENT_ACCESS_DENIED", "message": "학생 정보에 접근할 권한이 없습니다."}
                                    """))),
            @ApiResponse(responseCode = "404", description = "학생을 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 404, "name": "STUDENT_NOT_FOUND", "message": "해당 학생을 찾을 수 없습니다."}
                                    """))),
            @ApiResponse(responseCode = "409", description = "해당 날짜에 이미 출결 기록이 존재하는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 409, "name": "DATA_INTEGRITY_VIOLATION", "message": "이미 존재하는 데이터이거나 처리할 수 없는 요청입니다."}
                                    """)))
    })
    ResponseEntity<AttendanceResponse> create(
            @PathVariable Long studentId,
            @RequestBody @Valid CreateAttendanceRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails);

    @Operation(summary = "출결 수정", description = "출결 기록의 날짜, 상태, 사유를 수정합니다. 담임교사 또는 ADMIN만 호출 가능합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "수정 성공",
                    content = @Content(
                            schema = @Schema(implementation = AttendanceResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "id": 1,
                                      "studentId": 1,
                                      "date": "2025-03-02",
                                      "status": "LATE",
                                      "reason": "교통 체증"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "403", description = "담임교사가 아닌 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 403, "name": "STUDENT_ACCESS_DENIED", "message": "학생 정보에 접근할 권한이 없습니다."}
                                    """))),
            @ApiResponse(responseCode = "404", description = "학생 또는 출결 기록을 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 404, "name": "ATTENDANCE_NOT_FOUND", "message": "해당 출결 기록을 찾을 수 없습니다."}
                                    """)))
    })
    ResponseEntity<AttendanceResponse> update(
            @PathVariable Long studentId,
            @PathVariable Long attendanceId,
            @RequestBody @Valid UpdateAttendanceRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails);

    @Operation(summary = "출결 삭제", description = "출결 기록을 삭제합니다. 담임교사 또는 ADMIN만 호출 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "담임교사가 아닌 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 403, "name": "STUDENT_ACCESS_DENIED", "message": "학생 정보에 접근할 권한이 없습니다."}
                                    """))),
            @ApiResponse(responseCode = "404", description = "학생 또는 출결 기록을 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 404, "name": "ATTENDANCE_NOT_FOUND", "message": "해당 출결 기록을 찾을 수 없습니다."}
                                    """)))
    })
    ResponseEntity<Void> delete(
            @PathVariable Long studentId,
            @PathVariable Long attendanceId,
            @AuthenticationPrincipal UserDetailsImpl userDetails);
}
