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
 * Scheduled job that triggers synchronization tasks for customers and orders.
 *
 * <p>This component periodically polls the domain for rows that need to be synchronized (via {@link
 * SyncDiffService}) and delegates persistence and upload work to {@link CustomerSyncService} and
 * {@link OrderSyncService}. The job is scheduled using a cron expression and runs in the
 * Europe/Berlin timezone to align with local business hours.
 *
 * <p>Operational notes:
 *
 * <ul>
 *   <li>The job runs every minute.
 *   <li>Each run queries unsynced customers and orders separately and triggers their respective
 *       sync-and-upload flows only when work is detected.
 * </ul>
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
  @Scheduled(cron = "0 */1 * * * *", zone = "Europe/Berlin")
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
  }
}
