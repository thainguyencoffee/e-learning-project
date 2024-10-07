package com.el.order.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends CrudRepository<Order, UUID> {

    Page<Order> findAll(Pageable pageable);

    @Query("SELECT * FROM orders WHERE created_by = :createdBy LIMIT :size OFFSET :page * :size")
    List<Order> findAllByCreatedBy(String createdBy, int page, int size);


}
