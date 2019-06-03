package com.example.keabank.model;

public interface TransactionTarget {
    float getAmount();

    boolean canSubtractAmount(float amount);

    void subtract(float amount);

    void increase(float amount);

    boolean canGoNegative();
}
