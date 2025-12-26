package com.fincalc.adapter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${fincalc.admin.username:admin}")
    private String adminUsername;

    @Value("${fincalc.admin.password:#{T(java.util.UUID).randomUUID().toString()}}")
    private String adminPassword;

    @Value("${app.keycloak.issuer:https://keycloak-production-86b1.up.railway.app/realms/mcp}")
    private String keycloakIssuer;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
                .username(adminUsername)
                .password(passwordEncoder().encode(adminPassword))
                .roles("ADMIN")
                .build();

        // Log the generated password if using default
        if (adminPassword.length() == 36) { // UUID length
            System.out.println("========================================");
            System.out.println("ADMIN CREDENTIALS (auto-generated):");
            System.out.println("Username: " + adminUsername);
            System.out.println("Password: " + adminPassword);
            System.out.println("========================================");
        }

        return new InMemoryUserDetailsManager(admin);
    }

    // Security for Admin Dashboard - requires authentication
    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/admin/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/login", "/admin/login/**").permitAll()
                .anyRequest().hasRole("ADMIN")
            )
            .formLogin(form -> form
                .loginPage("/admin/login")
                .defaultSuccessUrl("/admin", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login?logout")
                .permitAll()
            );
        return http.build();
    }

    // JWT Decoder for Keycloak tokens
    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation(keycloakIssuer);
    }

    // Security for MCP endpoints - OAuth2 JWT authentication
    @Bean
    @Order(2)
    public SecurityFilterChain mcpSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/mcp/**")
            .authorizeHttpRequests(auth -> auth
                // All MCP endpoints are public (noauth mode for testing)
                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder()))
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable());
        return http.build();
    }

    // Security for API and other public endpoints
    @Bean
    @Order(3)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**", "/actuator/**", "/.well-known/**")
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }

    // Default security - allow public pages
    @Bean
    @Order(4)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/privacy", "/terms", "/error/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}
