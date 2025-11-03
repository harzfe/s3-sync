package com.example.s3sync.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.s3sync.domain.Customer;
import com.example.s3sync.domain.Order;
import com.example.s3sync.dto.CustomerCsvDto;
import com.example.s3sync.dto.OrderCsvDto;
import com.example.s3sync.repository.CustomerRepository;
import com.example.s3sync.util.DomainDataMapper;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

/**
 * Orchestrates synchronization workflows for customers and orders.
 *
 * <p>
 * This service coordinates the detection of changed or new domain rows
 * (via {@link SyncDiffService}), maps domain objects to CSV DTOs using
 * {@link com.example.s3sync.util.DomainDataMapper}, renders CSV content with
 * {@link CsvService} and uploads the resulting files to S3 using
 * {@link S3UploaderService}.
 * </p>
 *
 * <p>
 * Synchronization is grouped by country: for each country the service
 * writes a separate CSV file named with the pattern
 * <code>kunde_{country}_{timestamp}.csv</code> or
 * <code>auftraege_{country}_{timestamp}.csv</code>.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final SyncDiffService syncDiffService;
    private final CsvService csvService;
    private final S3UploaderService s3Uploader;
    private final CustomerRepository customerRepository;

    /**
     * Detect and upload all customer rows that need to be synchronized.
     *
     * <p>
     * The method asks {@link SyncDiffService} for unsynced customers, groups
     * them by country and for each group maps domain objects to
     * {@link CustomerCsvDto}, generates a UTF-8 encoded CSV using
     * {@link CsvService} and uploads the file to S3 via
     * {@link S3UploaderService}.
     * </p>
     *
     * <p>
     * If there are no unsynced customers the method returns early.
     * </p>
     */
    public void syncCustomer() {
        log.info("Start syncing customers");
        List<Customer> unsyncedCustomers = syncDiffService.getUnsyncedCustomers();
        if (unsyncedCustomers.isEmpty()) {
            log.info("No unsynced customers found");
            return;
        }

        Map<String, List<Customer>> unsynchedCustomersByCountry = unsyncedCustomers.stream()
                .collect(Collectors.groupingBy(Customer::getLand));

        for (Map.Entry<String, List<Customer>> entry : unsynchedCustomersByCountry.entrySet()) {
            String country = entry.getKey();
            List<CustomerCsvDto> customerDtos = entry.getValue().stream()
                    .map(DomainDataMapper::customerToDto)
                    .collect(Collectors.toList());

            byte[] csvBytes = csvService.customersToCsv(customerDtos);
            s3Uploader.uploadCsvBytes(csvBytes, "kunde_" + country + "_" + LocalDateTime.now() + ".csv");
        }

    }

    /**
     * Detect and upload all order rows that need to be synchronized.
     *
     * <p>
     * Similar to {@link #syncCustomer()}, this method retrieves unsynced
     * orders from {@link SyncDiffService}, groups them by the customer's
     * country (resolved via {@link #getLand(Order)}), maps to
     * {@link OrderCsvDto}, generates CSV bytes and uploads them to S3.
     * </p>
     *
     * <p>
     * If there are no unsynced orders the method returns early.
     * </p>
     */
    public void syncOrders() {
        log.info("Start syncing orders");
        List<Order> unsyncedOrders = syncDiffService.getUnsyncedOrders();
        if (unsyncedOrders.isEmpty()) {
            log.info("No unsynced orders found");
            return;
        }

        Map<String, List<Order>> unsynchedOrdersByCountry = unsyncedOrders.stream()
                .collect(Collectors.groupingBy(this::getLand));

        for (Map.Entry<String, List<Order>> entry : unsynchedOrdersByCountry.entrySet()) {
            String country = entry.getKey();
            List<OrderCsvDto> orderDtos = entry.getValue().stream()
                    .map(DomainDataMapper::orderToDto)
                    .collect(Collectors.toList());

            byte[] csvBytes = csvService.ordersToCsv(orderDtos);
            s3Uploader.uploadCsvBytes(csvBytes, "auftraege_" + country + "_" + LocalDateTime.now() + ".csv");
        }
    }

    /**
     * Resolve the country ('land') for the given order by loading the
     * associated customer and returning its {@link Customer#getLand()} value.
     *
     * <p>
     * Returns {@code null} if the customer cannot be found.
     * </p>
     *
     * @param order order whose customer's country should be returned
     * @return country code/name from the related customer or {@code null}
     */
    private String getLand(Order order) {
        Customer customer = customerRepository.findById(Long.valueOf(order.getKundeid())).orElse(null);
        return customer.getLand();
    }
}
