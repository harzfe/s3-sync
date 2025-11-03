package com.example.s3sync.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object used for CSV export of customer data.
 *
 * <p>
 * This DTO contains the fields that are written to the CSV file when
 * exporting customers. Field names reflect the columns used in the CSV.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCsvDto {

    /** Company name (CSV column: firma). */
    private String firma;

    /** Street name (CSV column: strasse). */
    private String strasse;

    /** Additional street information (CSV column: strassenzusatz). */
    private String strassenzusatz;

    /** City (CSV column: ort). */
    private String ort;

    /** Country (CSV column: land). */
    private String land;

    /** Postal code / ZIP (CSV column: plz). */
    private String plz;

    /** Given name / first name (CSV column: vorname). */
    private String vorname;

    /** Family name / last name (CSV column: nachname). */
    private String nachname;

    /** Email address (CSV column: email). */
    private String email;

    /** Customer id as string (CSV column: kundenId). */
    private String kundenId;
}
