package com.fincalc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * FinCalc Pro - Financial Calculator MCP Server for ChatGPT
 *
 * A production-ready MCP server implementing financial calculation tools:
 * - Loan Payment Calculator
 * - Compound Interest Calculator
 * - Tax Estimator (US 2024)
 *
 * Built with hexagonal architecture for clean separation of concerns.
 */
@SpringBootApplication
@EnableAsync
public class FinCalcProApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinCalcProApplication.class, args);
    }
}
