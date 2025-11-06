package com.example.s3sync.scheduling;

import com.example.s3sync.domain.Customer;
import com.example.s3sync.domain.Order;
import com.example.s3sync.service.CustomerSyncService;
import com.example.s3sync.service.OrderSyncService;
import com.example.s3sync.service.SyncDiffService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncJobTest {

    @Mock
    SyncDiffService syncDiffService;
    @Mock
    CustomerSyncService customerSyncService;
    @Mock
    OrderSyncService orderSyncService;

    @InjectMocks
    private SyncJob job;

    /**
     * When there are unsynced customers and orders, the job should delegate
     * to both CustomerSyncService and OrderSyncService to process and upload
     * the respective CSVs.
     */
    @Test
    void runSyncJob_withUnsyncedCustomersAndOrders() {
        List<Customer> customers = List.of(mock(Customer.class), mock(Customer.class));
        List<Order> orders = List.of(mock(Order.class));

        when(syncDiffService.getUnsyncedCustomers()).thenReturn(customers);
        when(syncDiffService.getUnsyncedOrders()).thenReturn(orders);

        job.runSyncJob();

        verify(customerSyncService).syncAndUpload(customers);
        verify(orderSyncService).syncAndUpload(orders);
    }

    /**
     * If there are no unsynced customers but unsynced orders exist, only the
     * OrderSyncService should be invoked; CustomerSyncService must not be
     * called.
     */
    @Test
    void runSyncJob_withNoUnsyncedCustomers_butWithUnsyncedOrders() {
        List<Customer> emptyCustomers = List.of();
        List<Order> orders = List.of(mock(Order.class));

        when(syncDiffService.getUnsyncedCustomers()).thenReturn(emptyCustomers);
        when(syncDiffService.getUnsyncedOrders()).thenReturn(orders);

        job.runSyncJob();

        verify(customerSyncService, never()).syncAndUpload(anyList());
        verify(orderSyncService).syncAndUpload(orders);
    }

    /**
     * If there are no unsynced orders but unsynced customers exist, only the
     * CustomerSyncService should be invoked; OrderSyncService must not be
     * called.
     */
    @Test
    void runSyncJob_withNoUnsyncedOrders_butWithUnsyncedCustomers() {
        List<Customer> customers = List.of(mock(Customer.class));
        List<Order> emptyOrders = List.of();

        when(syncDiffService.getUnsyncedCustomers()).thenReturn(customers);
        when(syncDiffService.getUnsyncedOrders()).thenReturn(emptyOrders);

        job.runSyncJob();

        verify(customerSyncService).syncAndUpload(customers);
        verify(orderSyncService, never()).syncAndUpload(anyList());
    }

    /**
     * When there are no unsynced customers nor orders the job should perform
     * no interactions with the sync services.
     */
    @Test
    void runSyncJob_withNoUnsyncedCustomersOrOrders_doesNothing() {
        when(syncDiffService.getUnsyncedCustomers()).thenReturn(List.of());
        when(syncDiffService.getUnsyncedOrders()).thenReturn(List.of());

        job.runSyncJob();

        verifyNoInteractions(customerSyncService, orderSyncService);
    }
}
