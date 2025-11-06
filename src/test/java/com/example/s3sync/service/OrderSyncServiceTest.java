package com.example.s3sync.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.s3sync.domain.Order;
import com.example.s3sync.domain.SyncedOrderHash;
import com.example.s3sync.dto.OrderCsvDto;
import com.example.s3sync.repository.CustomerRepository;
import com.example.s3sync.repository.SyncedOrderHashRepository;
import java.util.List;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
public class OrderSyncServiceTest {

  @Mock SyncedOrderHashRepository syncedOrderHashRepository;
  @Mock HashService hashService;
  @Mock CsvService csvService;
  @Mock S3UploaderService s3Uploader;
  @Mock CustomerRepository customerRepository;

  @Spy @InjectMocks OrderSyncService service;

  @BeforeEach
  void enableTransactionSync() {
    TransactionSynchronizationManager.initSynchronization();
  }

  @AfterEach
  void disableTransactionSync() {
    TransactionSynchronizationManager.clearSynchronization();
  }

  private Order createOrder(String id, String lastchange) {
    Order mockedOrder = mock(Order.class);
    when(mockedOrder.getId()).thenReturn(id);
    when(mockedOrder.getLastchange()).thenReturn(lastchange);
    return mockedOrder;
  }

  /**
   * Persist marker hashes for each unsynced order and save a corresponding tracking entry per
   * order.
   */
  @Test
  void persistsUnsyncedOrders() {
    List<Order> orders =
        List.of(
            createOrder("1", "2024-01-01T12:00:00"),
            createOrder("2", "2024-01-01T13:00:00"),
            createOrder("3", "2024-01-01T14:00:00"));

    when(hashService.orderMarkerHash(anyString())).thenReturn("hash1", "hash2", "hash3");

    Mockito.doReturn("DE", "DE", "US").when(service).getLand(Mockito.any());
    service.syncAndUpload(orders);

    verify(hashService, times(3)).orderMarkerHash(anyString());

    ArgumentCaptor<SyncedOrderHash> saved = ArgumentCaptor.forClass(SyncedOrderHash.class);
    verify(syncedOrderHashRepository, times(3)).save(saved.capture());
    assertThat(saved.getAllValues())
        .extracting(SyncedOrderHash::getOrderId, SyncedOrderHash::getMarkerHash)
        .containsExactlyInAnyOrder(
            Tuple.tuple("1", "hash1"), Tuple.tuple("2", "hash2"), Tuple.tuple("3", "hash3"));
  }

  /**
   * Ensure orders are grouped by the customer country (via getLand) so that separate CSVs are
   * produced per country.
   */
  @Test
  void groupsByCountry() {
    List<Order> orders =
        List.of(
            createOrder("1", "2024-01-01T12:00:00"),
            createOrder("2", "2024-01-01T13:00:00"),
            createOrder("3", "2024-01-01T14:00:00"));

    Mockito.doReturn("DE", "DE", "US").when(service).getLand(Mockito.any());
    service.syncAndUpload(orders);

    ArgumentCaptor<List<OrderCsvDto>> csvArg = ArgumentCaptor.forClass(List.class);
    verify(csvService, times(2)).ordersToCsv(csvArg.capture());
    List<List<OrderCsvDto>> allDtos = csvArg.getAllValues();
    assertThat(allDtos).hasSize(2);
    assertThat(allDtos.stream().map(List::size)).containsExactlyInAnyOrder(2, 1);
  }
}
