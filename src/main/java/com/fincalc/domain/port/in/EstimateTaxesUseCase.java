package com.fincalc.domain.port.in;

import com.fincalc.domain.model.TaxEstimation;
import com.fincalc.domain.validation.constraint.ValidFilingStatus;
import com.fincalc.domain.validation.constraint.ValidMoney;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Input port for tax estimation use case.
 */
public interface EstimateTaxesUseCase {

    TaxEstimation execute(Command command);

    record Command(
            @NotNull(message = "{validation.tax.income.required}")
            @ValidMoney(allowZero = true, message = "{validation.tax.income.non-negative}")
            BigDecimal grossIncome,

            @NotBlank(message = "{validation.filing-status.required}")
            @ValidFilingStatus
            String filingStatus,

            @ValidMoney(allowZero = true, message = "{validation.tax.deductions.non-negative}")
            BigDecimal deductions,

            @Size(min = 2, max = 2, message = "{validation.state.length}")
            @Pattern(regexp = "[A-Z]{2}", message = "{validation.state.format}")
            String state
    ) {}
}
