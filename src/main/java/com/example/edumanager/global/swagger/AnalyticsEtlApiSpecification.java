package com.example.edumanager.global.swagger;

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

@Tag(name = "분석 ETL API", description = "OLAP 분석 테이블 수동 재집계 (운영/성능측정용, 프론트 미사용)")
public interface AnalyticsEtlApiSpecification {

    @Operation(summary = "분석 테이블 전체 재집계 (수동 트리거)",
            description = "스케줄러가 매일 새벽 수행하는 OLAP 전체 재집계를 즉시 1회 실행합니다. " +
                    "교사 권한이 필요합니다. 전교 재계산이라 비용이 큽니다 — 부하테스트/성능 측정 용도입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재집계 완료"),
            @ApiResponse(responseCode = "401", description = "미인증",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 401, "name": "JWT_ENTRY_POINT", "message": "로그인이 필요합니다."}
                                    """))),
            @ApiResponse(responseCode = "403", description = "교사 권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject("""
                                    {"code": 403, "name": "JWT_ACCESS_DENIED", "message": "접근 권한이 없습니다."}
                                    """)))
    })
    ResponseEntity<Void> rebuild(@AuthenticationPrincipal UserDetailsImpl userDetails);
}
