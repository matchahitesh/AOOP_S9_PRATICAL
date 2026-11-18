package com.klu.casestudy2;

import java.util.Objects;

public record OrderId(String value) {
    public OrderId {
        Objects.requireNonNull(value, "OrderId cannot be null");
        if (value.isBlank()) throw new IllegalArgumentException("OrderId cannot be blank");
    }
}
