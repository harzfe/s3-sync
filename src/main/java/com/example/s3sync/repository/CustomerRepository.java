package com.example.s3sync.repository;

import com.example.s3sync.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Customer} entities.
 *
 * <p>This interface extends Spring Data JPA's {@link JpaRepository} to provide
 * CRUD operations and pagination for {@code Customer} entities. Additional
 * query methods (if needed) can be declared here following Spring Data's
 * method name conventions or using {@code @Query} annotations.</p>
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {

}
