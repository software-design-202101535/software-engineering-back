package com.example.edumanager.global.swagger;

import com.example.edumanager.domain.devicetoken.dto.RegisterDeviceTokenRequest;
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

@Tag(name = "디바이스 토큰 API", description = "FCM 푸시 발송용 디바이스 토큰 등록 및 해제")
public interface DeviceTokenApiSpecification {

    @Operation(summary = "디바이스 토큰 등록",
            description = "FCM 토큰을 등록합니다. 동일 토큰이 이미 다른 사용자에게 등록되어 있으면 소유권이 현재 사용자에게 이전됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "필수 입력값 누락",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 400, "name": "INVALID_INPUT_VALUE", "message": "잘못된 입력입니다.", "errors": {"token": "FCM 토큰을 입력해주세요."}}
                                    """))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 401, "name": "JWT_ENTRY_POINT", "message": "로그인이 필요합니다."}
                                    """)))
    })
    ResponseEntity<Void> register(
            @RequestBody @Valid RegisterDeviceTokenRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails);

    @Operation(summary = "디바이스 토큰 해제", description = "본인의 FCM 토큰을 삭제합니다. 토큰이 존재하지 않거나 본인 것이 아니면 무시됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "해제 성공 (대상이 없어도 동일)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 401, "name": "JWT_ENTRY_POINT", "message": "로그인이 필요합니다."}
                                    """)))
    })
    ResponseEntity<Void> unregister(
            @PathVariable String token,
            @AuthenticationPrincipal UserDetailsImpl userDetails);
}
