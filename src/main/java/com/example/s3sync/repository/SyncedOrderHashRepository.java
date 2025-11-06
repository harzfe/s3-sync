package com.example.s3sync.repository;

import com.example.s3sync.domain.SyncedOrderHash;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link SyncedOrderHash} entities.
 *
 * <p>This repository provides CRUD and query operations for the table that stores marker/hash
 * information for orders that have been synced to the external system. It is used to determine
 * whether an order needs re-syncing by comparing the stored marker against a newly computed marker.
 *
 * <p>Use this interface to persist new markers, retrieve existing markers by order id, or add
 * custom query methods (e.g. fetch entries last synced before a given timestamp) following Spring
 * Data JPA conventions.
 */
public interface SyncedOrderHashRepository extends JpaRepository<SyncedOrderHash, String> {}
