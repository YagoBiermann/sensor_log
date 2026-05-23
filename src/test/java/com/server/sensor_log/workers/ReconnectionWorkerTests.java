package com.server.sensor_log.workers;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.server.sensor_log.application.workers.ReconnectionWorker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

@ExtendWith(MockitoExtension.class)
class ReconnectionWorkerTest {

    @Mock
    private TaskScheduler scheduler;

    @Mock
    private ScheduledFuture<?> future;

    @InjectMocks
    private ReconnectionWorker reconnectionWorker;

    private final Runnable callback = mock(Runnable.class);

    @Test
    void shouldScheduleTaskWhenNoneIsActive() {
        doReturn(future).when(scheduler).scheduleWithFixedDelay(
                any(Runnable.class), any(Instant.class), any(Duration.class));

        reconnectionWorker.scheduleReconnect(callback);

        verify(scheduler).scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), any(Duration.class));
    }


    @Test
    void shouldCancelTaskWhenOneIsActive() {
        doReturn(future).when(scheduler).scheduleWithFixedDelay(
                any(Runnable.class), any(Instant.class), any(Duration.class));

        reconnectionWorker.scheduleReconnect(callback);
        reconnectionWorker.cancelReconnect();

        verify(future).cancel(false);
    }

    @Test
    void shouldDoNothingOnCancelWhenNoTaskIsActive() {
        assertDoesNotThrow(() -> reconnectionWorker.cancelReconnect());
        verifyNoInteractions(future);
    }

    @Test
    void shouldAllowReschedulingAfterCancel() {
        doReturn(future).when(scheduler).scheduleWithFixedDelay(
                any(Runnable.class), any(Instant.class), any(Duration.class));

        reconnectionWorker.scheduleReconnect(callback);
        reconnectionWorker.cancelReconnect();
        reconnectionWorker.scheduleReconnect(callback);

        verify(scheduler, times(2)).scheduleWithFixedDelay(any(), any(Instant.class), any());
        verify(future, times(1)).cancel(false);
    }
}
