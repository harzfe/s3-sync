package com.example.s3sync.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
/**
 * Configuration for AWS S3 client used by the application.
 *
 * <p>
 * This configuration exposes a {@link S3Client} Spring bean configured via
 * application properties. It supports custom endpoint overrides (useful for
 * LocalStack or S3-compatible endpoints), region configuration and path-style
 * access.
 * </p>
 *
 * Properties (with defaults):
 * <ul>
 * <li><code>aws.region</code> (default: eu-central-1)</li>
 * <li><code>s3.endpoint</code> (default: http://localhost:4566)</li>
 * <li><code>s3.pathStyle</code> (default: true)</li>
 * </ul>
 */
public class AwsS3Config {

    @Value("${aws.region:eu-central-1}")
    /** AWS region used to configure the S3 client (e.g. eu-central-1). */
    private String region;

    @Value("${s3.endpoint:http://localhost:4566}")
    /**
     * Optional custom S3 endpoint. Commonly used to point the client to
     * LocalStack or an S3-compatible service. If empty, the default AWS
     * endpoint for the region will be used.
     */
    private String endpoint;

    @Value("${s3.pathStyle:true}")
    /**
     * Whether to use path-style access for buckets (true) or virtual-hosted-style
     * access (false). Path-style is required for many S3-compatible local
     * implementations such as LocalStack.
     */
    private boolean pathStyle;

    @Bean
    public S3Client s3Client() {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")))
                .serviceConfiguration(
                        S3Configuration.builder()
                                .pathStyleAccessEnabled(pathStyle)
                                .build());

        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        // Build and return the configured S3 client. Credentials are set to
        // dummy values here for local testing; in production, a proper
        // credentials provider should be used.
        return builder.build();
    }
}
