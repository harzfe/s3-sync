package com.example.s3sync.dto;

import lombok.Builder;

/**
 * Data Transfer Object used for CSV export of customer data.
 *
 * <p>
 * This DTO contains the fields that are written to the CSV file when
 * exporting customers. Field names reflect the columns used in the CSV.
 * </p>
 */
@Builder
public record CustomerCsvDto(

    /** Company name (CSV column: firma). */
    String firma,

    /** Street name (CSV column: strasse). */
    String strasse,

    /** Additional street information (CSV column: strassenzusatz). */
    String strassenzusatz,

    /** City (CSV column: ort). */
    String ort,

    /** Country (CSV column: land). */
    String land,

    /** Postal code / ZIP (CSV column: plz). */
    String plz,

    /** Given name / first name (CSV column: vorname). */
    String vorname,

    /** Family name / last name (CSV column: nachname). */
    String nachname,

    /** Email address (CSV column: email). */
    String email,

    /** Customer id as string (CSV column: kundenId). */
    String kundenId
) {}