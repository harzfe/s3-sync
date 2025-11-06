package com.example.s3sync.dto;

import lombok.Builder;

/**
 * Data Transfer Object used for CSV export of order data.
 *
 * <p>
 * This DTO contains the fields that are written to the CSV file when
 * exporting orders. Field names reflect the columns used in the CSV.
 * </p>
 */
@Builder
public record OrderCsvDto(
        /** Order id written to the CSV (column: auftragId). */
        String auftragId,

        /** Product / article number (CSV column: artikelnummer). */
        String artikelnummer,

        /** Customer id associated with the order (CSV column: kundeId). */
        String kundeId) {
}
