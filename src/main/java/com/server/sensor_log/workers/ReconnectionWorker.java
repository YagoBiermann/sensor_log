package com.server.sensor_log.workers;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReconnectionWorker {

    private final AtomicReference<ScheduledFuture<?>> reconnectTask = new AtomicReference<>();
    private final TaskScheduler scheduler;

    public void scheduleReconnect(Runnable callback) {
        if (reconnectTask.get() != null) {
            log.debug("⚪ Reconnect already scheduled, skipping.");
            return;
        }

        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(
                callback,
                Instant.now().plusSeconds(5),
                Duration.ofSeconds(10)
        );

        if (!reconnectTask.compareAndSet(null, future)) {
            future.cancel(false);
            log.debug("⚪ Reconnect already scheduled, skipping.");
            return;
        }

        log.debug("🔵 Reconnection task scheduled.");

    }

    public void cancelReconnect() {
        ScheduledFuture<?> task = reconnectTask.getAndSet(null);
        if (task != null) {
            task.cancel(false);
            log.debug("Reconnect task cancelled.");
        }
    }
}
