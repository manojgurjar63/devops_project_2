package com.societyshops.repository;

import com.societyshops.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByShopId(Long shopId);
    List<Inventory> findByShopIdAndIsAvailable(Long shopId, Boolean isAvailable);
}
