package com.fincalc.domain.port.in;

import com.fincalc.domain.model.LoanCalculation;
import com.fincalc.domain.validation.constraint.ValidInterestRate;
import com.fincalc.domain.validation.constraint.ValidLoanTerm;
import com.fincalc.domain.validation.constraint.ValidMoney;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Input port for loan payment calculation use case.
 */
public interface CalculateLoanPaymentUseCase {

    LoanCalculation execute(Command command);

    record Command(
            @NotNull(message = "{validation.loan.principal.required}")
            @ValidMoney(message = "{validation.loan.principal.positive}")
            BigDecimal principal,

            @NotNull(message = "{validation.loan.rate.required}")
            @ValidInterestRate(min = 0.01, max = 50, message = "{validation.loan.rate.range}")
            BigDecimal annualRate,

            @ValidLoanTerm(min = 1, max = 50, message = "{validation.loan.years.range}")
            int years
    ) {}
}
