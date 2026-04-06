package com.societyshops.repository;

import com.societyshops.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserId(Long userId);
    boolean existsByUserIdAndShopId(Long userId, Long shopId);
    Optional<Favorite> findByUserIdAndShopId(Long userId, Long shopId);
}
