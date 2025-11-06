package com.example.s3sync.service;

import com.example.s3sync.dto.CustomerCsvDto;
import com.example.s3sync.dto.OrderCsvDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Service that renders DTO lists to CSV byte arrays.
 *
 * <p>
 * This service uses Apache Commons CSV to produce UTF-8 encoded CSV
 * output. The helper methods accept lists of DTOs and return the CSV
 * content as a byte array suitable for uploading to S3 or writing to disk.
 * </p>
 *
 * <p>
 * CSV format details:
 * <ul>
 * <li>Format: {@link CSVFormat#DEFAULT} with LF record separators.</li>
 * <li>Encoding: UTF-8.</li>
 * <li>No header row is produced by the current helpers.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CsvService {

    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder().setRecordSeparator("\n").build();

    /**
     * Create a CSV from a list of {@link CustomerCsvDto} and return it as UTF-8
     * encoded bytes.
     *
     * <p>
     * The CSV contains one record per DTO in the following column order:
     * firma, strasse, strassenzusatz, ort, land, plz, vorname, nachname,
     * kundenId.
     * </p>
     *
     * @param rows the customer rows to write
     * @return CSV data encoded as UTF-8 bytes
     * @throws RuntimeException if CSV generation fails
     */
    public byte[] customersToCsv(List<CustomerCsvDto> rows) {
        log.info("Create customer CSV");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
                CSVPrinter csvPrinter = new CSVPrinter(writer, FORMAT)) {
            for (CustomerCsvDto r : rows) {
                csvPrinter.printRecord(
                        r.getFirma(),
                        r.getStrasse(),
                        r.getStrassenzusatz(),
                        r.getOrt(),
                        r.getLand(),
                        r.getPlz(),
                        r.getVorname(),
                        r.getNachname(),
                        r.getKundenId());
            }
        } catch (Exception e) {
            throw new RuntimeException("Customer CSV generation failed", e);
        }
        log.info("Customer CSV generation successful");
        return baos.toByteArray();
    }

    /**
     * Create a CSV from a list of {@link OrderCsvDto} and return it as UTF-8
     * encoded bytes.
     *
     * <p>
     * The CSV contains one record per DTO in the following column order:
     * auftragId, artikelnummer, kundeId.
     * </p>
     *
     * @param rows the order rows to write
     * @return CSV data encoded as UTF-8 bytes
     * @throws RuntimeException if CSV generation fails
     */
    public byte[] ordersToCsv(List<OrderCsvDto> rows) {
        log.info("Create order CSV");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
                CSVPrinter csvPrinter = new CSVPrinter(writer, FORMAT)) {

            for (OrderCsvDto r : rows) {
                csvPrinter.printRecord(
                        r.getAuftragId(),
                        r.getArtikelnummer(),
                        r.getKundeId());
            }

        } catch (Exception e) {
            throw new RuntimeException("Order CSV generation failed", e);
        }
        log.info("Order CSV generation successful");
        return baos.toByteArray();
    }
}
