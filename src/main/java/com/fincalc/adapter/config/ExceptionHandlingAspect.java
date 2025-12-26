package com.fincalc.adapter.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * AOP Aspect for centralized exception handling and transformation.
 * Converts technical exceptions to user-friendly messages.
 */
@Slf4j
@Aspect
@Component
@Order(1)
public class ExceptionHandlingAspect {

    /**
     * Wrap domain service exceptions with meaningful messages.
     */
    @Around("execution(* com.fincalc.domain.service.*.*(..))")
    public Object handleDomainExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (IllegalArgumentException e) {
            // Re-throw validation errors as-is (they have user-friendly messages)
            throw e;
        } catch (ArithmeticException e) {
            log.error("Arithmetic error in {}: {}", joinPoint.getSignature().getName(), e.getMessage());
            throw new IllegalArgumentException("Calculation error: Please check your input values are within valid ranges");
        } catch (NullPointerException e) {
            log.error("Null pointer in {}: {}", joinPoint.getSignature().getName(), e.getMessage());
            throw new IllegalArgumentException("Missing required input value");
        } catch (Exception e) {
            log.error("Unexpected error in {}: {}", joinPoint.getSignature().getName(), e.getMessage(), e);
            throw new RuntimeException("An unexpected calculation error occurred. Please try again.");
        }
    }

    /**
     * Wrap tool handler exceptions.
     */
    @Around("execution(* com.fincalc.application.McpToolHandler.executeTool(..))")
    public Object handleToolExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String toolName = args.length > 0 ? String.valueOf(args[0]) : "unknown";

        try {
            return joinPoint.proceed();
        } catch (IllegalArgumentException e) {
            // Enrich error message with tool context
            throw new IllegalArgumentException(
                    String.format("Error in %s: %s", formatToolName(toolName), e.getMessage())
            );
        } catch (Exception e) {
            log.error("Tool execution failed: {} - {}", toolName, e.getMessage(), e);
            throw new RuntimeException(
                    String.format("Failed to execute %s. Please verify your inputs and try again.",
                            formatToolName(toolName))
            );
        }
    }

    /**
     * Format tool name for display (snake_case to Title Case).
     */
    private String formatToolName(String toolName) {
        if (toolName == null) return "Unknown Tool";
        return switch (toolName) {
            case "calculate_loan_payment" -> "Loan Calculator";
            case "calculate_compound_interest" -> "Investment Calculator";
            case "estimate_taxes" -> "Tax Estimator";
            default -> toolName.replace("_", " ");
        };
    }
}
