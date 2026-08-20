package com.rentflow.invoice.service;

import java.math.BigDecimal;

public interface TaxService {
    /**
     * Calculates tax for a given taxable subtotal amount.
     */
    BigDecimal calculateTax(BigDecimal taxableSubtotal);

    /**
     * Returns the current active tax rate percentage (e.g. 8.25 for 8.25%).
     */
    BigDecimal getTaxRate();
}
