package com.example.s3sync.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.s3sync.domain.SyncedCustomerHash;

/**
 * Repository for {@link SyncedCustomerHash} entities.
 *
 * <p>
 * Provides CRUD operations and query support for the table that stores
 * hashes of already-synced customer rows. Use this repository to look up
 * stored hashes, persist new hash entries, and determine whether a customer
 * row needs to be re-synced by comparing hashes.
 * </p>
 *
 * <p>
 * Additional query methods can be added following Spring Data JPA naming
 * conventions if more advanced lookups are required (for example, fetching
 * all rows last synced before a given timestamp).
 * </p>
 */
public interface SyncedCustomerHashRepository extends JpaRepository<SyncedCustomerHash, Long> {

}
