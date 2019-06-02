package com.example.keabank.model;

import java.util.UUID;

public class Account implements DatabaseItem, TransactionTarget {
    private UUID mId;
    private float mAmount;
    private Type mType;
    private Customer mLinkedCustomer;

    public Account(UUID id, float amount, Type type, Customer linkedCustomer) {
        mId = id;
        mAmount = amount;
        mType = type;
        mLinkedCustomer = linkedCustomer;
    }

    public Account(float amount, Type type) {
        mId = UUID.randomUUID();
        mAmount = amount;
        mType = type;
    }

    @Override
    public UUID getId() {
        return mId;
    }

    public Customer getCustomer() {
        return mLinkedCustomer;
    }

    public Type getType() {
        return mType;
    }

    public void setCustomer(Customer customer) {
        mLinkedCustomer = customer;
    }

    @Override
    public float getAmount() {
        return mAmount;
    }


    // Always True for credit accounts
    // getAmount() - amount > 0 for debit accounts
    @Override
    public boolean canSubtractAmount(float amount) {
        if (canGoNegative()) {
            return true;
        } else {
            return getAmount() - amount > 0;
        }
    }

    @Override
    public void subtract(float amount) {
        mAmount -= amount;
    }

    @Override
    public void increase(float amount) {
        mAmount += amount;
    }

    @Override
    public boolean canGoNegative() {
        return true;
    }

    private enum Type {
        SAVINGS,
        BUDGET,
        PENSION,
        BUSINESS,
        DEFAULT
    }
}
