package com.societyshops.service;

import com.societyshops.dto.InventoryRequest;
import com.societyshops.entity.Inventory;
import com.societyshops.entity.Shop;
import com.societyshops.repository.InventoryRepository;
import com.societyshops.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ShopRepository shopRepository;

    public Inventory addItem(Long shopId, InventoryRequest request, Long ownerId) {
        Shop shop = getShopOwnedBy(shopId, ownerId);

        Inventory item = Inventory.builder()
                .shop(shop)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .unit(request.getUnit())
                .isAvailable(true)
                .build();

        return inventoryRepository.save(item);
    }

    public Inventory updateItem(Long itemId, InventoryRequest request, Long ownerId) {
        Inventory item = getItemOwnedBy(itemId, ownerId);
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setUnit(request.getUnit());
        return inventoryRepository.save(item);
    }

    public Inventory toggleAvailability(Long itemId, Long ownerId) {
        Inventory item = getItemOwnedBy(itemId, ownerId);
        item.setIsAvailable(!item.getIsAvailable());
        return inventoryRepository.save(item);
    }

    public void deleteItem(Long itemId, Long ownerId) {
        Inventory item = getItemOwnedBy(itemId, ownerId);
        inventoryRepository.delete(item);
    }

    public List<Inventory> getAvailableItems(Long shopId) {
        return inventoryRepository.findByShopIdAndIsAvailable(shopId, true);
    }

    public List<Inventory> getAllItems(Long shopId) {
        return inventoryRepository.findByShopId(shopId);
    }

    private Shop getShopOwnedBy(Long shopId, Long ownerId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        if (!shop.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("You do not own this shop");
        }
        return shop;
    }

    private Inventory getItemOwnedBy(Long itemId, Long ownerId) {
        Inventory item = inventoryRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        if (!item.getShop().getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("You do not own this item");
        }
        return item;
    }
}
