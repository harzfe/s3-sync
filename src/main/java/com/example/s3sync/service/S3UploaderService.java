package com.example.s3sync.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for uploading generated CSV data to S3.
 *
 * <p>
 * This service wraps an {@link S3Client} and provides a small helper to
 * upload a byte array as a CSV object into the configured S3 bucket. The
 * bucket name is injected from the <code>s3.bucket</code> property.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3UploaderService {

    /** AWS S3 client used to perform uploads. */
    private final S3Client s3;

    /** Target S3 bucket, injected from application properties (s3.bucket). */
    @Value("${s3.bucket}")
    private String bucket;

    /**
     * Upload the provided CSV bytes to S3 under the given key.
     *
     * <p>
     * The uploaded object will have the content type <code>text/csv</code>.
     * The method returns the ETag returned by S3 which can be used as a
     * lightweight verification token for the uploaded object.
     * </p>
     *
     * @param csvBytes CSV data encoded as bytes (UTF-8 recommended)
     * @param key      destination object key inside the configured bucket
     * @return the S3 ETag of the uploaded object
     * @throws RuntimeException if the upload fails
     */
    public String uploadCsvBytes(byte[] csvBytes, String key) {
        log.info("Uploading CSV to S3 bucket {} with key {}", bucket, key);
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("text/csv")
                .build();

        PutObjectResponse resp = s3.putObject(req, RequestBody.fromBytes(csvBytes));
        return resp.eTag();
    }
}
