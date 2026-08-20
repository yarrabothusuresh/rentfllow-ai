package com.rentflow.invoice.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class DemoTaxServiceImpl implements TaxService {

    private static final BigDecimal DEFAULT_DEMO_TAX_RATE = new BigDecimal("8.25");

    @Override
    public BigDecimal calculateTax(BigDecimal taxableSubtotal) {
        if (taxableSubtotal == null || taxableSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return taxableSubtotal.multiply(DEFAULT_DEMO_TAX_RATE)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getTaxRate() {
        return DEFAULT_DEMO_TAX_RATE;
    }
}
