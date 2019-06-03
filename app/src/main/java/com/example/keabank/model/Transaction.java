package com.example.keabank.model;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class Transaction implements DatabaseItem {
    private UUID mId;
    private TransactionTarget mSource;
    private TransactionTarget mDestination;
    private float mAmount;
    private String mMessage;
    private boolean mDone;
    private Date mDate;

    private Transaction(UUID id, TransactionTarget source, TransactionTarget destination, float amount, String message, boolean done, Date date) {
        mId = id;
        mSource = source;
        mDestination = destination;
        mAmount = amount;
        mMessage = message;
        mDone = done;
        mDate = date;
    }

    private Transaction() {
        mId = UUID.randomUUID();
        mDone = false;
        mAmount = -1f;
        mDate = Calendar.getInstance().getTime();
    }

    public static Transaction beginTransaction() {
        return new Transaction();
    }

    public Transaction setSource(@NonNull TransactionTarget source) {
        if (source == null) {
            throw new IllegalArgumentException("Transaction source is null");
        }

        if (mSource == null) {
            mSource = source;
        } else {
            throw new IllegalStateException("Transaction already has a source");
        }

        return this;
    }

    public Transaction setDestination(@NonNull TransactionTarget destination) {
        if (destination == null) {
            throw new IllegalArgumentException("Transaction destination is null");
        }

        if (mDestination == null) {
            mDestination = destination;
        } else {
            throw new IllegalStateException("Transaction already has a source");
        }

        return this;
    }

    public Transaction setAmount(float amount) {
        if (mAmount == -1) {
            if (amount > 0) {
                mAmount = amount;
            } else {
                throw new IllegalArgumentException("Transaction can't process a negative amount");
            }
        } else {
            throw new IllegalStateException("Transaction already has an amount");
        }

        return this;
    }

    public Transaction commit() throws TransactionException {
        if (mSource == null) {
            throw new TransactionException("Transaction has no source");
        }

        if (mDestination == null) {
            throw new TransactionException("Transaction has no destination");
        }

        if (mAmount == -1) {
            throw new TransactionException("Transaction has no amount for transfer");
        }

        if (mDone) {
            throw new TransactionException("Transaction has already finished");
        }

        if (mSource.canGoNegative() || mSource.canSubtractAmount(mAmount)) {
            doTransaction();
        } else {
            throw new TransactionException("Transaction source has insufficient balance");
        }

        return this;
    }

    private void doTransaction() {
        mSource.subtract(mAmount);
        mDestination.increase(mAmount);
        mDone = true;
    }

    @Override
    public UUID getId() {
        return mId;
    }

    public TransactionTarget getSource() {
        return mSource;
    }

    public TransactionTarget getDestination() {
        return mDestination;
    }

    public float getAmount() {
        return mAmount;
    }

    public boolean isDone() {
        return mDone;
    }

    public String getText() {
        return mDestination.getTitle() != null ? mDestination.getTitle() : mMessage.substring(0, 20);
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public UUID getSourceId() {
        return mSource.getId();
    }

    public UUID getDestinationId() {
        return mDestination.getId();
    }

    public Transaction reverse() {
        return new Transaction(this.mId,
                this.mDestination,
                this.mSource,
                -this.mAmount,
                this.mMessage,
                this.mDone,
                this.mDate
        );
    }

    public Date getDate() {
        return mDate;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "mId=" + mId +
                ", mSource=" + mSource +
                ", mDestination=" + mDestination +
                ", mAmount=" + mAmount +
                ", mMessage='" + mMessage + '\'' +
                ", mDone=" + mDone +
                ", mDate=" + mDate +
                '}';
    }
}

class TransactionException extends Exception {
    TransactionException(String cause) {
        super(cause);
    }
}
