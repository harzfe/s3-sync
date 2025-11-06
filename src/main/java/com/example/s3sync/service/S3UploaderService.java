package com.example.s3sync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

/**
 * Service responsible for uploading generated CSV data to S3.
 *
 * <p>This service wraps an {@link S3Client} and provides convenience methods to upload and delete
 * objects in the configured bucket. It's used by the synchronization pipeline to persist CSV
 * exports.
 *
 * <p>Configuration:
 *
 * <ul>
 *   <li><code>aws.s3.bucket</code> - target bucket for CSV uploads
 * </ul>
 *
 * <p>Notes:
 *
 * <ul>
 *   <li>Uploaded objects are set with content type <code>text/csv</code>.
 *   <li>The upload method returns the S3 ETag which can be used as a lightweight verification
 *       token.
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3UploaderService {

  /** AWS S3 client used to perform uploads. */
  private final S3Client s3;

  /** Target S3 bucket, injected from application properties (<code>aws.s3.bucket</code>). */
  @Value("${aws.s3.bucket}")
  private String bucket;

  /**
   * Upload the provided CSV bytes to S3 under the given object key.
   *
   * <p>The uploaded object will be stored in the configured bucket with content type <code>text/csv
   * </code>. This method returns the ETag reported by S3 which can be used as a simple verification
   * or logging token.
   *
   * @param csvBytes CSV content as a byte array (UTF-8 encoding)
   * @param file destination object key inside the configured bucket
   * @return the S3 ETag of the uploaded object
   * @throws software.amazon.awssdk.core.exception.SdkException if the upload fails at the SDK level
   */
  public String uploadCsvBytes(byte[] csvBytes, String file) {
    log.info("Uploading CSV to S3 bucket {} with key {}", bucket, file);
    PutObjectRequest req =
        PutObjectRequest.builder().bucket(bucket).key(file).contentType("text/csv").build();

    PutObjectResponse resp = s3.putObject(req, RequestBody.fromBytes(csvBytes));
    return resp.eTag();
  }

  /**
   * Delete the object with the given key from the configured bucket.
   *
   * <p>On SDK-level failures an {@link software.amazon.awssdk.core.exception.SdkException} is
   * propagated. Other unexpected exceptions are re-thrown as well after being logged.
   *
   * @param file object key to delete from the bucket
   * @throws software.amazon.awssdk.core.exception.SdkException on S3 SDK errors
   */
  public void delete(String file) {
    log.info("Deleting S3 object {} from bucket {}", file, bucket);

    try {
      DeleteObjectRequest req = DeleteObjectRequest.builder().bucket(bucket).key(file).build();

      s3.deleteObject(req);
      log.info("Successfully deleted object {} from S3", file);
    } catch (SdkException e) {
      log.error("Failed to delete object {} from S3: {}", file, e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error while deleting S3 object {}", file, e);
      throw e;
    }
  }
}
