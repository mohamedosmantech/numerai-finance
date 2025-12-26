package com.fincalc.adapter.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * AOP Aspect for logging and performance monitoring.
 * Provides cross-cutting logging without polluting business logic.
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    /**
     * Pointcut for all domain service methods.
     */
    @Pointcut("execution(* com.fincalc.domain.service.*.*(..))")
    public void domainServiceMethods() {}

    /**
     * Pointcut for all controller methods.
     */
    @Pointcut("execution(* com.fincalc.adapter.in.web.*.*(..))")
    public void controllerMethods() {}

    /**
     * Pointcut for all application layer methods.
     */
    @Pointcut("execution(* com.fincalc.application.*.*(..))")
    public void applicationMethods() {}

    /**
     * Log method entry and exit with execution time for domain services.
     */
    @Around("domainServiceMethods()")
    public Object logDomainServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();

        log.debug(">>> Entering: {} with args: {}", methodName, summarizeArgs(joinPoint.getArgs()));

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            log.debug("<<< Exiting: {} ({}ms)", methodName, duration);

            if (duration > 1000) {
                log.warn("Slow execution detected: {} took {}ms", methodName, duration);
            }

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("!!! Exception in: {} after {}ms - {}", methodName, duration, e.getMessage());
            throw e;
        }
    }

    /**
     * Log tool execution in application layer.
     */
    @Around("applicationMethods()")
    public Object logToolExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        if ("executeTool".equals(methodName)) {
            Object[] args = joinPoint.getArgs();
            String toolName = args.length > 0 ? String.valueOf(args[0]) : "unknown";

            log.info("=== MCP Tool Invoked: {} ===", toolName);
            long startTime = System.currentTimeMillis();

            try {
                Object result = joinPoint.proceed();
                long duration = System.currentTimeMillis() - startTime;
                log.info("=== Tool {} completed in {}ms ===", toolName, duration);
                return result;
            } catch (Exception e) {
                log.error("=== Tool {} failed: {} ===", toolName, e.getMessage());
                throw e;
            }
        }

        return joinPoint.proceed();
    }

    /**
     * Log exceptions from controller layer.
     */
    @AfterThrowing(pointcut = "controllerMethods()", throwing = "exception")
    public void logControllerException(JoinPoint joinPoint, Throwable exception) {
        log.error("Controller exception in {}: {}",
                joinPoint.getSignature().toShortString(),
                exception.getMessage());
    }

    /**
     * Summarize method arguments for logging (avoid logging sensitive data).
     */
    private String summarizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        return Arrays.stream(args)
                .map(arg -> arg == null ? "null" : arg.getClass().getSimpleName())
                .toList()
                .toString();
    }
}
