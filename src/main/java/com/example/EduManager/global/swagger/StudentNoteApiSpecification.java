package com.example.EduManager.global.swagger;

import com.example.EduManager.domain.student.dto.CreateNoteRequest;
import com.example.EduManager.domain.student.dto.NoteResponse;
import com.example.EduManager.domain.student.dto.UpdateNoteRequest;
import com.example.EduManager.domain.student.entity.NoteCategory;
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

@Tag(name = "특기사항 API", description = "학생부 특기사항 조회 및 관리")
public interface StudentNoteApiSpecification {

    @Operation(
            summary = "특기사항 목록 조회",
            description = """
                    학생의 특기사항 목록을 날짜 역순으로 반환합니다.
                    category 파라미터를 지정하면 해당 카테고리만 조회합니다.
                    category: ACHIEVEMENT | SPECIAL | VOLUNTEER | CAREER | OTHER
                    담임교사 또는 ADMIN만 호출 가능합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = NoteResponse.class),
                            examples = @ExampleObject("""
                                    [
                                      {
                                        "id": 1,
                                        "studentId": 1,
                                        "teacherId": 1,
                                        "category": "ACHIEVEMENT",
                                        "content": "수학올림피아드 교내 대회 금상 수상",
                                        "date": "2025-03-15",
                                        "createdAt": "2025-03-15T10:00:00"
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
    ResponseEntity<List<NoteResponse>> getList(
            @PathVariable Long studentId,
            @RequestParam(required = false) NoteCategory category,
            @AuthenticationPrincipal UserDetailsImpl userDetails);

    @Operation(summary = "특기사항 추가", description = "학생의 특기사항을 추가합니다. 담임교사 또는 ADMIN만 호출 가능합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201", description = "추가 성공",
                    content = @Content(
                            schema = @Schema(implementation = NoteResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "id": 1,
                                      "studentId": 1,
                                      "teacherId": 1,
                                      "category": "ACHIEVEMENT",
                                      "content": "수학올림피아드 교내 대회 금상 수상",
                                      "date": "2025-03-15",
                                      "createdAt": "2025-03-15T10:00:00"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "필수 입력값 누락",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 400, "name": "INVALID_INPUT_VALUE", "message": "잘못된 입력입니다.", "errors": {"content": "내용을 입력해주세요."}}
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
                                    """)))
    })
    ResponseEntity<NoteResponse> create(
            @PathVariable Long studentId,
            @RequestBody @Valid CreateNoteRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails);

    @Operation(summary = "특기사항 수정", description = "특기사항의 카테고리, 내용, 날짜를 수정합니다. 담임교사 또는 ADMIN만 호출 가능합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "수정 성공",
                    content = @Content(
                            schema = @Schema(implementation = NoteResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "id": 1,
                                      "studentId": 1,
                                      "teacherId": 1,
                                      "category": "ACHIEVEMENT",
                                      "content": "수정된 내용",
                                      "date": "2025-03-15",
                                      "createdAt": "2025-03-15T10:00:00"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "403", description = "담임교사가 아닌 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 403, "name": "STUDENT_ACCESS_DENIED", "message": "학생 정보에 접근할 권한이 없습니다."}
                                    """))),
            @ApiResponse(responseCode = "404", description = "학생 또는 특기사항을 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 404, "name": "NOTE_NOT_FOUND", "message": "해당 특기사항을 찾을 수 없습니다."}
                                    """)))
    })
    ResponseEntity<NoteResponse> update(
            @PathVariable Long studentId,
            @PathVariable Long noteId,
            @RequestBody @Valid UpdateNoteRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails);

    @Operation(summary = "특기사항 삭제", description = "특기사항을 삭제합니다. 담임교사 또는 ADMIN만 호출 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "담임교사가 아닌 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 403, "name": "STUDENT_ACCESS_DENIED", "message": "학생 정보에 접근할 권한이 없습니다."}
                                    """))),
            @ApiResponse(responseCode = "404", description = "학생 또는 특기사항을 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 404, "name": "NOTE_NOT_FOUND", "message": "해당 특기사항을 찾을 수 없습니다."}
                                    """)))
    })
    ResponseEntity<Void> delete(
            @PathVariable Long studentId,
            @PathVariable Long noteId,
            @AuthenticationPrincipal UserDetailsImpl userDetails);
}
