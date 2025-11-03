package com.example.s3sync.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables Spring's scheduling support for the application.
 *
 * <p>Include this configuration class to activate scheduled tasks declared
 * with {@code @Scheduled} in the application. The class itself does not
 * declare any beans; it only turns on scheduling infrastructure provided by
 * Spring.</p>
 *
 * Usage:
 * <ul>
 *   <li>Annotate any {@code @Component} or {@code @Service} method with
 *       {@code @Scheduled(fixedDelay = ...)} or {@code @Scheduled(cron = ...)}
 *       to schedule tasks.</li>
 *   <li>Keep this class on the classpath (it is a lightweight marker config).</li>
 * </ul>
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}