package com.klu.casestudy2;

import java.time.Instant;
import java.util.*;

/**
 * Model A: Single Entity With a Guarded State Machine.
 * Total is derived from lines, not stored, preventing drifting bugs.
 */
public final class Order {
    private final OrderId id;
    private Status status = Status.DRAFT;
    private final Map<ProductId, OrderLine> lines = new LinkedHashMap<>();
    private Optional<Shipment> shipment = Optional.empty();
    private Optional<Instant> cancelledAt = Optional.empty();

    public Order(OrderId id) {
        this.id = Objects.requireNonNull(id, "OrderId cannot be null");
    }

    public OrderId id() {
        return id;
    }

    public Status status() {
        return status;
    }

    public void addLine(ProductId productId, long unitPriceMinor, int quantity) {
        requireStatus(Status.DRAFT);
        OrderLine newLine = new OrderLine(productId, unitPriceMinor, quantity);
        lines.merge(productId, newLine, OrderLine::combine);
    }

    public void removeLine(ProductId productId) {
        requireStatus(Status.DRAFT);
        if (lines.remove(productId) == null) {
            throw new DomainException("Product not found in order lines: " + productId);
        }
    }

    public void place() {
        requireStatus(Status.DRAFT);
        if (lines.isEmpty()) {
            throw new DomainException("Cannot place an empty order. At least one line is required.");
        }
        status = Status.PLACED;
    }

    public void pay() {
        requireStatus(Status.PLACED);
        status = Status.PAID;
    }

    public void ship(TrackingNumber trackingNumber, Instant at) {
        requireStatus(Status.PAID);
        this.shipment = Optional.of(new Shipment(trackingNumber, at));
        this.status = Status.SHIPPED;
    }

    public void deliver() {
        requireStatus(Status.SHIPPED);
        status = Status.DELIVERED;
    }

    public void cancel(Instant at) {
        if (status == Status.SHIPPED || status == Status.DELIVERED) {
            throw new DomainException("Cannot cancel an order that is already " + status);
        }
        if (status == Status.CANCELLED) {
            throw new DomainException("Order is already cancelled");
        }
        this.cancelledAt = Optional.of(at);
        this.status = Status.CANCELLED;
    }

    public long totalMinor() {
        return lines.values().stream()
                    .mapToLong(OrderLine::totalMinor)
                    .sum();
    }

    public List<OrderLine> lines() {
        return List.copyOf(lines.values());
    }

    public Optional<Shipment> shipment() {
        return shipment;
    }

    private void requireStatus(Status expected) {
        if (this.status != expected) {
            throw new DomainException("Invalid operation for order in status " + this.status + ". Expected status: " + expected);
        }
    }
}
