package com.societyshops.service;

import com.societyshops.entity.Favorite;
import com.societyshops.entity.Shop;
import com.societyshops.entity.User;
import com.societyshops.repository.FavoriteRepository;
import com.societyshops.repository.ShopRepository;
import com.societyshops.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock FavoriteRepository favoriteRepository;
    @Mock ShopRepository shopRepository;
    @Mock UserRepository userRepository;
    @InjectMocks FavoriteService favoriteService;

    @Test
    void addFavorite_success() {
        when(favoriteRepository.existsByUserIdAndShopId(1L, 2L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(shopRepository.findById(2L)).thenReturn(Optional.of(Shop.builder().id(2L).build()));
        when(favoriteRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Favorite result = favoriteService.addFavorite(1L, 2L);
        assertNotNull(result);
    }

    @Test
    void addFavorite_alreadyExists_throws() {
        when(favoriteRepository.existsByUserIdAndShopId(1L, 2L)).thenReturn(true);
        assertThrows(RuntimeException.class, () -> favoriteService.addFavorite(1L, 2L));
    }

    @Test
    void removeFavorite_success() {
        Favorite fav = Favorite.builder().id(1L).build();
        when(favoriteRepository.findByUserIdAndShopId(1L, 2L)).thenReturn(Optional.of(fav));
        favoriteService.removeFavorite(1L, 2L);
        verify(favoriteRepository).delete(fav);
    }

    @Test
    void removeFavorite_notFound_throws() {
        when(favoriteRepository.findByUserIdAndShopId(1L, 2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> favoriteService.removeFavorite(1L, 2L));
    }

    @Test
    void getMyFavorites_returnsList() {
        when(favoriteRepository.findByUserId(1L)).thenReturn(List.of(new Favorite()));
        assertEquals(1, favoriteService.getMyFavorites(1L).size());
    }
}
