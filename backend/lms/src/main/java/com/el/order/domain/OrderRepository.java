package com.el.order.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends CrudRepository<Order, UUID> {

    Page<Order> findAll(Pageable pageable);

    @Query("""
        SELECT * 
        FROM 
            orders 
        WHERE created_by = :createdBy 
        LIMIT :size OFFSET :page * :size
    """)
    List<Order> findAllByCreatedBy(String createdBy, int page, int size);

    Optional<Order> findByCreatedByAndId(String createdBy, UUID id);

    @Query("""
        SELECT 
            COUNT(*) > 0 
        FROM 
            orders o
        JOIN order_items oi ON o.id = oi.orders
        WHERE o.created_by = :username 
          AND oi.course = :courseId
    """)
    boolean hasPurchasedCourse(@Param("courseId") Long courseId, @Param("username") String username);

}
