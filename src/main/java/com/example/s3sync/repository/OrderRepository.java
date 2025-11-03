package com.example.s3sync.repository;

import com.example.s3sync.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Order} entities.
 *
 * <p>
 * Extends Spring Data JPA's {@link JpaRepository} to provide CRUD and
 * pagination support for orders. The repository uses {@code String} as the
 * id type because order identifiers are stored as strings. Additional
 * query methods (if needed) can be declared here following Spring Data's
 * method name conventions or using {@code @Query} annotations.
 * </p>
 */
public interface OrderRepository extends JpaRepository<Order, String> {

}
