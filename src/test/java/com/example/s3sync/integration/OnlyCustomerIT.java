package com.example.s3sync.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.s3sync.domain.Customer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

public class OnlyCustomerIT extends BaseIT {

  /**
   * Integration test that verifies when new customers exist (and no orders), the job uploads a
   * customer CSV for the appropriate country and does not produce an orders CSV. Also verifies the
   * customer tracking entries are persisted.
   */
  @Test
  void runITFlow_onlyCustomers() {
    Customer customer1 =
        customerRepository.save(
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
                    .build()));
    Customer customer2 =
        customerRepository.save(
            customerRepository.save(
                Customer.builder()
                    .vorname("hello I am in the csv")
                    .nachname("k")
                    .firmenname("l")
                    .strasse("m")
                    .strassenzusatz("n")
                    .plz("o")
                    .ort("p")
                    .land("q")
                    .email("r")
                    .build()));

    String customerCsvFile =
        "kunde_"
            + customer2.getLand()
            + "_"
            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HH"))
            + ".csv";

    storeCustomerAsSynced(customer1);
    assertThat(syncedCustomerRepository.count()).isEqualTo(1);

    syncJob.runSyncJob();

    assertThat(orderRepository.count()).isZero();

    HeadObjectResponse headObjectResoponse = getMetaData(customerCsvFile);
    assertThat(headObjectResoponse.contentType()).isEqualTo("text/csv");
    String csv = getValueOfCsv(customerCsvFile);
    assertThat(csv).contains(customer2.getVorname());

    assertThatThrownBy(() -> getMetaData(orderCsvFile)).isInstanceOf(NoSuchKeyException.class);

    assertThat(syncedCustomerRepository.count()).isEqualTo(2);
  }
}
