package com.example.s3sync.service;

import com.example.s3sync.domain.Customer;
import com.example.s3sync.domain.Order;
import com.example.s3sync.domain.SyncedCustomerHash;
import com.example.s3sync.domain.SyncedOrderHash;
import com.example.s3sync.repository.CustomerRepository;
import com.example.s3sync.repository.OrderRepository;
import com.example.s3sync.repository.SyncedCustomerHashRepository;
import com.example.s3sync.repository.SyncedOrderHashRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncDiffServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private SyncedCustomerHashRepository syncedCustomerRepository;
    @Mock
    private SyncedOrderHashRepository syncedOrderRepository;
    @Mock
    private HashService hashService;

    @InjectMocks
    private SyncDiffService service;

    private Customer customerWithIdOnly(Long id) {
        Customer customer = mock(Customer.class, "Customer" + id);
        when(customer.getId()).thenReturn(id);
        return customer;
    }

    private Customer customerForHash(
            Long id, String firma, String strasse, String strassenzusatz, String ort, String land,
            String plz, String vorname, String nachname, String email) {
        Customer customer = mock(Customer.class, "Customer" + id);
        when(customer.getId()).thenReturn(id);
        when(customer.getFirmenname()).thenReturn(firma);
        when(customer.getStrasse()).thenReturn(strasse);
        when(customer.getStrassenzusatz()).thenReturn(strassenzusatz);
        when(customer.getOrt()).thenReturn(ort);
        when(customer.getLand()).thenReturn(land);
        when(customer.getPlz()).thenReturn(plz);
        when(customer.getVorname()).thenReturn(vorname);
        when(customer.getNachname()).thenReturn(nachname);
        when(customer.getEmail()).thenReturn(email);
        return customer;
    }

    private Order orderWithIdOnly(String id) {
        Order order = mock(Order.class, "Order" + id);
        when(order.getId()).thenReturn(id);
        return order;
    }

    private Order orderForHash(String id, String lastchange) {
        Order order = mock(Order.class, "Order" + id);
        when(order.getId()).thenReturn(id);
        when(order.getLastchange()).thenReturn(lastchange);
        return order;
    }

    /**
     * When the persisted customer row hash equals the freshly computed one,
     * the customer should not be returned as unsynced.
     */
    @Test
    void skipCustomer_whenHashMatches() {
        Customer customer = customerForHash(1L, "a", "b", "c", "d", "e", "f", "g", "h", "i");

        when(customerRepository.findAll()).thenReturn(List.of(customer));
        when(syncedCustomerRepository.existsById(1L)).thenReturn(true);

        when(hashService.customerRowHash(
                "a", "b", "c", "d", "e", "f", "g", "h", "i", "1")).thenReturn("hash-same");

        SyncedCustomerHash alreadyStored = SyncedCustomerHash.builder()
                .kundenId(1L)
                .rowHash("hash-same")
                .build();
        when(syncedCustomerRepository.findById(1L)).thenReturn(Optional.of(alreadyStored));

        List<Customer> result = service.getUnsyncedCustomers();
        assertThat(result).isEmpty();
    }

    /**
     * When the computed customer row hash differs from the stored value the
     * customer must be returned in the list of unsynced customers.
     */
    @Test
    void returnCustomer_whenHashDiffers() {
        Customer customer = customerForHash(2L, "a", "b", "c", "d", "e", "f", "g", "h", "i");

        when(customerRepository.findAll()).thenReturn(List.of(customer));
        when(syncedCustomerRepository.existsById(2L)).thenReturn(true);

        when(hashService.customerRowHash(
                anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn("hash-modified");

        SyncedCustomerHash alreadyStored = SyncedCustomerHash.builder()
                .kundenId(2L)
                .rowHash("hash-stored")
                .build();
        when(syncedCustomerRepository.findById(2L)).thenReturn(Optional.of(alreadyStored));

        List<Customer> result = service.getUnsyncedCustomers();
        assertThat(result).containsExactly(customer);
    }

    /**
     * When a customer has no tracking entry (new customer) it should be
     * considered unsynced and returned; computing a hash is not required
     * in this case.
     */
    @Test
    void returnCustomer_newCustomer() {
        Customer customer = customerWithIdOnly(3L);

        when(customerRepository.findAll()).thenReturn(List.of(customer));
        when(syncedCustomerRepository.existsById(3L)).thenReturn(false);
        List<Customer> result = service.getUnsyncedCustomers();
        verifyNoInteractions(hashService);
        assertThat(result).containsExactly(customer);
    }

    /**
     * When the persisted order marker equals the computed marker the order
     * should be skipped (not returned as unsynced).
     */
    @Test
    void skipOrder_whenMarkerHashMatches() {
        Order order = orderForHash("1", "a");

        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(syncedOrderRepository.existsById("1")).thenReturn(true);
        when(hashService.orderMarkerHash("a")).thenReturn("markerhash-same");

        SyncedOrderHash stored = SyncedOrderHash.builder()
                .orderId("1")
                .markerHash("markerhash-same")
                .build();
        when(syncedOrderRepository.findById("1")).thenReturn(Optional.of(stored));

        List<Order> result = service.getUnsyncedOrders();
        assertThat(result).isEmpty();
    }

    /**
     * When the computed order marker/hash differs from the stored one the
     * order must be returned in the list of unsynced orders.
     */
    @Test
    void returnOrder_whenMarkerHashDiffers() {
        Order order = orderForHash("2", "b");

        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(syncedOrderRepository.existsById("2")).thenReturn(true);
        when(hashService.orderMarkerHash("b")).thenReturn("hash-modified");

        SyncedOrderHash stored = SyncedOrderHash.builder()
                .orderId("2")
                .markerHash("hash-stored")
                .build();
        when(syncedOrderRepository.findById("2")).thenReturn(Optional.of(stored));

        List<Order> result = service.getUnsyncedOrders();
        assertThat(result).containsExactly(order);
    }

    /**
     * When an order has no tracking entry (new order) it should be returned
     * as unsynced and no marker computation should be required.
     */
    @Test
    void returrnOrder_newOrder() {
        Order order = orderWithIdOnly("3");

        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(syncedOrderRepository.existsById("3")).thenReturn(false);

        List<Order> result = service.getUnsyncedOrders();
        verifyNoInteractions(hashService);
        assertThat(result).containsExactly(order);
    }
}
