package com.example.edumanager.domain.analytics.scheduler;

import com.example.edumanager.facade.AnalyticsEtlFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * OLAP ETL 배치 트리거. 매일 새벽 비피크에 1회 전체 재계산한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsEtlScheduler {

    private final AnalyticsEtlFacade analyticsEtlFacade;

    @Scheduled(cron = "0 30 4 * * *", zone = "Asia/Seoul")
    public void run() {
        log.info("OLAP ETL 배치 시작");
        analyticsEtlFacade.rebuild(LocalDateTime.now());
        log.info("OLAP ETL 배치 완료");
    }
}
