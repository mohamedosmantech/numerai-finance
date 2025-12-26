package com.fincalc.adapter.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for exposing tool usage metrics.
 */
@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final MetricsAspect metricsAspect;

    public MetricsController(MetricsAspect metricsAspect) {
        this.metricsAspect = metricsAspect;
    }

    @GetMapping("/tools")
    public ResponseEntity<Map<String, Object>> getToolMetrics() {
        Map<String, Object> response = new HashMap<>();
        Map<String, MetricsAspect.ToolMetrics> allMetrics = metricsAspect.getMetrics();

        long totalCalls = 0;
        long totalSuccess = 0;

        Map<String, Map<String, Object>> toolDetails = new HashMap<>();
        for (Map.Entry<String, MetricsAspect.ToolMetrics> entry : allMetrics.entrySet()) {
            MetricsAspect.ToolMetrics m = entry.getValue();
            totalCalls += m.getTotalCalls();
            totalSuccess += m.getSuccessCalls();

            Map<String, Object> details = new HashMap<>();
            details.put("totalCalls", m.getTotalCalls());
            details.put("successCalls", m.getSuccessCalls());
            details.put("failedCalls", m.getFailedCalls());
            details.put("successRate", String.format("%.2f%%", m.getSuccessRate()));
            details.put("avgDurationMs", String.format("%.2f", m.getAverageDuration()));
            details.put("minDurationMs", m.getMinDuration());
            details.put("maxDurationMs", m.getMaxDuration());
            toolDetails.put(entry.getKey(), details);
        }

        response.put("summary", Map.of(
                "totalToolCalls", totalCalls,
                "totalSuccessful", totalSuccess,
                "overallSuccessRate", totalCalls > 0 ? String.format("%.2f%%", (double) totalSuccess / totalCalls * 100) : "N/A"
        ));
        response.put("tools", toolDetails);

        return ResponseEntity.ok(response);
    }
}
