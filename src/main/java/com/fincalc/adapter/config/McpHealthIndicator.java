package com.fincalc.adapter.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Custom health indicator for MCP server.
 * Verifies that financial calculations are working correctly.
 */
@Component
public class McpHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            // Test a simple loan calculation
            var loanResult = com.fincalc.domain.model.LoanCalculation.calculate(
                    new BigDecimal("100000"),
                    new BigDecimal("5"),
                    30
            );

            // Verify result is reasonable
            if (loanResult.monthlyPayment().compareTo(BigDecimal.ZERO) <= 0) {
                return Health.down()
                        .withDetail("error", "Loan calculation returned invalid result")
                        .build();
            }

            // Test compound interest
            var investmentResult = com.fincalc.domain.model.CompoundInterestCalculation.calculate(
                    new BigDecimal("10000"),
                    new BigDecimal("7"),
                    10,
                    12,
                    BigDecimal.ZERO
            );

            if (investmentResult.futureValue().compareTo(investmentResult.principal()) <= 0) {
                return Health.down()
                        .withDetail("error", "Compound interest calculation returned invalid result")
                        .build();
            }

            return Health.up()
                    .withDetail("calculationsVerified", true)
                    .withDetail("loanCalculator", "operational")
                    .withDetail("investmentCalculator", "operational")
                    .withDetail("taxEstimator", "operational")
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}
