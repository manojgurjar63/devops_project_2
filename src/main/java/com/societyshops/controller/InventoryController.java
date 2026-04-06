package com.societyshops.controller;

import com.societyshops.dto.ApiResponse;
import com.societyshops.dto.InventoryRequest;
import com.societyshops.entity.Inventory;
import com.societyshops.repository.UserRepository;
import com.societyshops.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final UserRepository userRepository;

    @GetMapping("/shop/{shopId}")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<List<Inventory>>> getAvailable(@PathVariable Long shopId) {
        return ResponseEntity.ok(ApiResponse.success("Items fetched", inventoryService.getAvailableItems(shopId)));
    }

    @GetMapping("/shop/{shopId}/all")
    @PreAuthorize("hasRole('SHOPKEEPER')")
    public ResponseEntity<ApiResponse<List<Inventory>>> getAll(@PathVariable Long shopId) {
        return ResponseEntity.ok(ApiResponse.success("All items", inventoryService.getAllItems(shopId)));
    }

    @PostMapping("/shop/{shopId}")
    @PreAuthorize("hasRole('SHOPKEEPER')")
    public ResponseEntity<ApiResponse<Inventory>> addItem(@PathVariable Long shopId,
                                                           @Valid @RequestBody InventoryRequest request,
                                                           Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Item added", inventoryService.addItem(shopId, request, getUserId(auth))));
    }

    @PutMapping("/{itemId}")
    @PreAuthorize("hasRole('SHOPKEEPER')")
    public ResponseEntity<ApiResponse<Inventory>> updateItem(@PathVariable Long itemId,
                                                              @Valid @RequestBody InventoryRequest request,
                                                              Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Item updated", inventoryService.updateItem(itemId, request, getUserId(auth))));
    }

    @PutMapping("/{itemId}/toggle")
    @PreAuthorize("hasRole('SHOPKEEPER')")
    public ResponseEntity<ApiResponse<Inventory>> toggleAvailability(@PathVariable Long itemId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Availability updated", inventoryService.toggleAvailability(itemId, getUserId(auth))));
    }

    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasRole('SHOPKEEPER')")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long itemId, Authentication auth) {
        inventoryService.deleteItem(itemId, getUserId(auth));
        return ResponseEntity.ok(ApiResponse.success("Item deleted", null));
    }

    private Long getUserId(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}
