package com.klu.casestudy1;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Percentage {
    private final int basisPoints; // 0 to 10,000 (representing 0% to 100%)

    private Percentage(int basisPoints) {
        this.basisPoints = basisPoints;
    }

    public static Percentage ofBasisPoints(int basisPoints) {
        if (basisPoints < 0 || basisPoints > 10_000) {
            throw new IllegalArgumentException("Basis points must be between 0 and 10000 (0% to 100%). Given: " + basisPoints);
        }
        return new Percentage(basisPoints);
    }

    public static Percentage ofPercent(int percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100. Given: " + percent);
        }
        return new Percentage(percent * 100);
    }

    public Money applyTo(Money money) {
        BigDecimal minor = BigDecimal.valueOf(money.minor());
        BigDecimal rate = BigDecimal.valueOf(basisPoints).divide(BigDecimal.valueOf(10_000), 6, RoundingMode.UNNECESSARY);
        long calculatedMinor = minor.multiply(rate).setScale(0, RoundingMode.HALF_EVEN).longValueExact();
        return Money.ofMinor(calculatedMinor, money.currency());
    }

    public int basisPoints() {
        return basisPoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Percentage other)) return false;
        return this.basisPoints == other.basisPoints;
    }

    @Override
    public int hashCode() {
        return Objects.hash(basisPoints);
    }

    @Override
    public String toString() {
        return (basisPoints / 100.0) + "%";
    }
}
