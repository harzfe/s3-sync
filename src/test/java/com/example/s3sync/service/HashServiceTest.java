package com.example.s3sync.service;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class HashServiceTest {

  private final HashService svc = new HashService();

  /*
   * Used https://emn178.github.io/online-tools/sha256.html to compute expected
   * hashes
   */
  // "a|b||c|d|e|f|g|h|1"
  private static final String EXPECT_CUSTOMER_HASH =
      "8216357bbee10bfe493bb998aaf2d823195a424c74501ee7005513aabfddc7ac";
  // "2025-11-05T21:15:30Z"
  private static final String EXPECT_ORDER_MARKER_HASH =
      "620b5893fdf75700f00e595de879c351f8ddbccc4a3e77a9c626dea79391f700";

  /**
   * Ensure that customerRowHash trims and normalizes input fields (removes surrounding spaces) and
   * produces the expected SHA-256 hex digest for the input.
   */
  @Test
  void customerRowHash_with_Spaces() {
    String hash =
        svc.customerRowHash(" a ", " b ", " ", " c ", " d ", " e ", " f ", " g ", " h ", " 1 ");
    assertThat(hash).isEqualTo(EXPECT_CUSTOMER_HASH);
  }

  /** Verify that changing one or more fields results in a different customer row hash. */
  @Test
  void customerRowHash_field_changed() {
    String base = svc.customerRowHash("", "b", "", "c", "d", "e", "f", "g", "h", "1");
    String changed = svc.customerRowHash("a", "b", "c", "d", "e", "f", "g", "h", "i", "1");
    assertThat(changed).isNotEqualTo(base);
  }

  /**
   * Ensure that orderMarkerHash trims surrounding whitespace and computes the expected SHA-256
   * digest for a normalized marker string.
   */
  @Test
  void orderMarkerHash_with_Spaces() {
    String hash = svc.orderMarkerHash(" 2025-11-05T21:15:30Z ");
    assertThat(hash).isEqualTo(EXPECT_ORDER_MARKER_HASH);
  }

  /**
   * Verify that modifying the order marker changes the computed marker hash, so marker-based
   * detection will detect changes.
   */
  @Test
  void orderMarkerHash_marker_modified() {
    String base = svc.orderMarkerHash("2025-11-05T21:15:30Z");
    String changed = svc.orderMarkerHash("2025-11-05T21:15:30Z_modified");
    assertThat(changed).isNotEqualTo(base);
  }
}
