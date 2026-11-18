package com.klu.casestudy2;

import java.util.Objects;

public record TrackingNumber(String code) {
    public TrackingNumber {
        Objects.requireNonNull(code, "Tracking number cannot be null");
        if (code.isBlank()) throw new IllegalArgumentException("Tracking number cannot be blank");
    }
}
