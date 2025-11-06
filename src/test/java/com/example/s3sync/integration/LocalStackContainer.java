package com.example.s3sync.integration;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * Testcontainers helper that provides a singleton LocalStack container pre-configured for S3
 * testing.
 *
 * <p>This class starts a single LocalStack container (with the S3 service) once for the test JVM
 * and exposes a static {@link #get()} accessor so other test classes (for example {@code BaseIT})
 * can reuse the running container. The container is configured to expose port 4566 and uses an HTTP
 * health check to wait until LocalStack is ready.
 *
 * <p>The private constructor prevents instantiation; the singleton container instance is available
 * via {@link #get()}.
 */
public class LocalStackContainer {

  static final GenericContainer<?> LOCALSTACK =
      new GenericContainer<>("localstack/localstack:latest")
          .withEnv("SERVICES", "s3")
          .withExposedPorts(4566)
          .waitingFor(Wait.forHttp("/_localstack/health").forPort(4566));

  static {
    LOCALSTACK.start();
  }

  public static GenericContainer<?> get() {
    return LOCALSTACK;
  }

  private LocalStackContainer() {}
}
