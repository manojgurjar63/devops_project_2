package com.societyshops.config;

import com.societyshops.security.CustomAuthFailureHandler;
import com.societyshops.security.CustomUserDetailsService;
import com.societyshops.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthFailureHandler authFailureHandler;

    // ── Chain 1: REST API (JWT, stateless) ────────────────────────────────────
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ── Chain 2: Web dashboard (session-based form login) ─────────────────────
    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s
                .invalidSessionUrl("/web/login?session=expired")
                .sessionConcurrency(c -> c
                    .maximumSessions(1)
                    .expiredUrl("/web/login?session=expired")
                )
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/web/login", "/web/logout", "/web/register", "/web/forgot-password", "/web/reset-password", "/web", "/web/", "/").permitAll()
                .requestMatchers("/shops", "/shops/**", "/").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**", "/webjars/**").permitAll()
                .requestMatchers("/web/admin/**").hasRole("ADMIN")
                .requestMatchers("/web/shopkeeper/**").hasRole("SHOPKEEPER")
                .requestMatchers("/web/resident/**").hasRole("RESIDENT")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/web/login")
                .loginProcessingUrl("/web/login")
                .successHandler((req, res, auth) -> {
                    String redirect = req.getParameter("redirect");
                    if (redirect != null && !redirect.isBlank() && redirect.startsWith("/")) {
                        res.sendRedirect(redirect);
                    } else {
                        // role-based default
                        var authorities = auth.getAuthorities().stream().map(a -> a.getAuthority()).toList();
                        if (authorities.contains("ROLE_ADMIN")) res.sendRedirect("/web/admin/dashboard");
                        else if (authorities.contains("ROLE_SHOPKEEPER")) res.sendRedirect("/web/shopkeeper/dashboard");
                        else res.sendRedirect("/web/resident/dashboard");
                    }
                })
                .failureHandler(authFailureHandler)
                .permitAll()
            )
            .rememberMe(rm -> rm
                .key("societyshops-remember-me-key")
                .tokenValiditySeconds(30 * 24 * 60 * 60) // 30 days
                .userDetailsService(userDetailsService)
            )
            .logout(logout -> logout
                .logoutUrl("/web/logout")
                .logoutSuccessUrl("/web/login?logout=true")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((req, res, e) -> {
                    if ("application/json".equals(req.getContentType()) || "XMLHttpRequest".equals(req.getHeader("X-Requested-With"))) {
                        res.setStatus(403);
                        res.setContentType("application/json");
                        res.getWriter().write("{\"error\":\"forbidden\"}");
                    } else {
                        res.sendRedirect("/web/login?error=Access+denied");
                    }
                })
                .authenticationEntryPoint((req, res, e) -> {
                    if ("application/json".equals(req.getContentType()) || "XMLHttpRequest".equals(req.getHeader("X-Requested-With"))) {
                        res.setStatus(401);
                        res.setContentType("application/json");
                        res.getWriter().write("{\"error\":\"unauthorized\"}");
                    } else {
                        res.sendRedirect("/web/login");
                    }
                })
            )
            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
