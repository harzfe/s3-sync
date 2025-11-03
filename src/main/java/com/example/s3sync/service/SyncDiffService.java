package com.example.s3sync.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.s3sync.domain.Customer;
import com.example.s3sync.domain.Order;
import com.example.s3sync.domain.SyncedCustomerHash;
import com.example.s3sync.domain.SyncedOrderHash;
import com.example.s3sync.repository.CustomerRepository;
import com.example.s3sync.repository.OrderRepository;
import com.example.s3sync.repository.SyncedCustomerHashRepository;
import com.example.s3sync.repository.SyncedOrderHashRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service that computes the difference between local domain data and the
 * set of rows already synced to the external system.
 *
 * <p>
 * This service inspects customers and orders stored in the local
 * repositories, compares them against stored sync markers/hashes and
 * returns lists of entities that need to be (re-)synchronized. When an
 * entity is detected as changed or missing in the sync-tracking table, a
 * new tracking entry is created/updated with the current marker/hash so
 * subsequent runs will skip unchanged rows.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncDiffService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final SyncedCustomerHashRepository syncedCustomerHashRepository;
    private final SyncedOrderHashRepository syncedOrderHashRepository;
    private final HashService hashService;

    /**
     * Return a list of customers that need to be synced.
     *
     * <p>
     * The method scans all customers and compares a computed row-hash to
     * the stored value in {@code synced_kunde_hash}. If no stored entry
     * exists or the hash differs, the customer is considered unsynced and
     * included in the returned list. A tracking entry containing the new
     * hash will be saved for each processed customer.
     * </p>
     *
     * @return list of {@link Customer} entities that require synchronization
     */
    @Transactional
    List<Customer> getUnsyncedCustomers() {
        log.info("Checking for unsynced customers");
        List<Customer> unsyncedCustomers = new ArrayList<>();
        List<Customer> allCustomers = customerRepository.findAll();
        for (Customer customer : allCustomers) {
            if (!syncedCustomerHashRepository.existsById(customer.getId()) || !checkCustomerHash(customer)) {
                unsyncedCustomers.add(customer);

                syncedCustomerHashRepository.save(SyncedCustomerHash.builder().kundenId(
                        customer.getId())
                        .rowHash(hashService.customerRowHash(customer.getFirmenname(),
                                customer.getStrasse(),
                                customer.getStrassenzusatz(), customer.getOrt(), customer.getLand(),
                                customer.getPlz(),
                                customer.getVorname(), customer.getNachname(), customer.getEmail(),
                                customer.getId().toString()))
                        .build());
            }
        }
        log.info("Found {} unsynced customers", unsyncedCustomers.size());
        return unsyncedCustomers;
    }

    /**
     * Return a list of orders that need to be synced.
     *
     * <p>
     * Scans all orders and uses a marker/hash derived from the order's
     * <code>lastchange</code> field to decide whether the order changed
     * since the last sync. Missing or changed entries are returned and a
     * tracking marker is persisted for each.
     * </p>
     *
     * @return list of {@link Order} entities that require synchronization
     */
    @Transactional
    List<Order> getUnsyncedOrders() {
        log.info("Checking for unsynced orders");
        List<Order> unsyncedOrders = new ArrayList<>();
        List<Order> allOrders = orderRepository.findAll();
        for (Order order : allOrders) {
            if (!syncedOrderHashRepository.existsById(order.getId()) || !checkOrderHash(order)) {
                unsyncedOrders.add(order);
                syncedOrderHashRepository.save(SyncedOrderHash.builder().orderId(
                        order.getId())
                        .markerHash(hashService.orderMarkerHash(order.getLastchange()))
                        .build());
            }
        }
        log.info("Found {} unsynced orders", unsyncedOrders.size());
        return unsyncedOrders;
    }

    /**
     * Verify whether the stored customer row hash equals the freshly computed
     * hash for the provided customer.
     *
     * @param customer the customer to check
     * @return {@code true} if the stored hash matches the computed hash,
     *         {@code false} otherwise
     */
    private boolean checkCustomerHash(Customer customer) {
        String hash = hashService.customerRowHash(customer.getFirmenname(),
                customer.getStrasse(),
                customer.getStrassenzusatz(),
                customer.getOrt(),
                customer.getLand(),
                customer.getPlz(),
                customer.getVorname(),
                customer.getNachname(),
                customer.getEmail(),
                customer.getId().toString());
        SyncedCustomerHash syncedHash = syncedCustomerHashRepository.findById(customer.getId()).orElseThrow();
        return syncedHash.getRowHash().equals(hash);
    }

    /**
     * Verify whether the stored order marker/hash equals the freshly computed
     * marker for the provided order.
     *
     * @param order the order to check
     * @return {@code true} if the stored marker matches the computed marker,
     *         {@code false} otherwise
     */
    private boolean checkOrderHash(Order order) {
        String hash = hashService.orderMarkerHash(order.getLastchange());
        SyncedOrderHash syncedHash = syncedOrderHashRepository.findById(order.getId()).orElseThrow();
        return syncedHash.getMarkerHash().equals(hash);
    }
}
