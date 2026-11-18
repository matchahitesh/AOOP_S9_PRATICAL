package com.klu.casestudy1;

import java.util.Objects;

public final class CartItem {
    private final Product product;
    private Quantity quantity;

    public CartItem(Product product, Quantity quantity) {
        this.product = Objects.requireNonNull(product, "Product cannot be null");
        this.quantity = Objects.requireNonNull(quantity, "Quantity cannot be null");
    }

    public Product getProduct() {
        return product;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public void setQuantity(Quantity quantity) {
        this.quantity = Objects.requireNonNull(quantity, "Quantity cannot be null");
    }

    public Money lineTotal() {
        return product.getDiscountedPrice().times(quantity);
    }

    @Override
    public String toString() {
        return quantity + "x " + product.getName() + " @ " + product.getDiscountedPrice() + " each = " + lineTotal();
    }
}
