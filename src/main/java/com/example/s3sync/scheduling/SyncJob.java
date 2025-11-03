package com.example.s3sync.scheduling;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.example.s3sync.service.SyncService;

/**
 * Scheduled job that triggers synchronization tasks.
 *
 * <p>
 * This component runs periodically and delegates to {@link SyncService}
 * to perform customer and order synchronization. The scheduling configuration
 * on the {@link #runSyncJob()} method uses a cron expression and the
 * Europe/Berlin timezone so timestamps and triggers align with the local
 * business timezone.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SyncJob {

    private final SyncService syncService;

    /**
     * Executes the sync job on a schedule.
     *
     * <p>
     * Configured cron expression: — this runs the
     * job every minute (at second 0). The timezone is set to Europe/Berlin.
     * The method logs the start of a run and delegates to {@link SyncService}
     * for the actual sync work.
     * </p>
     */
    @Scheduled(cron = "0 */1 * * * *", zone = "Europe/Berlin")
    public void runSyncJob() {
        log.info("Starting sync job");
        syncService.syncCustomer();
        syncService.syncOrders();
    }
}
