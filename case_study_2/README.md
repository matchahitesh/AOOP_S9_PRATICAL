# Case Study 2: Order Lifecycle and State Transitions

## Overview
An order is created as a draft, lines are added, then it is placed, paid, shipped, and delivered. It can be cancelled before shipment.

---

## Exam Questions & Detailed Solutions

### Question 1 (Analysis, 6 marks)
**List at least four illegal or contradictory states reachable in the naive Order class. For each, state which invariant it violates.**

1. **State: `status = "SHIPPED"` with `lines = empty list` (or null)**
   * *Invariant Violated:* "An order that has been placed has at least one line." An empty order must never be placed, paid, or shipped.
2. **State: `status = "SHIPPED"` and `cancelled = true` (or `cancelledAt` is populated)**
   * *Invariant Violated:* "Shipped and Delivered cannot be cancelled." Once goods are shipped into physical transit, the order cannot be in a cancelled state simultaneously.
3. **State: `status = "DRAFT"` but `shippedAt` date / tracking data is non-null**
   * *Invariant Violated:* "`shippedAt` and tracking number exist if and only if the order has been shipped." A draft order cannot possess dispatch timestamps.
4. **State: `lines` has been modified/added/removed, but `total` remains unchanged (or total does not match line sums)**
   * *Invariant Violated:* "Total equals the sum of line totals, always." In the naive model, `lines` is a public mutable list and `total` is a decoupled mutable primitive.

---

### Question 2 (Design, 8 marks)
**(a) State Transition Table for the Order Lifecycle:**

| Current State | Event / Trigger | Next State | Condition / Invariant Guard |
| :--- | :--- | :--- | :--- |
| **DRAFT** | `addLine()` / `removeLine()` | **DRAFT** | Permitted only while in DRAFT. Unique by product. |
| **DRAFT** | `place()` | **PLACED** | Guard: `lines.size() >= 1` |
| **DRAFT** | `cancel()` | **CANCELLED** | Valid prior to shipment. |
| **PLACED** | `pay()` | **PAID** | Must be PLACED first. |
| **PLACED** | `cancel()` | **CANCELLED** | Valid prior to shipment. |
| **PAID** | `ship(tracking, at)` | **SHIPPED** | Guard: `status == PAID`, tracking & date non-null. |
| **PAID** | `cancel()` | **CANCELLED** | Valid prior to shipment (triggers refund workflow). |
| **SHIPPED** | `deliver(at)` | **DELIVERED** | Reached destination. |
| **SHIPPED** | `cancel()` | *Illegal* | Throws DomainException (cannot cancel shipped order). |
| **DELIVERED**| `cancel()` | *Illegal* | Throws DomainException. |
| **CANCELLED**| Any transition | *Illegal* | Terminal state. |

**(b) Implementation of `ship` method in Model A:**
```java
public void ship(TrackingNumber trackingNumber, Instant at) {
    // 1. Guard check: only PAID orders can transition to SHIPPED
    requireStatus(Status.PAID);
    
    // 2. Guaranteed non-null shipment data
    this.shipment = Optional.of(new Shipment(
        Objects.requireNonNull(trackingNumber, "Tracking number required"),
        Objects.requireNonNull(at, "Shipment timestamp required")
    ));
    
    // 3. Atomically transition state
    this.status = Status.SHIPPED;
}

private void requireStatus(Status expected) {
    if (this.status != expected) {
        throw new DomainException("Invalid operation. Expected " + expected + " but was " + this.status);
    }
}
```

* **How this prevents a cancelled order from being shipped:**  
  When an order is cancelled, its status is explicitly set to `Status.CANCELLED`. When `order.ship(...)` is invoked, the guard `requireStatus(Status.PAID)` immediately checks the state. Because `Status.CANCELLED != Status.PAID`, the method unconditionally throws a `DomainException` before touching shipment fields or modifying the status.

---

### Question 3 (Comparison, 8 marks)
**Compare Model A (Guarded Entity) and Model B (One Type per State):**

| Criterion | Model A (Guarded Entity) | Model B (State Types / Sealed Hierarchy) |
| :--- | :--- | :--- |
| **Type Safety** | Runtime enforcement: illegal transitions throw runtime exceptions (`DomainException`). | Compile-time enforcement: invalid methods do not exist on the type (e.g., `ShippedOrder` lacks a `cancel()` method). |
| **Code Volume** | Low: single class, standard enum, and private helper guard methods. | High: requires an interface plus separate classes/records for every state, transition return types, and mapping boilerplates. |
| **Persistence Mapping** | Simple: directly maps to a single database table (`orders`) with an enum column `status`. | Complex: requires ORM inheritance strategy (e.g., table-per-hierarchy or table-per-subclass) and state reconstructors. |

* **Recommendation for a system with 8 states that changes often:**  
  **Recommend Model A (Guarded Entity).**  
  *Justification:* With 8 states and frequent requirement churn, Model B causes an exponential explosion in classes, transition methods, and database discriminator mappings (each state change requires refactoring upstream and downstream classes). Model A encapsulates the state transitions cleanly in a single aggregate root with centralized guard clauses, making changes rapid, maintainable, and simple to map in relational persistence.

---

### Question 4 (Short answer, 4 marks)
* **Why total is better computed from lines than stored as a field:**  
  Deriving `total` directly (`lines.values().stream().mapToLong(...).sum()`) eliminates redundant state and synchronization bugs. It makes "total out-of-sync with line items" mathematically impossible (single source of truth).
* **Situation where storing it would be justified:**  
  When querying massive catalogs, reporting queries, or paginating over millions of orders where computing sums on read incurs unacceptable database I/O and CPU latency.
* **Extra invariant to protect:**  
  The aggregate root must strictly encapsulate mutations so that every `addLine`, `removeLine`, or quantity change synchronously updates the stored total in the same transaction, or the list must be made strictly immutable outside the aggregate boundary.

---

### Question 5 (Application, 6 marks)
**"An order must not be placed if the customer's credit limit would be exceeded."**

1. **Is this an Order invariant?**  
   **No.** It is a **cross-aggregate business rule**, not an invariant of the `Order` aggregate. The `Order` aggregate owns its items, quantities, and status; the customer's credit limit and balance belong to the `Customer` (or `Account`/`Credit`) aggregate. An aggregate boundary must only enforce rules within data it directly owns.

2. **Where the rule should live:**  
   It belongs in a **Domain Service** or an **Application Workflow Service** (e.g., `OrderPlacementUseCase` / `CheckoutService`).

3. **How to avoid a race condition between simultaneous orders:**  
   * **Pessimistic Locking / Atomic Reservation:** In the database, execute `SELECT ... FOR UPDATE` on the customer's credit account row when validating and reserving credit.
   * **Optimistic Locking:** Use a `@Version` number on the `CustomerCredit` aggregate. If two concurrent transactions attempt to debit credit at the same time, the second transaction fails with an optimistic locking exception and is retried.
   * **Saga / Reservation Pattern:** The order is placed in a `PENDING_CREDIT_APPROVAL` state, an asynchronous credit reservation command is processed sequentially by the credit ledger, and upon confirmation, the order transitions to `PLACED` (compensating/cancelling if credit fails).
