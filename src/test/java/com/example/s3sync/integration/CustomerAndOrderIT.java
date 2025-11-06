package com.example.s3sync.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

import com.example.s3sync.domain.Customer;
import com.example.s3sync.domain.Order;

import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

public class CustomerAndOrderIT extends BaseIT {

                /**
                 * Integration test that verifies the normal flow where both a customer and
                 * an order exist: both customer and order CSVs should be uploaded. A
                 * subsequent immediate run must not change the uploaded objects (ETag
                 * stays the same) since nothing changed.
                 */
                @Test
                void runITFlow_customer_and_order() {
                Customer customer = customerRepository.save(Customer.builder()
                                .vorname("hello I am in the csv as customer")
                                .nachname("b")
                                .firmenname("c")
                                .strasse("d")
                                .strassenzusatz("e")
                                .plz("f")
                                .ort("g")
                                .land("h")
                                .email("i")
                                .build());
                Order order = orderRepository.save(Order.builder()
                                .kundeid(customer.getId().toString())
                                .created("2025-01-01T00:00:00Z")
                                .lastchange("2025-01-02T00:00:00Z")
                                .artikelnummer("hello I am in the csv as order")
                                .build());

                syncJob.runSyncJob();

                String customerCsvFile = "kunde_" + customer.getLand() + "_"
                                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HH")) + ".csv";

                String orderCsvFile = "auftraege_"
                                + customerRepository.findById(Long.valueOf(order.getKundeid())).orElse(null).getLand()
                                + "_"
                                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HH"))
                                + ".csv";

                HeadObjectResponse heeadObjectResponseCustomerFirstRun = getMetaData(customerCsvFile);
                HeadObjectResponse heeadObjectResponseOrderFirstRun = getMetaData(orderCsvFile);

                assertThat(heeadObjectResponseCustomerFirstRun.contentType()).isEqualTo("text/csv");
                assertThat(heeadObjectResponseOrderFirstRun.contentType()).isEqualTo("text/csv");

                String csvCustomer = getValueOfCsv(customerCsvFile);
                String csvOrder = getValueOfCsv(orderCsvFile);

                assertThat(csvCustomer).contains(customer.getVorname());
                assertThat(csvOrder).contains(order.getArtikelnummer());

                // Second run - immediately after first run - should not change anything
                syncJob.runSyncJob();

                HeadObjectResponse heeadObjectResponseCustomerSecondRun = getMetaData(customerCsvFile);
                HeadObjectResponse heeadObjectResponseOrderSecondRun = getMetaData(orderCsvFile);

                assertThat(heeadObjectResponseCustomerSecondRun.eTag())
                                .isEqualTo(heeadObjectResponseCustomerFirstRun.eTag());
                assertThat(heeadObjectResponseOrderSecondRun.eTag()).isEqualTo(heeadObjectResponseOrderFirstRun.eTag());
        }
}
