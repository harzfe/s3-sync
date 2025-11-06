package com.example.s3sync.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.s3sync.dto.CustomerCsvDto;
import com.example.s3sync.dto.OrderCsvDto;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

public class CsvServiceTest {

  private final CsvService csvService = new CsvService();

  /**
   * Verify that a list of {@link CustomerCsvDto} is rendered to CSV with the expected column order
   * and UTF-8 encoding. The test asserts the exact output.
   */
  @Test
  void CustomerCsvDto_writesToCsv_inExpectedFormat() {
    List<CustomerCsvDto> customerCsvDtos =
        List.of(
            CustomerCsvDto.builder()
                .firma("Contargo GmbH & Co. KG")
                .strasse("Werfthallenstraße 9")
                .strassenzusatz("")
                .ort("Mannheim")
                .land("DE")
                .plz("68159")
                .vorname("F")
                .nachname("H")
                .kundenId("1")
                .build(),
            CustomerCsvDto.builder()
                .firma("Rhenus SE & Co. KG")
                .strasse("Wattstrasse 1")
                .strassenzusatz("iwas")
                .ort("Mannheim")
                .land("DE")
                .plz("68199")
                .vorname("A")
                .nachname("K")
                .kundenId("2")
                .build());

    byte[] bytes = csvService.customersToCsv(customerCsvDtos);
    String csv = new String(bytes, StandardCharsets.UTF_8);

    assertThat(csv)
        .isEqualTo(
            String.join(
                "\n",
                "Contargo GmbH & Co. KG,Werfthallenstraße 9,,Mannheim,DE,68159,F,H,1",
                "Rhenus SE & Co. KG,Wattstrasse 1,iwas,Mannheim,DE,68199,A,K,2",
                ""));
  }

  /**
   * Verify that a list of {@link OrderCsvDto} is rendered to CSV with the expected column order
   * (auftragId, artikelnummer, kundeId).
   */
  @Test
  void orderCsvDto_writesToCsv_inExpectedFormat() {
    List<OrderCsvDto> csvRows =
        List.of(
            OrderCsvDto.builder().auftragId("1").artikelnummer("123").kundeId("1").build(),
            OrderCsvDto.builder().auftragId("2").artikelnummer("456").kundeId("2").build());

    byte[] bytes = csvService.ordersToCsv(csvRows);
    String csv = new String(bytes, StandardCharsets.UTF_8);

    assertThat(csv).isEqualTo(String.join("\n", "1,123,1", "2,456,2", ""));
  }
}
