package com.example.s3sync.util;

import com.example.s3sync.domain.Customer;
import com.example.s3sync.domain.Order;
import com.example.s3sync.dto.CustomerCsvDto;
import com.example.s3sync.dto.OrderCsvDto;

/**
 * Helper for mapping domain entities to CSV DTOs.
 *
 * <p>
 * This utility provides simple, deterministic mapping functions that
 * transform domain objects into the DTOs used for CSV export. The mapping
 * preserves the field ordering expected by the CSV writer.
 * </p>
 */
public class DomainDataMapper {

    /**
     * Map a {@link Customer} domain object to a {@link CustomerCsvDto}.
     *
     * <p>
     * Fields are copied as-is
     * </p>
     *
     * @param customer domain customer to map
     * @return populated {@link CustomerCsvDto}
     */
    public static CustomerCsvDto customerToDto(Customer customer) {
        return CustomerCsvDto.builder()
                .firma(customer.getFirmenname())
                .strasse(customer.getStrasse())
                .strassenzusatz(customer.getStrassenzusatz())
                .ort(customer.getOrt())
                .land(customer.getLand())
                .plz(customer.getPlz())
                .vorname(customer.getVorname())
                .nachname(customer.getNachname())
                .email(customer.getEmail())
                .kundenId(customer.getId().toString())
                .build();
    }

    /**
     * Map an {@link Order} domain object to an {@link OrderCsvDto}.
     *
     * <p>
     * Order ids and customer ids are kept as strings to match the CSV
     * schema expected by the consumer.
     * </p>
     *
     * @param order domain order to map
     * @return populated {@link OrderCsvDto}
     */
    public static OrderCsvDto orderToDto(Order order) {
        return OrderCsvDto.builder()
                .auftragId(order.getId())
                .artikelnummer(order.getArtikelnummer())
                .kundeId(order.getKundeid())
                .build();
    }
}
