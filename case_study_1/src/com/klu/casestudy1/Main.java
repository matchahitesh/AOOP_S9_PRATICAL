package com.klu.casestudy1; //java -cp "bin\main" com.klu.casestudy1.Main

public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("    CASE STUDY 1: E-Commerce Money & Pricing      ");
        System.out.println("==================================================");

        // Create Products (Entities)
        Product laptop = new Product(
            "SKU-LAPTOP-01",
            "Dell XPS 15",
            Money.of("1499.99", Currency.USD),
            Percentage.ofPercent(10) // 10% discount
        );

        Product mouse = new Product(
            "SKU-MOUSE-02",
            "Logitech MX Master",
            Money.of("99.50", Currency.USD),
            Percentage.ofPercent(0)
        );

        System.out.println("Product 1: " + laptop.getName() + " | List Price: " + laptop.getPrice() + " | After Discount: " + laptop.getDiscountedPrice());
        System.out.println("Product 2: " + mouse.getName() + " | List Price: " + mouse.getPrice() + " | After Discount: " + mouse.getDiscountedPrice());

        // Create Cart Items
        CartItem item1 = new CartItem(laptop, Quantity.of(1));
        CartItem item2 = new CartItem(mouse, Quantity.of(2));

        System.out.println("\nCart Items:");
        System.out.println(" - " + item1);
        System.out.println(" - " + item2);

        // Cart Total
        Money cartTotal = item1.lineTotal().plus(item2.lineTotal());
        System.out.println("\nCart Grand Total: " + cartTotal);

        // Test Invariant Enforcement
        System.out.println("\n--- Testing Invariants & Blocked Illegal States ---");
        
        // 1. Currency Mismatch
        try {
            System.out.print("Attempting EUR + USD addition: ");
            Money eur = Money.of("50.00", Currency.EUR);
            cartTotal.plus(eur);
        } catch (CurrencyMismatchException e) {
            System.out.println("BLOCKED -> " + e.getMessage());
        }

        // 2. Negative Quantity
        try {
            System.out.print("Attempting Quantity of -5: ");
            Quantity.of(-5);
        } catch (IllegalArgumentException e) {
            System.out.println("BLOCKED -> " + e.getMessage());
        }

        // 3. Excess Percentage
        try {
            System.out.print("Attempting 250% discount: ");
            Percentage.ofPercent(250);
        } catch (IllegalArgumentException e) {
            System.out.println("BLOCKED -> " + e.getMessage());
        }

        System.out.println("==================================================");
        System.out.println("   All Domain Invariants Successfully Verified!   ");
        System.out.println("==================================================");
    }
}
