package com.example.s3sync.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Tracks synced order hashes for detecting changes and avoiding duplicate
 * exports.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "synced_auftrag_hash")
public class SyncedOrderHash {

    /**
     * Order identifier (primary key for this tracking entity).
     */
    @Id
    @Column(name = "auftragid", nullable = false, length = 255)
    private String orderId;

    /**
     * Hash/marker for the exported order row. Used to detect changes.
     * <p>
     * Hex/base64 values fit into the 128 character limit used here.
     * </p>
     */
    @Column(name = "marker_hash", nullable = false, length = 128)
    private String markerHash;

    /**
     * Time when this order was last synced. Automatically updated on persist
     * and update to reflect the last write time for this tracking row.
     */
    @Column(name = "last_synced_at", nullable = false)
    private Instant lastSyncedAt;

    /**
     * Lifecycle callback updating {@link #lastSyncedAt} to the current instant
     * before the entity is persisted or updated. Ensures the timestamp always
     * reflects the last modification time of this tracking row.
     */
    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.lastSyncedAt = Instant.now();
    }
}
