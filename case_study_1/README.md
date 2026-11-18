# Case Study 1: Money and Pricing in an E-Commerce Catalog

## Overview
An online shop sells products in several currencies. Each product has a price, may carry a percentage discount, and the cart shows totals.

### Domain Invariants Enforced
1. **Exact Representation**: Amounts are exact minor integer units (no binary floating-point drift).
2. **Currency Precision**: Scale is bounded by the currency's minor units (e.g. 2 for USD/EUR, 0 for JPY, 3 for KWD).
3. **No Direct Mismatch Addition**: Adding money with different currencies throws `CurrencyMismatchException`.
4. **Non-negative Prices**: Price cannot be negative.
5. **Bounded Percentage**: Discounts are strictly between `0%` and `100%` (0 to 10,000 basis points).
6. **Strict Quantities**: Quantity must be a strictly positive integer (`>= 1`).

---

## Exam Questions & Model Answers

### Question 1 (4 Marks): Why `double` is unsuitable & minor unit representation
- **Why double is unsuitable**: Binary floating point (IEEE 754) cannot represent fractional powers of ten (such as 0.10 or 0.01) exactly, leading to compounding rounding errors and discrepancy between cart and invoices.
- **How minor units address the problem**: Storing minor units as a `long` integer (e.g., cents for USD) ensures all addition, subtraction, and whole-number multiplication operations are 100% exact.
- **Remaining design decision**: The model must still explicitly specify the **rounding mode** (e.g., `RoundingMode.HALF_EVEN`) when dividing or computing percentage discounts.

### Question 2 (6 Marks): Entity vs. Value Object Classification
- **Money (Value Object)**: Defined solely by its minor units and currency; two instances with identical values are interchangeable.
- **Percentage (Value Object)**: Defined entirely by its numeric ratio with no distinct lifecycle.
- **Quantity (Value Object)**: Represents a dimensionless positive count without persistent identity.
- **Product (Entity)**: Possesses a persistent identity (SKU) that outlives changes to its title, description, or price.
- **Cart (Entity / Aggregate Root)**: Identified by a unique Cart ID; its state changes as items are added and removed over time.

### Question 3 (8 Marks): `Percentage` Value Object Design
- Constructor is **`private`** to guarantee that instances can only be instantiated through static factory methods (`ofBasisPoints`, `ofPercent`), making invalid percentage states (such as `< 0%` or `> 100%`) completely unrepresentable at runtime.

### Question 4 (6 Marks): Analysis of Silent Currency Conversion in `Money.plus`
- **Against silent conversion**: 
  1. *Purity*: Value objects should have pure mathematical operations with referential transparency. Calling today's conversion rate introduces hidden volatile side-effects (external clocks and network rates).
  2. *Testability*: Simple addition tests would require mocking currency feeds or databases.
  3. *Invariants*: Currency conversion belongs in a domain service with an explicit rate and date, not silently within addition.

### Question 5 (6 Marks): Negative Amounts / Refunds
- Introduce a distinct type hierarchy:
  1. `Price`: Guarantees non-negative amount (`>= 0`) enforced by type system for products and cart items.
  2. `Refund`: Encapsulates a positive refund amount explicitly typed as a reimbursement event.
  3. `MonetaryAdjustment` / `SignedMoney`: Represents ledger-level credits or debits, keeping product pricing strictly non-negative while allowing accounting adjustments.
