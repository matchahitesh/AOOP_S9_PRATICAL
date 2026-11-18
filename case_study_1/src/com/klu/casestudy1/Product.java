package com.klu.casestudy1;

import java.util.Objects;

/**
 * Product is an Entity: identified by its persistent sku / id.
 */
public final class Product {
    private final String sku;
    private String name;
    private Money price;
    private Percentage discount;

    public Product(String sku, String name, Money price, Percentage discount) {
        this.sku = Objects.requireNonNull(sku, "SKU cannot be null");
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.price = Objects.requireNonNull(price, "Price cannot be null");
        this.discount = Objects.requireNonNull(discount, "Discount cannot be null");
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public Money getPrice() {
        return price;
    }

    public void setPrice(Money price) {
        this.price = Objects.requireNonNull(price);
    }

    public Percentage getDiscount() {
        return discount;
    }

    public void setDiscount(Percentage discount) {
        this.discount = Objects.requireNonNull(discount);
    }

    public Money getDiscountedPrice() {
        return price.minus(discount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product other)) return false;
        return this.sku.equals(other.sku);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku);
    }

    @Override
    public String toString() {
        return "Product[sku=" + sku + ", name=" + name + ", price=" + price + ", discount=" + discount + "]";
    }
}
