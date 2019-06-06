package com.example.keabank.model;

import android.os.Parcelable;

public interface TransactionTarget extends DatabaseItem, Parcelable {
    float getAmount();

    boolean canSubtractAmount(float amount);

    void subtract(float amount);

    void increase(float amount);

    boolean canGoNegative();

    String getTitle();

    String getDescription();
}
