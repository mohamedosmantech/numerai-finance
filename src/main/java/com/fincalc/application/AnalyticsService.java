package com.fincalc.application;

import com.fincalc.adapter.out.persistence.entity.AnalyticsDailyEntity;
import com.fincalc.adapter.out.persistence.entity.AnalyticsStatEntity;
import com.fincalc.adapter.out.persistence.repository.AnalyticsDailyRepository;
import com.fincalc.adapter.out.persistence.repository.AnalyticsStatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsStatRepository statRepository;
    private final AnalyticsDailyRepository dailyRepository;

    /**
     * Track a tool invocation (async to not block request)
     */
    @Async
    @Transactional
    public void trackToolCall(String toolName) {
        LocalDate today = LocalDate.now();
        String statId = "tool:" + toolName;

        try {
            statRepository.upsertIncrement(statId, "tool", toolName);
            dailyRepository.upsertIncrement(today, "tool", toolName);
            log.debug("Tracked tool call: {}", toolName);
        } catch (Exception e) {
            log.warn("Failed to track tool call {}: {}", toolName, e.getMessage());
        }
    }

    /**
     * Track when a default value is used
     */
    @Async
    @Transactional
    public void trackDefaultUsed(String paramName) {
        LocalDate today = LocalDate.now();
        String statId = "default:" + paramName;

        try {
            statRepository.upsertIncrement(statId, "default", paramName);
            dailyRepository.upsertIncrement(today, "default", paramName);
            log.debug("Tracked default used: {}", paramName);
        } catch (Exception e) {
            log.warn("Failed to track default {}: {}", paramName, e.getMessage());
        }
    }

    /**
     * Track multiple defaults used at once
     */
    @Async
    @Transactional
    public void trackDefaultsUsed(Map<String, Object> defaultsUsed) {
        if (defaultsUsed == null || defaultsUsed.isEmpty()) return;

        LocalDate today = LocalDate.now();
        for (String paramName : defaultsUsed.keySet()) {
            String statId = "default:" + paramName;
            try {
                statRepository.upsertIncrement(statId, "default", paramName);
                dailyRepository.upsertIncrement(today, "default", paramName);
            } catch (Exception e) {
                log.warn("Failed to track default {}: {}", paramName, e.getMessage());
            }
        }
    }

    /**
     * Track country usage
     */
    @Async
    @Transactional
    public void trackCountry(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) return;

        LocalDate today = LocalDate.now();
        String statId = "country:" + countryCode.toUpperCase();

        try {
            statRepository.upsertIncrement(statId, "country", countryCode.toUpperCase());
            dailyRepository.upsertIncrement(today, "country", countryCode.toUpperCase());
            log.debug("Tracked country: {}", countryCode);
        } catch (Exception e) {
            log.warn("Failed to track country {}: {}", countryCode, e.getMessage());
        }
    }

    /**
     * Track currency usage
     */
    @Async
    @Transactional
    public void trackCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) return;

        LocalDate today = LocalDate.now();
        String statId = "currency:" + currencyCode.toUpperCase();

        try {
            statRepository.upsertIncrement(statId, "currency", currencyCode.toUpperCase());
            dailyRepository.upsertIncrement(today, "currency", currencyCode.toUpperCase());
            log.debug("Tracked currency: {}", currencyCode);
        } catch (Exception e) {
            log.warn("Failed to track currency {}: {}", currencyCode, e.getMessage());
        }
    }

    /**
     * Track rate provider usage
     */
    @Async
    @Transactional
    public void trackProvider(String providerCode) {
        if (providerCode == null || providerCode.isBlank()) return;

        LocalDate today = LocalDate.now();
        String statId = "provider:" + providerCode;

        try {
            statRepository.upsertIncrement(statId, "provider", providerCode);
            dailyRepository.upsertIncrement(today, "provider", providerCode);
            log.debug("Tracked provider: {}", providerCode);
        } catch (Exception e) {
            log.warn("Failed to track provider {}: {}", providerCode, e.getMessage());
        }
    }

    /**
     * Track MCP session
     */
    @Async
    @Transactional
    public void trackMcpSession() {
        LocalDate today = LocalDate.now();
        try {
            statRepository.upsertIncrement("mcp:sessions", "mcp", "sessions");
            dailyRepository.upsertIncrement(today, "mcp", "sessions");
        } catch (Exception e) {
            log.warn("Failed to track MCP session: {}", e.getMessage());
        }
    }

    /**
     * Track MCP request
     */
    @Async
    @Transactional
    public void trackMcpRequest() {
        LocalDate today = LocalDate.now();
        try {
            statRepository.upsertIncrement("mcp:total_requests", "mcp", "total_requests");
            dailyRepository.upsertIncrement(today, "mcp", "total_requests");
        } catch (Exception e) {
            log.warn("Failed to track MCP request: {}", e.getMessage());
        }
    }

    /**
     * Track page view
     */
    @Async
    @Transactional
    public void trackPageView(String pageName) {
        LocalDate today = LocalDate.now();
        String statId = "page:" + pageName;

        try {
            statRepository.upsertIncrement(statId, "page", pageName);
            dailyRepository.upsertIncrement(today, "page", pageName);
        } catch (Exception e) {
            log.warn("Failed to track page view {}: {}", pageName, e.getMessage());
        }
    }

    // ============= QUERY METHODS =============

    /**
     * Get all-time stats by category
     */
    public List<AnalyticsStatEntity> getStatsByCategory(String category) {
        return statRepository.findByCategoryOrderByCountDesc(category);
    }

    /**
     * Get total count for a stat
     */
    public long getTotalCount(String statId) {
        return statRepository.findById(statId)
                .map(AnalyticsStatEntity::getCount)
                .orElse(0L);
    }

    /**
     * Get stats for today
     */
    public Map<String, Long> getTodayStats(String category) {
        LocalDate today = LocalDate.now();
        List<AnalyticsDailyEntity> stats = dailyRepository.findByStatDateBetweenAndCategory(today, today, category);

        Map<String, Long> result = new LinkedHashMap<>();
        for (AnalyticsDailyEntity stat : stats) {
            result.put(stat.getName(), stat.getCount());
        }
        return result;
    }

    /**
     * Get stats for last N days
     */
    public Map<String, Long> getStatsForLastDays(String category, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<Object[]> results = dailyRepository.findTopByCategoryInDateRange(startDate, endDate, category);

        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : results) {
            result.put((String) row[0], ((Number) row[1]).longValue());
        }
        return result;
    }

    /**
     * Get total count for category in last N days
     */
    public long getTotalForLastDays(String category, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        Long sum = dailyRepository.sumCountByDateRangeAndCategory(startDate, endDate, category);
        return sum != null ? sum : 0L;
    }

    /**
     * Get analytics summary for dashboard
     */
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();

        // All-time totals
        summary.put("totalMcpSessions", getTotalCount("mcp:sessions"));
        summary.put("totalMcpRequests", getTotalCount("mcp:total_requests"));

        // Today's stats
        summary.put("todaySessions", getTotalForLastDays("mcp", 1));
        summary.put("todayToolCalls", getTotalForLastDays("tool", 1));

        // Last 7 days
        summary.put("weekSessions", getTotalForLastDays("mcp", 7));
        summary.put("weekToolCalls", getTotalForLastDays("tool", 7));

        // Last 30 days
        summary.put("monthSessions", getTotalForLastDays("mcp", 30));
        summary.put("monthToolCalls", getTotalForLastDays("tool", 30));

        // Top tools (all time)
        summary.put("topTools", getStatsByCategory("tool"));

        // Top countries (all time)
        summary.put("topCountries", getStatsByCategory("country"));

        // Top currencies (all time)
        summary.put("topCurrencies", getStatsByCategory("currency"));

        // Defaults used
        summary.put("defaultsUsed", getStatsByCategory("default"));

        // Last 7 days breakdown
        summary.put("toolsLast7Days", getStatsForLastDays("tool", 7));
        summary.put("countriesLast7Days", getStatsForLastDays("country", 7));

        return summary;
    }

    /**
     * Get daily trend data for charts
     */
    public List<Map<String, Object>> getDailyTrend(String category, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<AnalyticsDailyEntity> stats = dailyRepository.findByStatDateBetweenAndCategory(startDate, endDate, category);

        // Group by date
        Map<LocalDate, Long> byDate = new TreeMap<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            byDate.put(date, 0L);
        }
        for (AnalyticsDailyEntity stat : stats) {
            byDate.merge(stat.getStatDate(), stat.getCount(), Long::sum);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<LocalDate, Long> entry : byDate.entrySet()) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", entry.getKey().toString());
            point.put("count", entry.getValue());
            result.add(point);
        }
        return result;
    }
}
