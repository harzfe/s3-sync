package com.example.s3sync.repository;

import com.example.s3sync.domain.SyncedCustomerHash;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link SyncedCustomerHash} entities.
 *
 * <p>Provides CRUD operations and query support for the table that stores hashes of already-synced
 * customer rows. Use this repository to look up stored hashes, persist new hash entries, and
 * determine whether a customer row needs to be re-synced by comparing hashes.
 *
 * <p>Additional query methods can be added following Spring Data JPA naming conventions if more
 * advanced lookups are required (for example, fetching all rows last synced before a given
 * timestamp).
 */
public interface SyncedCustomerHashRepository extends JpaRepository<SyncedCustomerHash, Long> {}
