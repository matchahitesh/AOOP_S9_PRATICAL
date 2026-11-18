package com.klu.casestudy1;

public class CurrencyMismatchException extends RuntimeException {
    public CurrencyMismatchException(Currency expected, Currency actual) {
        super("Currency mismatch: expected " + expected + " but received " + actual);
    }
}
