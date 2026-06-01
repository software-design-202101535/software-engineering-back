package com.example.edumanager.domain.analytics.controller;

import com.example.edumanager.facade.AnalyticsEtlFacade;
import com.example.edumanager.global.security.UserDetailsImpl;
import com.example.edumanager.global.swagger.AnalyticsEtlApiSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OLAP 분석 테이블 수동 재집계 트리거 (운영/성능측정용, 프론트 미사용).
 * 스케줄러가 새벽에 부르는 재집계를 즉시 1회 실행한다.
 * 인증(401)은 SecurityConfig, 교사 한정(403)은 facade 가 책임진다.
 */
@RestController
@RequestMapping("/api/analytics/etl")
@RequiredArgsConstructor
public class AnalyticsEtlController implements AnalyticsEtlApiSpecification {

    private final AnalyticsEtlFacade analyticsEtlFacade;

    @PostMapping("/rebuild")
    public ResponseEntity<Void> rebuild(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        analyticsEtlFacade.rebuildByTeacher(userDetails);
        return ResponseEntity.ok().build();
    }
}
