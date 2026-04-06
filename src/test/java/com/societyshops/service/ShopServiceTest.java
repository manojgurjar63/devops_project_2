package com.societyshops.service;

import com.societyshops.dto.ShopRequest;
import com.societyshops.entity.Shop;
import com.societyshops.entity.User;
import com.societyshops.enums.ShopStatus;
import com.societyshops.repository.ShopRepository;
import com.societyshops.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock ShopRepository shopRepository;
    @Mock UserRepository userRepository;
    @InjectMocks ShopService shopService;

    private User mockUser(Long id) {
        return User.builder().id(id).name("Owner").email("owner@example.com").build();
    }

    private Shop mockShop(Long shopId, User owner, ShopStatus status) {
        return Shop.builder().id(shopId).owner(owner).name("My Shop")
                .status(status).isApproved(false).build();
    }

    @Test
    void registerShop_success() {
        User owner = mockUser(1L);
        ShopRequest req = new ShopRequest();
        req.setName("My Shop");

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(shopRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Shop shop = shopService.registerShop(req, 1L);

        assertThat(shop.getName()).isEqualTo("My Shop");
        assertThat(shop.getIsApproved()).isFalse();
        assertThat(shop.getStatus()).isEqualTo(ShopStatus.CLOSED);
    }

    @Test
    void toggleStatus_openToClosed() {
        User owner = mockUser(1L);
        Shop shop = mockShop(10L, owner, ShopStatus.OPEN);

        when(shopRepository.findById(10L)).thenReturn(Optional.of(shop));
        when(shopRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Shop result = shopService.toggleStatus(10L, 1L);

        assertThat(result.getStatus()).isEqualTo(ShopStatus.CLOSED);
    }

    @Test
    void toggleStatus_wrongOwner_throws() {
        User owner = mockUser(1L);
        Shop shop = mockShop(10L, owner, ShopStatus.OPEN);

        when(shopRepository.findById(10L)).thenReturn(Optional.of(shop));

        assertThatThrownBy(() -> shopService.toggleStatus(10L, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("You do not own this shop");
    }

    @Test
    void approveShop_setsApprovedTrue() {
        User owner = mockUser(1L);
        Shop shop = mockShop(10L, owner, ShopStatus.CLOSED);

        when(shopRepository.findById(10L)).thenReturn(Optional.of(shop));
        when(shopRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Shop result = shopService.approveShop(10L);

        assertThat(result.getIsApproved()).isTrue();
    }

    @Test
    void rejectShop_deletesShop() {
        User owner = mockUser(1L);
        Shop shop = mockShop(10L, owner, ShopStatus.CLOSED);

        when(shopRepository.findById(10L)).thenReturn(Optional.of(shop));

        shopService.rejectShop(10L);

        verify(shopRepository).delete(shop);
    }
}
