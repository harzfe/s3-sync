package com.example.s3sync.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility service that computes stable hashes for domain rows.
 *
 * <p>
 * The service provides helper methods to compute a row-level hash for
 * customers and a marker/hash for orders. Hashes are computed using SHA-256
 * and returned as lower-case hex strings. The produced values are stable for
 * identical input content and suitable for change detection (e.g. comparing
 * against previously stored hashes).
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HashService {

    /**
     * Compute a hash for a customer row using the provided fields.
     *
     * <p>
     * Fields are trimmed and joined with a pipe character (<code>|</code>)
     * before hashing to produce a reproducible single string representation
     * of the row. The returned string is the SHA-256 hex digest of that
     * representation.
     * </p>
     *
     * @param firma          company name
     * @param strasse        street
     * @param strassenzusatz street addition
     * @param ort            city
     * @param land           country
     * @param plz            postal code
     * @param vorname        given name
     * @param nachname       family name
     * @param email          email address
     * @param kundenId       customer id
     * @return hex-encoded SHA-256 digest representing the customer row
     */
    public String customerRowHash(
            String firma,
            String strasse,
            String strassenzusatz,
            String ort,
            String land,
            String plz,
            String vorname,
            String nachname,
            String email,
            String kundenId) {

        String joined = String.join("|",
                firma.trim(), strasse.trim(), strassenzusatz.trim(), ort.trim(), land.trim(),
                plz.trim(), vorname.trim(), nachname.trim(), email.trim(), kundenId.trim());

        return hashOfFields(joined);
    }

    /**
     * Compute a marker hash for an order row.
     *
     * <p>
     * Currently the marker is derived from the <code>lastchange</code>
     * timestamp string (trimmed) and hashed using SHA-256. This provides a
     * compact representation that can be compared to detect changes in the
     * order state.
     * </p>
     *
     * @param lastchange the last-change timestamp or marker string from the
     *                   upstream system
     * @return hex-encoded SHA-256 digest representing the order marker
     */
    public String orderMarkerHash(String lastchange) {
        return hashOfFields(lastchange.trim());
    }

    /**
     * Compute SHA-256 hex digest of the given input string (UTF-8).
     *
     * @param input the input string to hash
     * @return lower-case hex representation of the SHA-256 digest
     */
    private String hashOfFields(String input) {
        try {
            log.debug("Hashing {}", input);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }
}
