package com.societyshops.service;

import com.societyshops.entity.PasswordResetToken;
import com.societyshops.entity.User;
import com.societyshops.repository.PasswordResetTokenRepository;
import com.societyshops.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            tokenRepository.deleteByUserId(user.getId());
            String token = UUID.randomUUID().toString();
            tokenRepository.save(PasswordResetToken.builder()
                    .token(token).user(user)
                    .expiresAt(LocalDateTime.now().plusMinutes(30))
                    .used(false).build());
            emailService.sendPasswordReset(user.getName(), email, token);
        });
        // Always return silently — don't reveal if email exists
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken prt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired link"));
        if (prt.getUsed() || prt.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new RuntimeException("This link has expired. Please request a new one.");
        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        prt.setUsed(true);
        tokenRepository.save(prt);
    }
}
