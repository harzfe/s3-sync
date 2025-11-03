package com.example.s3sync.domain;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents an order record persisted to the database.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "auftraege")
public class Order {

    /**
     * Order identifier (primary key).
     */
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue
    @UuidGenerator
    @Column(name = "auftragid", nullable = false, length = 255)
    private String id;

    /** Article / product number associated with this order. */
    @Column(name = "artikelnummer", nullable = false, length = 255)
    private String artikelnummer;

    /** Creation timestamp as represented by the upstream system (string). */
    @Column(name = "created", nullable = false, length = 255)
    private String created;

    /** Last modification timestamp as string. */
    @Column(name = "lastchange", nullable = false, length = 255)
    private String lastchange;

    /** Customer (kundeid). Stored as String. */
    @Column(name = "kundeid", nullable = false, length = 255)
    private String kundeid;
}
