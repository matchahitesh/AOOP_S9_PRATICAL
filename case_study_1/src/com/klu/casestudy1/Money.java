package com.klu.casestudy1;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money {
    private final long minor;
    private final Currency currency;

    private Money(long minor, Currency currency) {
        if (minor < 0) {
            throw new IllegalArgumentException("Price/Money amount cannot be negative. Given: " + minor);
        }
        this.minor = minor;
        this.currency = Objects.requireNonNull(currency, "Currency cannot be null");
    }

    public static Money ofMinor(long minor, Currency currency) {
        return new Money(minor, currency);
    }

    public static Money of(String decimal, Currency currency) {
        Objects.requireNonNull(decimal, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        BigDecimal bd = new BigDecimal(decimal);
        if (bd.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative: " + decimal);
        }
        if (bd.scale() > currency.getMinorUnits()) {
            throw new IllegalArgumentException(
                "Excess precision for " + currency + ". Allowed minor units: " + currency.getMinorUnits() + ", given: " + bd.scale()
            );
        }
        BigDecimal scaled = bd.movePointRight(currency.getMinorUnits());
        return new Money(scaled.longValueExact(), currency);
    }

    public Money plus(Money other) {
        requireSame(other);
        return new Money(Math.addExact(this.minor, other.minor), this.currency);
    }

    public Money times(Quantity quantity) {
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        return new Money(Math.multiplyExact(this.minor, quantity.value()), this.currency);
    }

    public Money minus(Percentage percentage) {
        Objects.requireNonNull(percentage, "Percentage cannot be null");
        Money discount = percentage.applyTo(this);
        long resultMinor = Math.max(0, this.minor - discount.minor);
        return new Money(resultMinor, this.currency);
    }

    private void requireSame(Money other) {
        Objects.requireNonNull(other, "Operand cannot be null");
        if (this.currency != other.currency) {
            throw new CurrencyMismatchException(this.currency, other.currency);
        }
    }

    public long minor() {
        return minor;
    }

    public Currency currency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money other)) return false;
        return this.minor == other.minor && this.currency == other.currency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minor, currency);
    }

    @Override
    public String toString() {
        BigDecimal decimal = BigDecimal.valueOf(minor).movePointLeft(currency.getMinorUnits());
        return decimal.toPlainString() + " " + currency.name();
    }
}
