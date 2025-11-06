package com.example.s3sync.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.s3sync.domain.Customer;
import com.example.s3sync.domain.Order;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

public class OnlyOrderIT extends BaseIT {

  /**
   * Integration test that verifies when only orders are unsynced (customers already tracked), the
   * job uploads an orders CSV for the customer's country and does not upload a customer CSV. Also
   * verifies the tracked order entry is created.
   */
  @Test
  void runITFlow_onlyOrders() {

    Customer customer =
        customerRepository.save(
            Customer.builder()
                .vorname("a")
                .nachname("b")
                .firmenname("c")
                .strasse("d")
                .strassenzusatz("e")
                .plz("f")
                .ort("g")
                .land("h")
                .email("i")
                .build());

    storeCustomerAsSynced(customer);

    Order order =
        orderRepository.save(
            Order.builder()
                .kundeid(customer.getId().toString())
                .created("2025-01-01T00:00:00Z")
                .lastchange("2025-01-02T00:00:00Z")
                .artikelnummer("Hello I am in the csv")
                .build());

    syncJob.runSyncJob();

    String orderCsvFile =
        "auftraege_"
            + customerRepository.findById(Long.valueOf(order.getKundeid())).orElse(null).getLand()
            + "_"
            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HH"))
            + ".csv";

    HeadObjectResponse headObjectResponse = getMetaData(orderCsvFile);
    assertThat(headObjectResponse.contentType()).isEqualTo("text/csv");
    String csv = getValueOfCsv(orderCsvFile);
    assertThat(csv).contains(order.getArtikelnummer());

    assertThatThrownBy(() -> getMetaData(customerCsvFile)).isInstanceOf(NoSuchKeyException.class);

    assertThat(syncedOrderRepository.count()).isEqualTo(1);
  }
}
