package com.fincalc.domain.service;

import com.fincalc.domain.model.CompoundInterestCalculation;
import com.fincalc.domain.port.in.CalculateCompoundInterestUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Domain service for compound interest calculations.
 * Single Responsibility: Only handles investment growth calculations.
 * Validation is handled by Bean Validation annotations on Command record.
 */
@Slf4j
@Service
public class CompoundInterestService implements CalculateCompoundInterestUseCase {

    @Override
    public CompoundInterestCalculation execute(Command command) {
        log.debug("Calculating compound interest: principal={}, rate={}%, years={}, frequency={}",
                command.principal(), command.annualRate(), command.years(), command.compoundingFrequency());

        return CompoundInterestCalculation.calculate(
                command.principal(),
                command.annualRate(),
                command.years(),
                command.compoundingFrequency(),
                command.monthlyContribution()
        );
    }
}
