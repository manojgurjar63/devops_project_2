package com.societyshops.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.societyshops.dto.AuthRequest;
import com.societyshops.dto.AuthResponse;
import com.societyshops.dto.RegisterRequest;
import com.societyshops.enums.Role;
import com.societyshops.exception.GlobalExceptionHandler;
import com.societyshops.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();

    @Mock AuthService authService;
    @InjectMocks AuthController authController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void register_returns200() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setName("Alice");
        req.setEmail("alice@example.com");
        req.setPassword("pass123");
        req.setRole(Role.SHOPKEEPER);

        when(authService.register(any())).thenReturn(new AuthResponse("token", "SHOPKEEPER", "Alice"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("token"))
                .andExpect(jsonPath("$.data.role").value("SHOPKEEPER"));
    }

    @Test
    void login_returns200() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setEmail("alice@example.com");
        req.setPassword("pass123");

        when(authService.login(any())).thenReturn(new AuthResponse("token", "SHOPKEEPER", "Alice"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("token"));
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setName("Alice");
        req.setEmail("not-an-email");
        req.setPassword("pass123");
        req.setRole(Role.SHOPKEEPER);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
