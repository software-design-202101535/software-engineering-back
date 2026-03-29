package com.example.EduManager.global.swagger;

import com.example.EduManager.domain.student.dto.StudentSummaryResponse;
import com.example.EduManager.global.exception.ErrorResponse;
import com.example.EduManager.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@Tag(name = "학생 API", description = "학생 목록 조회")
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
                                      },
                                      {
                                        "id": 2,
                                        "name": "김영희",
                                        "grade": 2,
                                        "classNum": 3,
                                        "number": 2
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
}
