package com.societyshops.security;

import com.societyshops.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class CustomAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String email = request.getParameter("username");
        String errorMsg;

        if (!userRepository.existsByEmail(email)) {
            errorMsg = "No account found with this email. Please register first.";
        } else {
            errorMsg = "Incorrect password. Please try again.";
        }

        String encoded = URLEncoder.encode(errorMsg, StandardCharsets.UTF_8);
        response.sendRedirect("/web/login?error=" + encoded);
    }
}
