package com.societyshops.service;

import com.societyshops.dto.InventoryRequest;
import com.societyshops.entity.Inventory;
import com.societyshops.entity.Shop;
import com.societyshops.entity.User;
import com.societyshops.repository.InventoryRepository;
import com.societyshops.repository.ShopRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock InventoryRepository inventoryRepository;
    @Mock ShopRepository shopRepository;
    @InjectMocks InventoryService inventoryService;

    private User owner(Long id) {
        return User.builder().id(id).build();
    }

    private Shop shop(Long id, User owner) {
        return Shop.builder().id(id).owner(owner).build();
    }

    private Inventory item(Long id, Shop shop, boolean available) {
        return Inventory.builder().id(id).shop(shop).name("Rice").price(BigDecimal.TEN).isAvailable(available).build();
    }

    private InventoryRequest request() {
        InventoryRequest r = new InventoryRequest();
        r.setName("Rice");
        r.setPrice(BigDecimal.TEN);
        return r;
    }

    @Test
    void addItem_success() {
        User owner = owner(1L);
        Shop shop = shop(10L, owner);

        when(shopRepository.findById(10L)).thenReturn(Optional.of(shop));
        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Inventory result = inventoryService.addItem(10L, request(), 1L);

        assertThat(result.getName()).isEqualTo("Rice");
        assertThat(result.getIsAvailable()).isTrue();
    }

    @Test
    void addItem_wrongOwner_throws() {
        User owner = owner(1L);
        Shop shop = shop(10L, owner);

        when(shopRepository.findById(10L)).thenReturn(Optional.of(shop));

        assertThatThrownBy(() -> inventoryService.addItem(10L, request(), 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("You do not own this shop");
    }

    @Test
    void toggleAvailability_trueToFalse() {
        User owner = owner(1L);
        Shop shop = shop(10L, owner);
        Inventory item = item(5L, shop, true);

        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Inventory result = inventoryService.toggleAvailability(5L, 1L);

        assertThat(result.getIsAvailable()).isFalse();
    }

    @Test
    void deleteItem_success() {
        User owner = owner(1L);
        Shop shop = shop(10L, owner);
        Inventory item = item(5L, shop, true);

        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(item));

        inventoryService.deleteItem(5L, 1L);

        verify(inventoryRepository).delete(item);
    }

    @Test
    void deleteItem_wrongOwner_throws() {
        User owner = owner(1L);
        Shop shop = shop(10L, owner);
        Inventory item = item(5L, shop, true);

        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> inventoryService.deleteItem(5L, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("You do not own this item");
    }

    @Test
    void updateItem_success() {
        User owner = owner(1L);
        Shop shop = shop(10L, owner);
        Inventory item = item(5L, shop, true);

        InventoryRequest req = request();
        req.setName("Wheat");

        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Inventory result = inventoryService.updateItem(5L, req, 1L);

        assertThat(result.getName()).isEqualTo("Wheat");
    }
}
