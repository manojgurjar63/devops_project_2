package com.societyshops.repository;

import com.societyshops.entity.Shop;
import com.societyshops.enums.ShopStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    List<Shop> findByIsApproved(Boolean isApproved);
    List<Shop> findByIsApprovedAndStatus(Boolean isApproved, ShopStatus status);
    List<Shop> findByIsApprovedAndCategory(Boolean isApproved, String category);
    List<Shop> findByOwnerId(Long ownerId);
    List<Shop> findByIsApprovedFalse();

    @Query("SELECT s FROM Shop s WHERE s.isApproved = true AND " +
           "(:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%',:search,'%')) OR LOWER(s.category) LIKE LOWER(CONCAT('%',:search,'%')))")
    Page<Shop> findApprovedBySearch(String search, Pageable pageable);
}
