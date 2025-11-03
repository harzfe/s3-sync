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
 * Service responsible for converting lists of DTOs into CSV byte arrays.
 *
 * <p>
 * The service uses Apache Commons CSV to produce UTF-8 encoded CSV data
 * which can be uploaded to S3 or saved locally. Methods are small helpers
 * that accept DTO lists and return the generated CSV as a byte array.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CsvService {

    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder().build();

    /**
     * Create a CSV from a list of {@link CustomerCsvDto} and return it as a
     * UTF-8 encoded byte array.
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
     * Create a CSV from a list of {@link OrderCsvDto} and return it as a
     * UTF-8 encoded byte array.
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
