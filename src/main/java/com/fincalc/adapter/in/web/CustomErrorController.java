package com.fincalc.adapter.in.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Custom error controller that renders error pages for web requests
 * while allowing API endpoints to return JSON errors.
 */
@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Get error details
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object error = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        int statusCode = 500;
        String errorMessage = "Internal Server Error";

        if (status != null) {
            statusCode = Integer.parseInt(status.toString());
            HttpStatus httpStatus = HttpStatus.resolve(statusCode);
            if (httpStatus != null) {
                errorMessage = httpStatus.getReasonPhrase();
            }
        }

        // Check if this is an API request - if so, don't render HTML
        String acceptHeader = request.getHeader("Accept");
        String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        if (isApiRequest(acceptHeader, requestUri)) {
            // Let Spring's default JSON error handling take over
            return null;
        }

        model.addAttribute("status", statusCode);
        model.addAttribute("error", errorMessage);
        model.addAttribute("message", error != null ? error.toString() : null);
        model.addAttribute("path", requestUri);

        return "error";
    }

    private boolean isApiRequest(String acceptHeader, String requestUri) {
        // Check if it's an API path
        if (requestUri != null &&
                (requestUri.startsWith("/api/") || requestUri.startsWith("/mcp/") ||
                        requestUri.startsWith("/actuator/"))) {
            return true;
        }
        // Check Accept header for JSON
        return acceptHeader != null && acceptHeader.contains("application/json");
    }
}
