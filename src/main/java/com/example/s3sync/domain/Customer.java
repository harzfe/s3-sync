package com.example.s3sync.domain;

import jakarta.persistence.*;
import lombok.*;

/** Represents a customer record persisted to the database. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "kunde")
public class Customer {

  /** Primary key (customer id). */
  @Id
  @EqualsAndHashCode.Include
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "kundenid")
  private Long id;

  /** Given first name. */
  @Column(name = "vorname", nullable = false, length = 100)
  private String vorname;

  /** Family name / last name. */
  @Column(name = "nachname", nullable = false, length = 100)
  private String nachname;

  /** Email address. */
  @Column(name = "email", nullable = false, length = 100)
  private String email;

  /** Street name / street address. */
  @Column(name = "strasse", nullable = false, length = 255)
  private String strasse;

  /** Additional street information (e.g. building, floor). */
  @Column(name = "strassenzusatz", nullable = false, length = 255)
  private String strassenzusatz;

  /** City. */
  @Column(name = "ort", nullable = false, length = 255)
  private String ort;

  /** Country. */
  @Column(name = "land", nullable = false, length = 255)
  private String land;

  /** Postal code / ZIP code. */
  @Column(name = "plz", nullable = false, length = 255)
  private String plz;

  /** Company name */
  @Column(name = "firmenname", nullable = false, length = 100)
  private String firmenname;
}
