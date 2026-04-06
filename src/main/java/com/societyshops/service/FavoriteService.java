package com.societyshops.service;

import com.societyshops.entity.Favorite;
import com.societyshops.entity.Shop;
import com.societyshops.entity.User;
import com.societyshops.repository.FavoriteRepository;
import com.societyshops.repository.ShopRepository;
import com.societyshops.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    public Favorite addFavorite(Long userId, Long shopId) {
        if (favoriteRepository.existsByUserIdAndShopId(userId, shopId)) {
            throw new RuntimeException("Shop already in favorites");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        return favoriteRepository.save(Favorite.builder().user(user).shop(shop).build());
    }

    public void removeFavorite(Long userId, Long shopId) {
        Favorite favorite = favoriteRepository.findByUserIdAndShopId(userId, shopId)
                .orElseThrow(() -> new RuntimeException("Favorite not found"));
        favoriteRepository.delete(favorite);
    }

    public List<Favorite> getMyFavorites(Long userId) {
        return favoriteRepository.findByUserId(userId);
    }
}
