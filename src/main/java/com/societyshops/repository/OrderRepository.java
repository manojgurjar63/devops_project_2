package com.societyshops.repository;

import com.societyshops.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByResidentIdOrderByCreatedAtDesc(Long residentId);
    List<Order> findByShopIdOrderByCreatedAtDesc(Long shopId);
}
