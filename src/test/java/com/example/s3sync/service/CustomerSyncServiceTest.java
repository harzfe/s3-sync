package com.example.s3sync.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.s3sync.domain.Customer;
import com.example.s3sync.domain.SyncedCustomerHash;
import com.example.s3sync.dto.CustomerCsvDto;
import com.example.s3sync.repository.SyncedCustomerHashRepository;
import java.util.List;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class CustomerSyncServiceUnitTest {

  @Mock SyncedCustomerHashRepository syncedCustomerHashRepository;
  @Mock HashService hashService;
  @Mock CsvService csvService;
  @Mock S3UploaderService s3Uploader;

  @InjectMocks CustomerSyncService service;

  @BeforeEach
  void enableTransactionSync() {
    TransactionSynchronizationManager.initSynchronization();
  }

  @AfterEach
  void disableTransactionSync() {
    TransactionSynchronizationManager.clearSynchronization();
  }

  private Customer createCustomer(long id, String land, String name) {
    Customer mockedCustomer = mock(Customer.class);
    when(mockedCustomer.getId()).thenReturn(id);
    when(mockedCustomer.getLand()).thenReturn(land);
    when(mockedCustomer.getFirmenname()).thenReturn(name);
    return mockedCustomer;
  }

  /**
   * Persist computed row-hashes for each unsynced customer and save a corresponding tracking entry
   * per customer.
   */
  @Test
  void persistsUnsyncedCustomers() {
    List<Customer> customers =
        List.of(
            createCustomer(1, "DE", "ascasc"),
            createCustomer(2, "DE", "sadasd"),
            createCustomer(3, "US", "asfafsaf"));

    when(hashService.customerRowHash(
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn("hash1", "hash2", "hash3");

    service.syncAndUpload(customers);

    verify(hashService, times(3))
        .customerRowHash(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());

    ArgumentCaptor<SyncedCustomerHash> saved = ArgumentCaptor.forClass(SyncedCustomerHash.class);
    verify(syncedCustomerHashRepository, times(3)).save(saved.capture());
    assertThat(saved.getAllValues())
        .extracting(SyncedCustomerHash::getKundenId, SyncedCustomerHash::getRowHash)
        .containsExactlyInAnyOrder(
            Tuple.tuple(1L, "hash1"), Tuple.tuple(2L, "hash2"), Tuple.tuple(3L, "hash3"));
  }

  /**
   * Ensure customers are grouped by their country code so that a separate CSV is created/uploaded
   * per country.
   */
  @Test
  void groupsByCountry() {
    List<Customer> customers =
        List.of(
            createCustomer(1, "DE", "ascasc"),
            createCustomer(2, "DE", "sadasd"),
            createCustomer(3, "US", "asfafsaf"));

    service.syncAndUpload(customers);

    ArgumentCaptor<List<CustomerCsvDto>> csvArg = ArgumentCaptor.forClass(List.class);
    verify(csvService, times(2)).customersToCsv(csvArg.capture());
    List<List<CustomerCsvDto>> allDtos = csvArg.getAllValues();
    assertThat(allDtos).hasSize(2);
    assertThat(allDtos.stream().map(List::size)).containsExactlyInAnyOrder(2, 1);
  }
}
