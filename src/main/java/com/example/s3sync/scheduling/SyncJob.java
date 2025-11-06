package com.example.s3sync.scheduling;

import com.example.s3sync.domain.Customer;
import com.example.s3sync.domain.Order;
import com.example.s3sync.service.CustomerSyncService;
import com.example.s3sync.service.OrderSyncService;
import com.example.s3sync.service.SyncDiffService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Lightweight scheduled trigger that initiates the synchronization flow for customers and orders.
 *
 * <p>Behavior: on each scheduled execution the job asks {@link SyncDiffService} for unsynced
 * customers and unsynced orders and delegates processing to the {@link CustomerSyncService} and
 * {@link OrderSyncService} respectively. The sync services perform persistence of tracking entries
 * and schedule any S3 uploads transactionally.
 *
 * <p>Scheduling is driven by configuration properties. The method-level scheduling annotation uses
 * `fixedRateString` and `initialDelayString` to read `scheduler.rate` and `scheduler.delay` from
 * the application configuration — this allows controlling execution frequency without recompiling
 * the application.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SyncJob {

  private final SyncDiffService syncDiffService;
  private final CustomerSyncService customerSyncService;
  private final OrderSyncService orderSyncService;

  /**
   * Runs the synchronization cycle.
   *
   * <p>Behavior:
   *
   * <ol>
   *   <li>Ask {@link SyncDiffService} for unsynced customers and, if any are found, call {@link
   *       CustomerSyncService#syncAndUpload(List)}.
   *   <li>Ask {@link SyncDiffService} for unsynced orders and, if any are found, call {@link
   *       OrderSyncService#syncAndUpload(List)}.
   * </ol>
   *
   * <p>The method is annotated with a cron schedule that currently triggers execution every minute
   * at second 0 in the Europe/Berlin timezone.
   */
  @Scheduled(fixedRateString = "${scheduler.rate}", initialDelayString = "${scheduler.delay}")
  public void runSyncJob() {
    log.info("Starting sync job");
    List<Customer> unsyncedCustomers = syncDiffService.getUnsyncedCustomers();
    if (!unsyncedCustomers.isEmpty()) {
      customerSyncService.syncAndUpload(unsyncedCustomers);
    } else {
      log.info("No unsynced customers found");
    }
    List<Order> unsyncedOrders = syncDiffService.getUnsyncedOrders();
    if (!unsyncedOrders.isEmpty()) {
      orderSyncService.syncAndUpload(unsyncedOrders);
    } else {
      log.info("No unsynced orders found");
    }
    log.info("Sync job completed");
  }
}
