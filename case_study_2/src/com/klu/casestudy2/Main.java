package com.klu.casestudy2; //java -cp "bin\main" com.klu.casestudy2.Main

import java.time.Instant;

public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   CASE STUDY 2: Order Lifecycle & Invariants     ");
        System.out.println("==================================================");

        // --- Demonstrating Model A (Guarded State Machine) ---
        System.out.println("\n[1] Running Model A Lifecycle:");
        Order order = new Order(new OrderId("ORD-2026-001"));
        
        // Add lines
        ProductId p1 = new ProductId("SKU-KEYBOARD");
        ProductId p2 = new ProductId("SKU-MONITOR");
        order.addLine(p1, 7500, 1); // $75.00 x 1
        order.addLine(p1, 7500, 2); // $75.00 x 2 (merges with p1 -> 3 total)
        order.addLine(p2, 29900, 1); // $299.00 x 1

        System.out.println("Order Lines:");
        for (OrderLine line : order.lines()) {
            System.out.println(" - " + line);
        }
        System.out.println("Derived Total: $" + (order.totalMinor() / 100.0));

        // State Transitions
        order.place();
        System.out.println("State after place(): " + order.status());
        
        order.pay();
        System.out.println("State after pay():   " + order.status());

        order.ship(new TrackingNumber("TRK-FEDEX-9988"), Instant.now());
        System.out.println("State after ship():  " + order.status() + " | Tracking: " + order.shipment().get().trackingNumber().code());

        // Invariant Guard Tests
        System.out.println("\n--- Testing Invariant Guards in Model A ---");
        try {
            System.out.print("Attempting to cancel an already SHIPPED order: ");
            order.cancel(Instant.now());
        } catch (DomainException e) {
            System.out.println("BLOCKED -> " + e.getMessage());
        }

        try {
            System.out.print("Attempting to add lines after order is placed: ");
            order.addLine(p1, 7500, 1);
        } catch (DomainException e) {
            System.out.println("BLOCKED -> " + e.getMessage());
        }

        // --- Demonstrating Model B (Type-per-state) ---
        System.out.println("\n[2] Running Model B (Compile-Time Type Safety):");
        DraftOrder draft = new DraftOrder(new OrderId("ORD-MODEL-B-101"));
        draft.addLine(p1, 5000, 2);
        
        PlacedOrder placed = draft.place();
        PaidOrder paid = placed.pay("PAYPAL-TRANS-1234");
        ShippedOrder shipped = paid.ship(new TrackingNumber("TRK-UPS-5544"), Instant.now());
        DeliveredOrder delivered = shipped.deliver(Instant.now());

        System.out.println("Successfully transitioned Draft -> Placed -> Paid -> Shipped -> Delivered!");
        System.out.println("Shipped order tracking: " + shipped.trackingNumber().code());
        System.out.println("Notice: ShippedOrder and DeliveredOrder do not even have a cancel() method!");

        System.out.println("==================================================");
        System.out.println("  Case Study 2 Models Successfully Validated!     ");
        System.out.println("==================================================");
    }
}
