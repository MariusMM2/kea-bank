package com.example.keabank.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Account implements TransactionTarget, Serializable {
    private UUID mId;
    private float mAmount;
    private Type mType;
    private UUID mCustomerId;
    private transient List<Transaction> mTransactionList;

    public Account(UUID id, float amount, Type type, UUID customerId, Transaction... transactions) {
        mId = id;
        mAmount = amount;
        mType = type;
        mCustomerId = customerId;
        mTransactionList = Arrays.asList(transactions);
    }

    public Account(float amount, Type type, UUID customerId) {
        mId = UUID.randomUUID();
        mAmount = amount;
        mType = type;
        mCustomerId = customerId;
        mTransactionList = new ArrayList<>();
    }

    public static Account newSavings(float amount, UUID customerId) {
        return new Account(amount, Type.SAVINGS, customerId);
    }

    public static Account newBudget(float amount, UUID customerId) {
        return new Account(amount, Type.BUDGET, customerId);
    }

    public static Account newPension(float amount, UUID customerId) {
        return new Account(amount, Type.PENSION, customerId);
    }

    public static Account newBusiness(float amount, UUID customerId) {
        return new Account(amount, Type.BUSINESS, customerId);
    }

    public static Account newDefault(float amount, UUID customerId) {
        return new Account(amount, Type.DEFAULT, customerId);
    }

    @Override
    public UUID getId() {
        return mId;
    }

    public Type getType() {
        return mType;
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

    public UUID getCustomerId() {
        return mCustomerId;
    }

    public void setCustomerId(UUID customerId) {
        mCustomerId = customerId;
    }

    public List<Transaction> getTransactionList() {
        return mTransactionList;
    }

    public void addTransaction(Transaction transactions) {
        mTransactionList.add(transactions);
    }

    private enum Type {
        SAVINGS,
        BUDGET,
        PENSION,
        BUSINESS,
        DEFAULT
    }
}
