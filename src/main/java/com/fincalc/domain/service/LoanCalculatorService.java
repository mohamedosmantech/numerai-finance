package com.fincalc.domain.service;

import com.fincalc.domain.model.LoanCalculation;
import com.fincalc.domain.port.in.CalculateLoanPaymentUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Domain service for loan payment calculations.
 * Single Responsibility: Only handles loan-related calculations.
 * Validation is handled by Bean Validation annotations on Command record.
 */
@Slf4j
@Service
public class LoanCalculatorService implements CalculateLoanPaymentUseCase {

    @Override
    public LoanCalculation execute(Command command) {
        log.debug("Calculating loan payment: principal={}, rate={}%, years={}",
                command.principal(), command.annualRate(), command.years());

        return LoanCalculation.calculate(command.principal(), command.annualRate(), command.years());
    }
}
