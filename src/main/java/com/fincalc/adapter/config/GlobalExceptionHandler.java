package com.fincalc.adapter.config;

import com.fincalc.adapter.in.web.dto.JsonRpcResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

/**
 * Global exception handler for API endpoints only.
 * Excludes admin dashboard which uses Thymeleaf templates.
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.fincalc.adapter.in.web",
                      basePackageClasses = {})
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResource(NoResourceFoundException e) {
        // Let 404s pass through for proper handling
        log.debug("Resource not found: {}", e.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "Not found",
                        "message", "The requested resource was not found."
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<JsonRpcResponse> handleParseError(HttpMessageNotReadableException e) {
        log.warn("JSON parse error: {}", e.getMessage());
        return ResponseEntity.badRequest().body(JsonRpcResponse.parseError());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<JsonRpcResponse> handleValidationError(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        log.warn("Validation error: {}", message);
        return ResponseEntity.badRequest().body(JsonRpcResponse.invalidRequest(null));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<JsonRpcResponse> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("Missing parameter: {}", e.getParameterName());
        return ResponseEntity.badRequest()
                .body(JsonRpcResponse.invalidParams(null, "Missing required parameter: " + e.getParameterName()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<JsonRpcResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Invalid argument: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(JsonRpcResponse.invalidParams(null, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpectedError(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "Internal server error",
                        "message", "An unexpected error occurred. Please try again later."
                ));
    }
}
