package com.example.keabank.model;

import java.util.Date;
import java.util.UUID;

public class Bill implements TransactionTarget {
    private final float mAmount;
    private UUID mId;
    private String mTitle, mDescription;
    private boolean mAutomated, mRecurrent, mPendingPayment;
    private Date mDueDate;
    private Customer mLinkedCustomer;

    public Bill(UUID id, String title, String description, boolean automated, boolean recurrent, boolean pendingPayment, float amount, Date dueDate, Customer linkedCustomer) {
        mId = id;
        mTitle = title;
        mDescription = description;
        mAutomated = automated;
        mRecurrent = recurrent;
        mPendingPayment = pendingPayment;
        mAmount = amount;
        mDueDate = dueDate;
        mLinkedCustomer = linkedCustomer;
    }

    public Bill(String title, String description, boolean recurrent, float amount, Date dueDate) {
        mId = UUID.randomUUID();
        mTitle = title;
        mDescription = description;
        mRecurrent = recurrent;
        mAutomated = false;
        mAmount = amount;
        mPendingPayment = false;
        mDueDate = dueDate;
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public boolean isAutomated() {
        return mAutomated;
    }

    public boolean isRecurrent() {
        return mRecurrent;
    }

    public boolean isPendingPayment() {
        return mPendingPayment;
    }

    public Date getDueDate() {
        return mDueDate;
    }

    public Customer getCustomer() {
        return mLinkedCustomer;
    }

    public void setCustomer(Customer linkedCustomer) {
        mLinkedCustomer = linkedCustomer;
    }

    @Override
    public float getAmount() {
        return mAmount;
    }

    @Override
    public boolean canSubtractAmount(float amount) {
        return false;
    }

    @Override
    public void subtract(float amount) {
        throw new IllegalStateException();
    }

    @Override
    public void increase(float amount) {
        mPendingPayment = true;
    }

    @Override
    public boolean canGoNegative() {
        return false;
    }
}
