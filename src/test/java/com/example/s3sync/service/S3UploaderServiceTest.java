package com.example.s3sync.service;

/**
 * Unit tests for {@code S3UploaderService} are intentionally omitted.
 *
 * <p>The uploader is a thin wrapper around the AWS SDK and performs network I/O. Verifying its
 * behavior (content-type, ETag handling, actual upload and deletion semantics) requires integration
 * tests against an S3-like endpoint (for example LocalStack or a real S3 bucket). These integration
 * tests provide more meaningful coverage than isolated unit tests that would need extensive mocking
 * of the SDK.
 */
public class S3UploaderServiceTest {}
