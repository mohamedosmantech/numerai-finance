package com.fincalc.domain.port.in;

import com.fincalc.domain.model.CompoundInterestCalculation;
import com.fincalc.domain.validation.constraint.ValidInterestRate;
import com.fincalc.domain.validation.constraint.ValidLoanTerm;
import com.fincalc.domain.validation.constraint.ValidMoney;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Input port for compound interest calculation use case.
 */
public interface CalculateCompoundInterestUseCase {

    CompoundInterestCalculation execute(Command command);

    record Command(
            @NotNull(message = "{validation.investment.principal.required}")
            @ValidMoney(message = "{validation.investment.principal.positive}")
            BigDecimal principal,

            @NotNull(message = "{validation.investment.rate.required}")
            @ValidInterestRate(min = 0.01, max = 100, message = "{validation.investment.rate.range}")
            BigDecimal annualRate,

            @ValidLoanTerm(min = 1, max = 100, message = "{validation.investment.years.range}")
            int years,

            @Min(value = 1, message = "{validation.investment.frequency.range}")
            @Max(value = 365, message = "{validation.investment.frequency.range}")
            int compoundingFrequency,

            @ValidMoney(allowZero = true, message = "{validation.investment.contribution.non-negative}")
            BigDecimal monthlyContribution
    ) {
        public Command {
            // Set defaults for optional fields
            if (compoundingFrequency <= 0) compoundingFrequency = 12;
            if (monthlyContribution == null) monthlyContribution = BigDecimal.ZERO;
        }
    }
}
