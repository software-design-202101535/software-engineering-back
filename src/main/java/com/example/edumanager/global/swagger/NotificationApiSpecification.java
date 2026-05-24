package com.example.edumanager.global.swagger;

import com.example.edumanager.domain.notification.dto.NotificationResponse;
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

import java.util.List;

@Tag(name = "알림 API", description = "사용자 본인의 알림 목록 조회 및 읽음 처리")
public interface NotificationApiSpecification {

    @Operation(summary = "내 알림 목록 조회", description = "로그인한 사용자 본인의 알림을 최신순으로 반환합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = NotificationResponse.class),
                            examples = @ExampleObject("""
                                    [
                                      {
                                        "id": 12,
                                        "type": "GRADE_UPDATED",
                                        "title": "성적이 등록되었습니다",
                                        "message": "수학 중간고사 성적이 등록되었습니다.",
                                        "isRead": false,
                                        "referenceId": 5,
                                        "referenceType": "GRADE",
                                        "createdAt": "2025-03-14T09:30:00"
                                      }
                                    ]
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 401, "name": "JWT_ENTRY_POINT", "message": "로그인이 필요합니다."}
                                    """)))
    })
    ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal UserDetailsImpl userDetails);

    @Operation(summary = "알림 단건 읽음 처리", description = "특정 알림을 읽음 처리합니다. 본인의 알림만 처리 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "처리 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 401, "name": "JWT_ENTRY_POINT", "message": "로그인이 필요합니다."}
                                    """))),
            @ApiResponse(responseCode = "404", description = "알림이 없거나 본인의 알림이 아닌 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 404, "name": "NOTIFICATION_NOT_FOUND", "message": "해당 알림을 찾을 수 없습니다."}
                                    """)))
    })
    ResponseEntity<Void> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserDetailsImpl userDetails);

    @Operation(summary = "내 알림 전체 읽음 처리", description = "로그인한 사용자 본인의 읽지 않은 알림을 모두 읽음 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "처리 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 401, "name": "JWT_ENTRY_POINT", "message": "로그인이 필요합니다."}
                                    """)))
    })
    ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal UserDetailsImpl userDetails);
}
