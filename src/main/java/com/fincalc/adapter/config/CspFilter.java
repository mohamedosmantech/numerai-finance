package com.fincalc.adapter.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Content Security Policy filter for OpenAI ChatGPT Apps compliance.
 * Required for app submission to the ChatGPT Apps Directory.
 */
@Component
@Order(1)
public class CspFilter extends OncePerRequestFilter {

    @Value("${fincalc.csp.frame-ancestors:https://chatgpt.com https://*.openai.com}")
    private String frameAncestors;

    @Value("${fincalc.csp.connect-src:https://chatgpt.com https://*.openai.com}")
    private String connectSrc;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Content Security Policy for OpenAI embedding
        String csp = String.format(
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data: https:; " +
                "font-src 'self'; " +
                "connect-src 'self' %s; " +
                "frame-ancestors %s; " +
                "base-uri 'self'; " +
                "form-action 'self'",
                connectSrc, frameAncestors
        );

        response.setHeader("Content-Security-Policy", csp);
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "ALLOW-FROM https://chatgpt.com");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        filterChain.doFilter(request, response);
    }
}
