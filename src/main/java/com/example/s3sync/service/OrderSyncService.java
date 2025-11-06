package com.example.s3sync.service;

import com.example.s3sync.domain.Customer;
import com.example.s3sync.domain.Order;
import com.example.s3sync.domain.SyncedOrderHash;
import com.example.s3sync.dto.OrderCsvDto;
import com.example.s3sync.repository.CustomerRepository;
import com.example.s3sync.repository.SyncedOrderHashRepository;
import com.example.s3sync.util.DomainDataMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Service that persists order sync markers and uploads per-country CSVs.
 *
 * <p>This component accepts a list of orders determined to be unsynced, groups them by country
 * (resolved via the related customer's country), computes and persists a marker/hash for each
 * order, renders CSV files per country and uploads those files to S3. Uploads are scheduled in a
 * transaction synchronization so that objects are cleaned up if the surrounding transaction rolls
 * back.
 *
 * <p>Notes:
 *
 * <ul>
 *   <li>The main method {@link #syncAndUpload(List)} is annotated with {@link Transactional} and
 *       registers a {@link TransactionSynchronization} to perform uploads in {@code beforeCommit}
 *       and cleanup on rollback.
 *   <li>The service persists {@link SyncedOrderHash} entries (marker hashes) for each processed
 *       order prior to scheduling uploads.
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSyncService {

  private final CustomerRepository customerRepository;
  private final SyncedOrderHashRepository syncedOrderHashRepository;
  private final HashService hashService;
  private final CsvService csvService;
  private final S3UploaderService s3Uploader;

  /**
   * Persist sync markers for the provided orders and upload per-country CSVs.
   *
   * <p>Behavior:
   *
   * <ol>
   *   <li>Group {@code unsyncedOrders} by country, resolving the country via the related customer.
   *   <li>For each order compute a stable marker/hash and persist a {@link SyncedOrderHash} entry.
   *   <li>Render a CSV for each country and schedule S3 uploads inside a transaction
   *       synchronization. Uploads happen in {@code beforeCommit} so they only occur when the
   *       transaction successfully commits; uploaded files are deleted in {@code afterCompletion}
   *       if the transaction rolled back.
   * </ol>
   *
   * @param unsyncedOrders list of orders that need to be synchronized
   */
  @Transactional
  public void syncAndUpload(List<Order> unsyncedOrders) {

    Map<String, List<Order>> byCountry =
        unsyncedOrders.stream().collect(Collectors.groupingBy(this::getLand));

    Map<String, byte[]> csvToBeUploaded = new HashMap<>();

    for (Map.Entry<String, List<Order>> entry : byCountry.entrySet()) {
      String country = entry.getKey();
      List<Order> orders = entry.getValue();

      for (Order order : orders) {
        String markerHash = hashService.orderMarkerHash(order.getLastchange());

        syncedOrderHashRepository.save(
            SyncedOrderHash.builder().orderId(order.getId()).markerHash(markerHash).build());
      }

      List<OrderCsvDto> orderDtos =
          entry.getValue().stream().map(DomainDataMapper::orderToDto).collect(Collectors.toList());

      byte[] csvBytes = csvService.ordersToCsv(orderDtos);

      String filename =
          "auftraege_"
              + country
              + "_"
              + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HH"))
              + ".csv";

      csvToBeUploaded.put(filename, csvBytes);
    }

    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          List<String> uploadedCsvFiles = new ArrayList<>();

          @Override
          public void beforeCommit(boolean readOnly) {
            for (Map.Entry<String, byte[]> entry : csvToBeUploaded.entrySet()) {
              String filename = entry.getKey();
              byte[] csvBytes = entry.getValue();

              s3Uploader.uploadCsvBytes(csvBytes, filename);
              uploadedCsvFiles.add(filename);
            }
          }

          @Override
          public void afterCompletion(int status) {
            if (status == STATUS_ROLLED_BACK) {
              for (String key : uploadedCsvFiles) {
                try {
                  s3Uploader.delete(key);
                } catch (Exception ignored) {
                  log.warn("S3 cleanup failed for {}", key);
                }
              }
            }
          }
        });
  }

  /**
   * Resolve the country ('land') for the given order by loading the associated customer and
   * returning its {@link Customer#getLand()} value.
   *
   * <p>Returns {@code null} if the customer cannot be found.
   *
   * @param order order whose customer's country should be returned
   * @return country code/name from the related customer or {@code null}
   */
  String getLand(Order order) {
    Customer customer = customerRepository.findById(Long.valueOf(order.getKundeid())).orElse(null);
    return customer.getLand();
  }
}
