package com.example.s3sync.service;

import com.example.s3sync.domain.Customer;
import com.example.s3sync.domain.Order;
import com.example.s3sync.domain.SyncedCustomerHash;
import com.example.s3sync.domain.SyncedOrderHash;
import com.example.s3sync.repository.CustomerRepository;
import com.example.s3sync.repository.OrderRepository;
import com.example.s3sync.repository.SyncedCustomerHashRepository;
import com.example.s3sync.repository.SyncedOrderHashRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service that computes which rows require synchronization.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Scan domain repositories (customers and orders).
 *   <li>Compute stable markers/hashes for each row using {@link HashService}.
 *   <li>Compare computed values against persisted sync markers stored in {@code synced_kunde_hash}
 *       / {@code synced_auftrag_hash}.
 *   <li>Return lists of entities that are either missing in the tracking table or whose computed
 *       marker differs from the persisted value.
 * </ul>
 *
 * <p>Important: these helper methods only detect and return changed rows. They do not persist
 * tracking entries themselves
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
   * Return a list of customers that should be synchronized.
   *
   * <p>This method iterates over all customers and computes a stable row-hash. If no entry exists
   * in the sync-tracking repository or the stored hash does not match the computed one, the
   * customer is included in the returned list.
   *
   * <p>Note: the method does not mutate the tracking table
   *
   * @return list of {@link Customer} entities that require synchronization
   */
  public List<Customer> getUnsyncedCustomers() {
    log.info("Checking for unsynced customers");
    List<Customer> unsyncedCustomers = new ArrayList<>();
    List<Customer> allCustomers = customerRepository.findAll();
    for (Customer customer : allCustomers) {
      if (!syncedCustomerHashRepository.existsById(customer.getId())
          || !checkCustomerHash(customer)) {
        unsyncedCustomers.add(customer);
      }
    }
    log.info("Found {} unsynced customers", unsyncedCustomers.size());
    return unsyncedCustomers;
  }

  /**
   * Return a list of orders that should be synchronized.
   *
   * <p>All orders are scanned and a marker is derived from the order's <code>lastchange</code>
   * value. Orders without a tracking entry or with a differing marker are returned.
   *
   * <p>As with customers, this method does not persist tracking markers
   *
   * @return list of {@link Order} entities that require synchronization
   */
  public List<Order> getUnsyncedOrders() {
    log.info("Checking for unsynced orders");
    List<Order> unsyncedOrders = new ArrayList<>();
    List<Order> allOrders = orderRepository.findAll();
    for (Order order : allOrders) {
      if (!syncedOrderHashRepository.existsById(order.getId()) || !checkOrderHash(order)) {
        unsyncedOrders.add(order);
      }
    }
    log.info("Found {} unsynced orders", unsyncedOrders.size());
    return unsyncedOrders;
  }

  /**
   * Verify whether the persisted customer row hash equals the computed one.
   *
   * <p>This method expects a tracking entry to exist for the provided customer's id; otherwise
   * {@link java.util.NoSuchElementException} will be thrown by the underlying repository call.
   *
   * @param customer the customer to check
   * @return {@code true} when the stored hash matches the computed hash, {@code false} otherwise
   * @throws java.util.NoSuchElementException if no tracking entry exists for the given customer id
   */
  private boolean checkCustomerHash(Customer customer) {
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
            customer.getId().toString());
    SyncedCustomerHash syncedHash =
        syncedCustomerHashRepository.findById(customer.getId()).orElseThrow();
    return syncedHash.getRowHash().equals(hash);
  }

  /**
   * Verify whether the persisted order marker equals the computed marker.
   *
   * <p>As above, a missing tracking entry will cause the repository call to throw {@link
   * java.util.NoSuchElementException}.
   *
   * @param order the order to check
   * @return {@code true} when the stored marker matches the computed marker, {@code false}
   *     otherwise
   * @throws java.util.NoSuchElementException if no tracking entry exists for the given order id
   */
  private boolean checkOrderHash(Order order) {
    String hash = hashService.orderMarkerHash(order.getLastchange());
    SyncedOrderHash syncedHash = syncedOrderHashRepository.findById(order.getId()).orElseThrow();
    return syncedHash.getMarkerHash().equals(hash);
  }
}
