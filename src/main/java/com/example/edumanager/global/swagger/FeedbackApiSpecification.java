package com.example.edumanager.global.swagger;

import com.example.edumanager.domain.feedback.dto.CreateFeedbackRequest;
import com.example.edumanager.domain.feedback.dto.FeedbackResponse;
import com.example.edumanager.domain.feedback.dto.UpdateFeedbackRequest;
import com.example.edumanager.domain.feedback.dto.UpdateFeedbackVisibilityRequest;
import com.example.edumanager.domain.feedback.entity.FeedbackCategory;
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

@Tag(name = "피드백 API", description = "학생 피드백 조회 및 관리")
public interface FeedbackApiSpecification {

    @Operation(summary = "피드백 목록 조회", description = "역할에 따라 필터링된 피드백 목록을 반환합니다. TEACHER는 전체, STUDENT는 studentVisible, PARENT는 parentVisible 피드백만 반환됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = FeedbackResponse.class),
                            examples = @ExampleObject("""
                                    [
                                      {
                                        "id": 1,
                                        "category": "GRADE",
                                        "date": "2025-03-14",
                                        "content": "성적이 향상되었습니다.",
                                        "studentVisible": true,
                                        "parentVisible": true,
                                        "authorId": 10,
                                        "authorName": "김교사"
                                      }
                                    ]
                                    """))),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음",
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
    ResponseEntity<List<FeedbackResponse>> getList(
            @PathVariable Long studentId,
            @RequestParam(required = false) FeedbackCategory category,
            @AuthenticationPrincipal UserDetailsImpl userDetails);

    @Operation(summary = "피드백 작성", description = "학생에게 피드백을 작성합니다. TEACHER만 호출 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "작성 성공",
                    content = @Content(schema = @Schema(implementation = FeedbackResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "id": 1,
                                      "category": "GRADE",
                                      "date": "2025-03-14",
                                      "content": "성적이 향상되었습니다.",
                                      "studentVisible": true,
                                      "parentVisible": false,
                                      "authorId": 10,
                                      "authorName": "김교사"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "필수 입력값 누락",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 400, "name": "INVALID_INPUT_VALUE", "message": "잘못된 입력입니다.", "errors": {"category": "카테고리를 입력해주세요."}}
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
    ResponseEntity<FeedbackResponse> create(
            @PathVariable Long studentId,
            @RequestBody @Valid CreateFeedbackRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails);

    @Operation(summary = "피드백 수정", description = "피드백 전체 내용을 수정합니다. 본인 작성분만 수정 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = FeedbackResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "id": 1,
                                      "category": "BEHAVIOR",
                                      "date": "2025-03-15",
                                      "content": "수업 태도가 개선되었습니다.",
                                      "studentVisible": true,
                                      "parentVisible": true,
                                      "authorId": 10,
                                      "authorName": "김교사"
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "타인 작성분 수정 시도",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 403, "name": "FEEDBACK_ACCESS_DENIED", "message": "해당 피드백에 접근할 권한이 없습니다."}
                                    """))),
            @ApiResponse(responseCode = "404", description = "피드백을 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 404, "name": "FEEDBACK_NOT_FOUND", "message": "해당 피드백을 찾을 수 없습니다."}
                                    """)))
    })
    ResponseEntity<FeedbackResponse> update(
            @PathVariable Long studentId,
            @PathVariable Long feedbackId,
            @RequestBody @Valid UpdateFeedbackRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails);

    @Operation(summary = "피드백 공개 설정 변경", description = "피드백의 학생/학부모 공개 여부만 변경합니다. 본인 작성분만 수정 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공",
                    content = @Content(schema = @Schema(implementation = FeedbackResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "id": 1,
                                      "category": "GRADE",
                                      "date": "2025-03-14",
                                      "content": "성적이 향상되었습니다.",
                                      "studentVisible": false,
                                      "parentVisible": true,
                                      "authorId": 10,
                                      "authorName": "김교사"
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "타인 작성분 수정 시도",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 403, "name": "FEEDBACK_ACCESS_DENIED", "message": "해당 피드백에 접근할 권한이 없습니다."}
                                    """))),
            @ApiResponse(responseCode = "404", description = "피드백을 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 404, "name": "FEEDBACK_NOT_FOUND", "message": "해당 피드백을 찾을 수 없습니다."}
                                    """)))
    })
    ResponseEntity<FeedbackResponse> updateVisibility(
            @PathVariable Long studentId,
            @PathVariable Long feedbackId,
            @RequestBody @Valid UpdateFeedbackVisibilityRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails);

    @Operation(summary = "피드백 삭제", description = "피드백을 삭제합니다. 본인 작성분만 삭제 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "타인 작성분 삭제 시도",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 403, "name": "FEEDBACK_ACCESS_DENIED", "message": "해당 피드백에 접근할 권한이 없습니다."}
                                    """))),
            @ApiResponse(responseCode = "404", description = "피드백을 찾을 수 없는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 404, "name": "FEEDBACK_NOT_FOUND", "message": "해당 피드백을 찾을 수 없습니다."}
                                    """)))
    })
    ResponseEntity<Void> delete(
            @PathVariable Long studentId,
            @PathVariable Long feedbackId,
            @AuthenticationPrincipal UserDetailsImpl userDetails);
}
