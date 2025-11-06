package com.example.s3sync.domain;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

/** Stores a hash for a customer row that has been synced. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "synced_kunde_hash")
public class SyncedCustomerHash {

  /** Customer id */
  @Id
  @Column(name = "kundenid", nullable = false)
  private Long kundenId;

  /**
   * Hex-encoded row hash. Used to detect changes in the exported customer representation. Length
   * limited to 128 characters to allow common hash encodings (SHA-1/256/512 in hex/base64, etc.).
   */
  @Column(name = "row_hash", nullable = false, length = 128)
  private String rowHash;

  /**
   * Timestamp of the last successful sync for this row.
   *
   * <p>The field is automatically updated on persist and update to the current instant.
   */
  @Column(name = "last_synced_at", nullable = false)
  private Instant lastSyncedAt;

  /**
   * Lifecycle callback that updates {@link #lastSyncedAt} to the current instant before the entity
   * is persisted or updated. This ensures the timestamp reflects the last change to this tracking
   * row.
   */
  @PrePersist
  @PreUpdate
  public void updateTimestamp() {
    this.lastSyncedAt = Instant.now();
  }
}
