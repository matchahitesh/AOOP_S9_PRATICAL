package com.klu.casestudy2;

import java.time.Instant;
import java.util.*;

/**
 * Model B: One Type Per State (Compile-time Type Safety using Java 17 Sealed Types).
 * Transitions that are invalid at a given stage simply do not exist as methods on that type.
 */
public sealed interface ModelBOrder permits 
    DraftOrder, PlacedOrder, PaidOrder, ShippedOrder, DeliveredOrder, CancelledOrder {

    OrderId id();
    List<OrderLine> lines();
}

final class DraftOrder implements ModelBOrder {
    private final OrderId id;
    private final Map<ProductId, OrderLine> lines = new LinkedHashMap<>();

    public DraftOrder(OrderId id) {
        this.id = Objects.requireNonNull(id);
    }

    @Override
    public OrderId id() { return id; }

    @Override
    public List<OrderLine> lines() { return List.copyOf(lines.values()); }

    public DraftOrder addLine(ProductId productId, long unitPriceMinor, int quantity) {
        lines.merge(productId, new OrderLine(productId, unitPriceMinor, quantity), OrderLine::combine);
        return this;
    }

    public PlacedOrder place() {
        if (lines.isEmpty()) {
            throw new DomainException("Cannot place an empty order. At least one line is required.");
        }
        return new PlacedOrder(id, List.copyOf(lines.values()));
    }

    public CancelledOrder cancel(String reason) {
        return new CancelledOrder(id, lines(), reason);
    }
}

final class PlacedOrder implements ModelBOrder {
    private final OrderId id;
    private final List<OrderLine> lines;

    PlacedOrder(OrderId id, List<OrderLine> lines) {
        this.id = id;
        this.lines = lines;
    }

    @Override
    public OrderId id() { return id; }

    @Override
    public List<OrderLine> lines() { return lines; }

    public PaidOrder pay(String paymentReference) {
        return new PaidOrder(id, lines, paymentReference);
    }

    public CancelledOrder cancel(String reason) {
        return new CancelledOrder(id, lines, reason);
    }
}

final class PaidOrder implements ModelBOrder {
    private final OrderId id;
    private final List<OrderLine> lines;
    private final String paymentReference;

    PaidOrder(OrderId id, List<OrderLine> lines, String paymentReference) {
        this.id = id;
        this.lines = lines;
        this.paymentReference = paymentReference;
    }

    @Override
    public OrderId id() { return id; }

    @Override
    public List<OrderLine> lines() { return lines; }

    public String paymentReference() { return paymentReference; }

    public ShippedOrder ship(TrackingNumber trackingNumber, Instant shippedAt) {
        return new ShippedOrder(id, lines, trackingNumber, shippedAt);
    }

    public CancelledOrder cancel(String reason) {
        return new CancelledOrder(id, lines, reason);
    }
}

final class ShippedOrder implements ModelBOrder {
    private final OrderId id;
    private final List<OrderLine> lines;
    private final TrackingNumber trackingNumber;
    private final Instant shippedAt;

    ShippedOrder(OrderId id, List<OrderLine> lines, TrackingNumber trackingNumber, Instant shippedAt) {
        this.id = id;
        this.lines = lines;
        this.trackingNumber = Objects.requireNonNull(trackingNumber);
        this.shippedAt = Objects.requireNonNull(shippedAt);
    }

    @Override
    public OrderId id() { return id; }

    @Override
    public List<OrderLine> lines() { return lines; }

    public TrackingNumber trackingNumber() { return trackingNumber; }
    public Instant shippedAt() { return shippedAt; }

    // NOTE: ShippedOrder has NO cancel(...) method! Impossible to cancel at compile time.
    public DeliveredOrder deliver(Instant deliveredAt) {
        return new DeliveredOrder(id, lines, trackingNumber, shippedAt, deliveredAt);
    }
}

final class DeliveredOrder implements ModelBOrder {
    private final OrderId id;
    private final List<OrderLine> lines;
    private final TrackingNumber trackingNumber;
    private final Instant shippedAt;
    private final Instant deliveredAt;

    DeliveredOrder(OrderId id, List<OrderLine> lines, TrackingNumber trackingNumber, Instant shippedAt, Instant deliveredAt) {
        this.id = id;
        this.lines = lines;
        this.trackingNumber = trackingNumber;
        this.shippedAt = shippedAt;
        this.deliveredAt = deliveredAt;
    }

    @Override
    public OrderId id() { return id; }

    @Override
    public List<OrderLine> lines() { return lines; }

    public Instant deliveredAt() { return deliveredAt; }
}

final class CancelledOrder implements ModelBOrder {
    private final OrderId id;
    private final List<OrderLine> lines;
    private final String reason;

    CancelledOrder(OrderId id, List<OrderLine> lines, String reason) {
        this.id = id;
        this.lines = lines;
        this.reason = reason;
    }

    @Override
    public OrderId id() { return id; }

    @Override
    public List<OrderLine> lines() { return lines; }

    public String reason() { return reason; }
}
