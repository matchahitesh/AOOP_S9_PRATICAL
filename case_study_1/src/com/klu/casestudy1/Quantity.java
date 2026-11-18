package com.klu.casestudy1;

import java.util.Objects;

public final class Quantity {
    private final int value;

    private Quantity(int value) {
        this.value = value;
    }

    public static Quantity of(int value) {
        if (value < 1) {
            throw new IllegalArgumentException("Quantity must be a positive integer (>= 1). Given: " + value);
        }
        return new Quantity(value);
    }

    public int value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Quantity other)) return false;
        return this.value == other.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
