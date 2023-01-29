package com.dynatrace.orders.repository;

import com.dynatrace.orders.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByEmail(String email);
    List<Order> findByIsbn(String isbn);
    Order findByEmailAndIsbn(String email, String isbn);
    List<Order> findByCompleted(boolean completed);
}
