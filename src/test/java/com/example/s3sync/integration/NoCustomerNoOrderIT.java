package com.example.s3sync.integration;

import static org.assertj.core.api.Assertions.*;

import com.example.s3sync.domain.Customer;
import com.example.s3sync.domain.Order;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

class NoCustomerNoOrderIT extends BaseIT {

  /**
   * Integration test that verifies when all customers and orders are already tracked (synced), the
   * job does not upload customer or order CSVs and the counts of tracking entries remain unchanged.
   */
  @Test
  void runITFlow_noCustomers_noOrders() {
    Customer customer1 =
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
    Customer customer2 =
        customerRepository.save(
            Customer.builder()
                .vorname("j")
                .nachname("k")
                .firmenname("l")
                .strasse("m")
                .strassenzusatz("n")
                .plz("o")
                .ort("p")
                .land("q")
                .email("r")
                .build());

    Order order =
        orderRepository.save(
            Order.builder()
                .kundeid(customer1.getId().toString())
                .created("2025-01-01T00:00:00Z")
                .lastchange("2025-01-01T00:00:00Z")
                .artikelnummer("XYZ123")
                .build());

    storeCustomerAsSynced(customer1);
    storeCustomerAsSynced(customer2);
    storeOrderAsSynced(order);

    syncJob.runSyncJob();

    assertThatThrownBy(() -> getMetaData(customerCsvFile)).isInstanceOf(NoSuchKeyException.class);
    assertThatThrownBy(() -> getMetaData(orderCsvFile)).isInstanceOf(NoSuchKeyException.class);

    assertThat(syncedCustomerRepository.count()).isEqualTo(2);
    assertThat(syncedOrderRepository.count()).isEqualTo(1);
  }
}
