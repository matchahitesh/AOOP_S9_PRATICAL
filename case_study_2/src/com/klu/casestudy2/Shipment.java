package com.klu.casestudy2;

import java.time.Instant;
import java.util.Objects;

public record Shipment(TrackingNumber trackingNumber, Instant shippedAt) {
    public Shipment {
        Objects.requireNonNull(trackingNumber, "Tracking number cannot be null");
        Objects.requireNonNull(shippedAt, "ShippedAt date cannot be null");
    }
}
