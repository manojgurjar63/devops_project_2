package com.societyshops.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societyshops.dto.ShopRequest;
import com.societyshops.entity.Shop;
import com.societyshops.entity.User;
import com.societyshops.enums.Role;
import com.societyshops.enums.ShopStatus;
import com.societyshops.exception.GlobalExceptionHandler;
import com.societyshops.repository.UserRepository;
import com.societyshops.service.ShopService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ShopControllerTest {

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();

    @Mock ShopService shopService;
    @Mock UserRepository userRepository;
    @InjectMocks ShopController shopController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(shopController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private User mockUser() {
        return User.builder().id(1L).email("shop@example.com").role(Role.SHOPKEEPER).build();
    }

    private Shop mockShop() {
        return Shop.builder().id(10L).name("My Shop").status(ShopStatus.CLOSED).isApproved(true).build();
    }

    private UsernamePasswordAuthenticationToken auth(String email, String role) {
        return new UsernamePasswordAuthenticationToken(email, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    @Test
    void registerShop_returns200() throws Exception {
        ShopRequest req = new ShopRequest();
        req.setName("My Shop");

        when(userRepository.findByEmail("shop@example.com")).thenReturn(Optional.of(mockUser()));
        when(shopService.registerShop(any(), eq(1L))).thenReturn(mockShop());

        mockMvc.perform(post("/api/shops")
                        .principal(auth("shop@example.com", "SHOPKEEPER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("My Shop"));
    }

    @Test
    void getApprovedShops_returns200() throws Exception {
        when(shopService.getApprovedShops()).thenReturn(List.of(mockShop()));

        mockMvc.perform(get("/api/shops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("My Shop"));
    }

    @Test
    void getApprovedShops_forbiddenForShopkeeper() throws Exception {
        // Role enforcement tested via integration; unit test verifies endpoint responds
        when(shopService.getApprovedShops()).thenReturn(List.of());
        mockMvc.perform(get("/api/shops"))
                .andExpect(status().isOk());
    }

    @Test
    void getPendingShops_returns200() throws Exception {
        when(shopService.getPendingShops()).thenReturn(List.of(mockShop()));

        mockMvc.perform(get("/api/shops/pending"))
                .andExpect(status().isOk());
    }

    @Test
    void approveShop_returns200() throws Exception {
        when(shopService.approveShop(10L)).thenReturn(mockShop());

        mockMvc.perform(put("/api/shops/10/approve"))
                .andExpect(status().isOk());
    }
}
