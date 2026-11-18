package com.klu.casestudy2;

import java.util.Objects;

public final class OrderLine {
    private final ProductId productId;
    private final long unitPriceMinor; // exact minor units (e.g. cents)
    private final int quantity;

    public OrderLine(ProductId productId, long unitPriceMinor, int quantity) {
        this.productId = Objects.requireNonNull(productId, "ProductId cannot be null");
        if (unitPriceMinor < 0) throw new IllegalArgumentException("Unit price cannot be negative");
        if (quantity < 1) throw new IllegalArgumentException("Quantity must be at least 1");
        this.unitPriceMinor = unitPriceMinor;
        this.quantity = quantity;
    }

    public ProductId productId() {
        return productId;
    }

    public long unitPriceMinor() {
        return unitPriceMinor;
    }

    public int quantity() {
        return quantity;
    }

    public long totalMinor() {
        return Math.multiplyExact(unitPriceMinor, (long) quantity);
    }

    public OrderLine combine(OrderLine other) {
        if (!this.productId.equals(other.productId)) {
            throw new IllegalArgumentException("Cannot combine order lines of different products");
        }
        return new OrderLine(this.productId, this.unitPriceMinor, this.quantity + other.quantity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderLine other)) return false;
        return unitPriceMinor == other.unitPriceMinor && quantity == other.quantity && productId.equals(other.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, unitPriceMinor, quantity);
    }

    @Override
    public String toString() {
        return quantity + "x " + productId.value() + " @ " + (unitPriceMinor / 100.0) + " = $" + (totalMinor() / 100.0);
    }
}
