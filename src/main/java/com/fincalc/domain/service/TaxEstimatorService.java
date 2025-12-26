package com.fincalc.domain.service;

import com.fincalc.domain.model.TaxEstimation;
import com.fincalc.domain.port.in.EstimateTaxesUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Domain service for tax estimation calculations.
 * Single Responsibility: Only handles tax-related calculations.
 * Validation is handled by Bean Validation annotations on Command record.
 */
@Slf4j
@Service
public class TaxEstimatorService implements EstimateTaxesUseCase {

    @Override
    public TaxEstimation execute(Command command) {
        log.debug("Estimating taxes: income={}, status={}, state={}",
                command.grossIncome(), command.filingStatus(), command.state());

        TaxEstimation.FilingStatus status = TaxEstimation.FilingStatus.fromString(command.filingStatus());
        return TaxEstimation.calculate(command.grossIncome(), status, command.deductions(), command.state());
    }
}
