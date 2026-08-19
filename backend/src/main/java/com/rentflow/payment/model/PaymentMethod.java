package com.rentflow.payment.model;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum PaymentMethod {
    CASH,
    BANK_TRANSFER,
    CREDIT_CARD,
    DEBIT_CARD,
    CHECK,
    OTHER
}
