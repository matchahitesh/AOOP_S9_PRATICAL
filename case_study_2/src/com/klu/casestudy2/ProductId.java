package com.klu.casestudy2;

import java.util.Objects;

public record ProductId(String value) {
    public ProductId {
        Objects.requireNonNull(value, "ProductId cannot be null");
        if (value.isBlank()) throw new IllegalArgumentException("ProductId cannot be blank");
    }
}
