package com.societyshops.controller;

import com.societyshops.dto.ApiResponse;
import com.societyshops.entity.Favorite;
import com.societyshops.repository.UserRepository;
import com.societyshops.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<List<Favorite>>> getMyFavorites(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Favorites fetched", favoriteService.getMyFavorites(getUserId(auth))));
    }

    @PostMapping("/{shopId}")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<Favorite>> addFavorite(@PathVariable Long shopId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Added to favorites", favoriteService.addFavorite(getUserId(auth), shopId)));
    }

    @DeleteMapping("/{shopId}")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(@PathVariable Long shopId, Authentication auth) {
        favoriteService.removeFavorite(getUserId(auth), shopId);
        return ResponseEntity.ok(ApiResponse.success("Removed from favorites", null));
    }

    private Long getUserId(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}
