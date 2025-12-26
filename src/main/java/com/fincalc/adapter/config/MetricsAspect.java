package com.fincalc.adapter.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AOP Aspect for collecting metrics about tool usage.
 * Provides insights into which tools are used most frequently.
 */
@Aspect
@Component
public class MetricsAspect {

    private final Map<String, ToolMetrics> toolMetrics = new ConcurrentHashMap<>();

    /**
     * Collect metrics for each tool execution.
     */
    @Around("execution(* com.fincalc.application.McpToolHandler.executeTool(String, ..))")
    public Object collectToolMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String toolName = args.length > 0 ? String.valueOf(args[0]) : "unknown";

        ToolMetrics metrics = toolMetrics.computeIfAbsent(toolName, k -> new ToolMetrics());
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            metrics.recordSuccess(System.currentTimeMillis() - startTime);
            return result;
        } catch (Exception e) {
            metrics.recordFailure(System.currentTimeMillis() - startTime);
            throw e;
        }
    }

    /**
     * Get metrics for all tools.
     */
    public Map<String, ToolMetrics> getMetrics() {
        return Map.copyOf(toolMetrics);
    }

    /**
     * Get metrics for a specific tool.
     */
    public ToolMetrics getMetrics(String toolName) {
        return toolMetrics.get(toolName);
    }

    /**
     * Metrics container for a single tool.
     */
    public static class ToolMetrics {
        private final AtomicLong totalCalls = new AtomicLong(0);
        private final AtomicLong successCalls = new AtomicLong(0);
        private final AtomicLong failedCalls = new AtomicLong(0);
        private final AtomicLong totalDuration = new AtomicLong(0);
        private volatile long minDuration = Long.MAX_VALUE;
        private volatile long maxDuration = 0;

        void recordSuccess(long duration) {
            totalCalls.incrementAndGet();
            successCalls.incrementAndGet();
            recordDuration(duration);
        }

        void recordFailure(long duration) {
            totalCalls.incrementAndGet();
            failedCalls.incrementAndGet();
            recordDuration(duration);
        }

        private synchronized void recordDuration(long duration) {
            totalDuration.addAndGet(duration);
            if (duration < minDuration) minDuration = duration;
            if (duration > maxDuration) maxDuration = duration;
        }

        public long getTotalCalls() { return totalCalls.get(); }
        public long getSuccessCalls() { return successCalls.get(); }
        public long getFailedCalls() { return failedCalls.get(); }
        public double getSuccessRate() {
            long total = totalCalls.get();
            return total > 0 ? (double) successCalls.get() / total * 100 : 0;
        }
        public double getAverageDuration() {
            long total = totalCalls.get();
            return total > 0 ? (double) totalDuration.get() / total : 0;
        }
        public long getMinDuration() { return minDuration == Long.MAX_VALUE ? 0 : minDuration; }
        public long getMaxDuration() { return maxDuration; }
    }
}
