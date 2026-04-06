package com.societyshops.service;

import com.societyshops.dto.AuthRequest;
import com.societyshops.dto.AuthResponse;
import com.societyshops.dto.RegisterRequest;
import com.societyshops.entity.User;
import com.societyshops.enums.Role;
import com.societyshops.repository.UserRepository;
import com.societyshops.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @InjectMocks AuthService authService;

    @Test
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Alice");
        req.setEmail("alice@example.com");
        req.setPassword("pass123");
        req.setRole(Role.SHOPKEEPER);

        when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(req.getPassword())).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("token123");

        AuthResponse res = authService.register(req);

        assertThat(res.getToken()).isEqualTo("token123");
        assertThat(res.getRole()).isEqualTo("SHOPKEEPER");
        assertThat(res.getName()).isEqualTo("Alice");
    }

    @Test
    void register_duplicateEmail_throws() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("alice@example.com");
        when(userRepository.existsByEmail(req.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already registered");
    }

    @Test
    void login_success() {
        AuthRequest req = new AuthRequest();
        req.setEmail("alice@example.com");
        req.setPassword("pass123");

        User user = User.builder().name("Alice").email(req.getEmail())
                .password("encoded").role(Role.SHOPKEEPER).build();

        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.getPassword(), "encoded")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("token123");

        AuthResponse res = authService.login(req);

        assertThat(res.getToken()).isEqualTo("token123");
    }

    @Test
    void login_wrongPassword_throws() {
        AuthRequest req = new AuthRequest();
        req.setEmail("alice@example.com");
        req.setPassword("wrong");

        User user = User.builder().email(req.getEmail()).password("encoded").role(Role.SHOPKEEPER).build();

        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.getPassword(), "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void login_userNotFound_throws() {
        AuthRequest req = new AuthRequest();
        req.setEmail("nobody@example.com");
        req.setPassword("pass");

        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid email or password");
    }
}
