package com.example.keabank.model;

public interface TransactionTarget {
    float getAmount();

    boolean canSubtractAmount(float amount);

    // Save in case i manage to change language level
//    default boolean canSubtractAmount(float amount) {
//        if (canGoNegative()) {
//            return true;
//        } else {
//            return getAmount() - amount > 0;
//        }
//    }

    void subtract(float amount);

    void increase(float amount);

    boolean canGoNegative();
}
