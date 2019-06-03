package com.example.keabank.model;

public interface TransactionTarget extends DatabaseItem {
    float getAmount();

    boolean canSubtractAmount(float amount);

    void subtract(float amount);

    void increase(float amount);

    boolean canGoNegative();
}
