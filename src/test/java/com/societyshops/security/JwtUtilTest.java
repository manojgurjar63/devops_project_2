package com.societyshops.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    JwtUtil jwtUtil = new JwtUtil();

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(jwtUtil, "secret", "societyshops_secret_key_must_be_at_least_32_characters_long");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    void generateToken_and_extractEmail() {
        String token = jwtUtil.generateToken("test@example.com", "RESIDENT");
        assertEquals("test@example.com", jwtUtil.extractEmail(token));
    }

    @Test
    void generateToken_and_extractRole() {
        String token = jwtUtil.generateToken("test@example.com", "ADMIN");
        assertEquals("ADMIN", jwtUtil.extractRole(token));
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtUtil.generateToken("test@example.com", "SHOPKEEPER");
        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void isTokenValid_invalidToken_returnsFalse() {
        assertFalse(jwtUtil.isTokenValid("invalid.token.here"));
    }
}
