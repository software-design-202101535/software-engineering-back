package com.example.edumanager.domain.analytics.scheduler;

import com.example.edumanager.facade.AnalyticsEtlFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsEtlScheduler 단위 테스트")
class AnalyticsEtlSchedulerTest {

    @Mock AnalyticsEtlFacade analyticsEtlFacade;
    @InjectMocks AnalyticsEtlScheduler scheduler;

    @Test
    @DisplayName("TC-1. run() 은 ETL 전체 재계산을 호출한다")
    void runTriggersRebuild() {
        scheduler.run();

        verify(analyticsEtlFacade).rebuild(any(LocalDateTime.class));
    }
}
