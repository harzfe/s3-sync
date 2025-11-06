package com.example.s3sync.integration;

import static org.assertj.core.api.Assertions.*;

import com.example.s3sync.domain.Customer;
import com.example.s3sync.domain.Order;
import com.example.s3sync.domain.SyncedCustomerHash;
import com.example.s3sync.domain.SyncedOrderHash;
import com.example.s3sync.repository.CustomerRepository;
import com.example.s3sync.repository.OrderRepository;
import com.example.s3sync.repository.SyncedCustomerHashRepository;
import com.example.s3sync.repository.SyncedOrderHashRepository;
import com.example.s3sync.scheduling.SyncJob;
import com.example.s3sync.service.HashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

/**
 * Base integration test class that provisions Testcontainers for a PostgreSQL database and
 * LocalStack (S3) and wires the Spring Boot test context. Tests extending this class reuse the
 * provided containers and helpers to interact with S3 and the repositories.
 *
 * <p>Responsibilities and helpers provided:
 *
 * <ul>
 *   <li>Start and expose a Postgres container and LocalStack container.
 *   <li>Set dynamic Spring properties for S3 endpoint/region and disable scheduled tasks during
 *       tests.
 *   <li>Autowired test beans: {@code S3Client}, {@code SyncJob}, repositories and {@code
 *       HashService}.
 *   <li>Lifecycle helper {@link #ensureBucket()} which clears repositories and (re)creates the S3
 *       test bucket before each test.
 *   <li>S3 helpers: {@link #getMetaData(String)}, {@link #getValueOfCsv(String)}, and {@link
 *       #checkIfExists(String)} for assertions against uploaded CSV objects.
 *   <li>Convenience methods to mark domain rows as already synced: {@link
 *       #storeCustomerAsSynced(Customer)} and {@link #storeOrderAsSynced(Order)}.
 * </ul>
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
abstract class BaseIT {

  @Container
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18");

  static final GenericContainer<?> LOCALSTACK = LocalStackContainer.get();

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r) {
    r.add("aws.region", () -> "eu-central-1");
    r.add(
        "aws.s3.endpoint",
        () -> "http://" + LOCALSTACK.getHost() + ":" + LOCALSTACK.getMappedPort(4566));
    r.add("spring.task.scheduling.enabled", () -> "false");
  }

  @Autowired S3Client s3;
  @Autowired SyncJob syncJob;
  @Autowired HashService hashService;
  @Autowired CustomerRepository customerRepository;
  @Autowired OrderRepository orderRepository;
  @Autowired SyncedCustomerHashRepository syncedCustomerRepository;
  @Autowired SyncedOrderHashRepository syncedOrderRepository;

  String bucket = "test-bucket";
  String customerCsvFile = "overwritten";
  String orderCsvFile = "overwritten.csv";

  @BeforeEach
  void ensureBucket() {
    orderRepository.deleteAll();
    customerRepository.deleteAll();
    syncedOrderRepository.deleteAll();
    syncedCustomerRepository.deleteAll();
    try {
      s3.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
    } catch (BucketAlreadyOwnedByYouException | BucketAlreadyExistsException ignored) {
    }
  }

  void checkIfExists(String key) {
    HeadObjectRequest request = HeadObjectRequest.builder().bucket(bucket).key(key).build();

    assertThatThrownBy(() -> s3.headObject(request)).isInstanceOf(NoSuchKeyException.class);
  }

  HeadObjectResponse getMetaData(String key) {
    HeadObjectRequest request = HeadObjectRequest.builder().bucket(bucket).key(key).build();

    return s3.headObject(request);
  }

  String getValueOfCsv(String key) {
    GetObjectRequest request = GetObjectRequest.builder().bucket(bucket).key(key).build();

    return s3.getObjectAsBytes(request).asUtf8String();
  }

  void storeCustomerAsSynced(Customer customer) {
    String hash =
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
            String.valueOf(customer.getId()));
    syncedCustomerRepository.save(
        SyncedCustomerHash.builder().kundenId(customer.getId()).rowHash(hash).build());
  }

  void storeOrderAsSynced(Order order) {
    String hash = hashService.orderMarkerHash(order.getLastchange());
    syncedOrderRepository.save(
        SyncedOrderHash.builder().orderId(order.getId()).markerHash(hash).build());
  }
}
