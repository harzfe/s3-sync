package com.example.s3sync.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object used for CSV export of orders.
 *
 * <p>
 * This DTO contains the columns that are written to the CSV file when
 * exporting orders. Field names reflect the CSV column names.
 * </p>
 * 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCsvDto {

    /** Order id written to the CSV (column: auftragId). */
    private String auftragId;

    /** Product / article number (CSV column: artikelnummer). */
    private String artikelnummer;

    /** Customer id associated with the order (CSV column: kundeId). */
    private String kundeId;
}
