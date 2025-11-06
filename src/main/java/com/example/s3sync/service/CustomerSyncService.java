package com.example.s3sync.service;

import com.example.s3sync.domain.Customer;
import com.example.s3sync.domain.SyncedCustomerHash;
import com.example.s3sync.dto.CustomerCsvDto;
import com.example.s3sync.repository.SyncedCustomerHashRepository;
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
 * Service that persists customer sync markers and uploads per-country CSVs.
 *
 * <p>This component accepts a list of customers determined to be unsynced, groups them by country,
 * computes and persists a stable row-hash for each customer, renders CSV files per country and
 * uploads those files to S3. The CSV uploads are performed in a transaction hook so that uploaded
 * objects are removed when the surrounding transaction rolls back.
 *
 * <p>Notes:
 *
 * <ul>
 *   <li>Method {@link #syncAndUpload(List)} is {@link Transactional} and registers a {@link
 *       TransactionSynchronization} to perform S3 uploads in {@code beforeCommit} and cleanup in
 *       {@code afterCompletion}.
 *   <li>The service persists {@link SyncedCustomerHash} entries (row hashes) for each processed
 *       customer before attempting S3 uploads.
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerSyncService {

  private final SyncedCustomerHashRepository syncedCustomerHashRepository;
  private final HashService hashService;
  private final CsvService csvService;
  private final S3UploaderService s3Uploader;

  /**
   * Persist sync markers for the provided customers and upload per-country CSVs.
   *
   * <p>Behavior:
   *
   * <ol>
   *   <li>Group {@code unsyncedCustomers} by {@link Customer#getLand()}.
   *   <li>For each customer compute a stable row-hash and persist a {@link SyncedCustomerHash}
   *       entry.
   *   <li>Render a CSV for each country and schedule S3 uploads inside a transaction
   *       synchronization. Uploads happen in {@code beforeCommit} so they only occur when the
   *       transaction successfully commits; uploaded files are deleted in {@code afterCompletion}
   *       if the transaction rolled back.
   * </ol>
   *
   * @param unsyncedCustomers list of customers that need to be synchronized
   */
  @Transactional
  public void syncAndUpload(List<Customer> unsyncedCustomers) {

    Map<String, List<Customer>> byCountry =
        unsyncedCustomers.stream().collect(Collectors.groupingBy(Customer::getLand));

    Map<String, byte[]> csvToBeUploaded = new HashMap<>();

    for (Map.Entry<String, List<Customer>> entry : byCountry.entrySet()) {
      String country = entry.getKey();
      List<Customer> customers = entry.getValue();

      for (Customer customer : customers) {
        String rowHash =
            hashService.customerRowHash(
                customer.getFirmenname(),
                customer.getStrasse(),
                customer.getStrassenzusatz(),
                customer.getOrt(),
                customer.getLand(),
                customer.getPlz(),
                customer.getVorname(),
                customer.getNachname(),
                customer.getEmail(),
                customer.getId().toString());

        syncedCustomerHashRepository.save(
            SyncedCustomerHash.builder().kundenId(customer.getId()).rowHash(rowHash).build());
      }

      List<CustomerCsvDto> customerDtos =
          entry.getValue().stream()
              .map(DomainDataMapper::customerToDto)
              .collect(Collectors.toList());

      byte[] csvBytes = csvService.customersToCsv(customerDtos);

      String filename =
          "kunde_"
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
}
