package com.example.s3sync.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

/**
 * Spring configuration that provides a configured {@link S3Client} bean.
 *
 * <p>This configuration reads a small set of application properties to configure an AWS S3 client
 * used to upload CSV files. It supports an optional custom endpoint (for LocalStack or
 * S3-compatible services), region selection, and toggling path-style access.
 *
 * <p>Configuration properties used:
 *
 * <ul>
 *   <li><code>aws.region</code> - AWS region (default: <code>eu-central-1</code>).
 *   <li><code>aws.s3.endpoint</code> - Optional custom S3 endpoint (default: <code>
 *       http://localhost:4566</code>).
 *   <li><code>aws.s3.pathStyle</code> - Use path-style bucket access when <code>true</code>
 *       (default: <code>true</code>).
 *   <li><code>aws.accessKey</code> - Access key used when a custom endpoint is configured.
 *   <li><code>aws.secretKey</code> - Secret key used when a custom endpoint is configured.
 *   <li><code>aws.profile</code> - AWS CLI profile name used when no custom endpoint is configured
 *       (default: <code>default</code>).
 * </ul>
 */
@Configuration
public class AwsS3Config {

  /** AWS region used to configure the S3 client (e.g. "eu-central-1"). */
  @Value("${aws.region:eu-central-1}")
  private String region;

  /**
   * Optional custom S3 endpoint (e.g. LocalStack). When set and non-blank the client will use an
   * endpoint override and the configured access/secret keys instead of the profile-based
   * credentials provider.
   */
  @Value("${aws.s3.endpoint}")
  private String endpoint;

  /**
   * Whether to use path-style access (true) or virtual-hosted-style access (false). Path-style is
   * commonly required for local S3-compatible stacks.
   */
  @Value("${aws.s3.pathStyle}")
  private boolean pathStyle;

  /** Access key used when a custom endpoint is configured. */
  @Value("${aws.accessKey:test}")
  private String accessKey;

  /** Secret key used when a custom endpoint is configured. */
  @Value("${aws.secretKey:test}")
  private String secretKey;

  /** AWS CLI profile name used when no custom endpoint is configured. */
  @Value("${aws.profile:default}")
  private String awsProfile;

  /**
   * Create and configure the {@link S3Client} used by the application.
   *
   * <p>Behavior:
   *
   * <ul>
   *   <li>If {@code aws.s3.endpoint} is non-blank the client will use a static credentials provider
   *       built from {@code aws.accessKey} and {@code aws.secretKey} and the endpoint will be
   *       overridden (useful for LocalStack).
   *   <li>Otherwise the client will use {@link ProfileCredentialsProvider} with the configured
   *       {@code aws.profile} value.
   * </ul>
   *
   * <p>The client is also configured with path-style access according to the {@code
   * aws.s3.pathStyle} property.
   *
   * @return a configured {@link S3Client}
   */
  @Bean
  public S3Client s3Client() {
    boolean useCustomEndpoint = endpoint != null && !endpoint.isBlank();

    AwsCredentialsProvider credentialsProvider =
        useCustomEndpoint
            ? StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))
            : ProfileCredentialsProvider.create(awsProfile);

    S3Configuration s3cfg = S3Configuration.builder().pathStyleAccessEnabled(pathStyle).build();

    S3ClientBuilder builder =
        S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(credentialsProvider)
            .serviceConfiguration(s3cfg);

    if (useCustomEndpoint) {
      builder = builder.endpointOverride(URI.create(endpoint));
    }

    return builder.build();
  }
}
