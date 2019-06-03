package com.example.keabank.model;

import android.support.annotation.NonNull;

import java.util.UUID;

public class Transaction implements DatabaseItem {
    private UUID mId;
    private TransactionTarget mSource;
    private TransactionTarget mDestination;
    private float mAmount;
    private String mDescription;
    private boolean mDone;

    public Transaction(UUID id, TransactionTarget source, TransactionTarget destination, float amount, boolean done) {
        mId = id;
        mSource = source;
        mDestination = destination;
        mAmount = amount;
        mDone = done;
    }

    private Transaction(UUID id, TransactionTarget source, TransactionTarget destination, float amount, String description, boolean done) {
        mId = id;
        mSource = source;
        mDestination = destination;
        mAmount = amount;
        mDescription = description;
        mDone = done;
    }

    private Transaction() {
        mId = UUID.randomUUID();
        mDone = false;
        mAmount = -1f;
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

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
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
                this.mDescription,
                this.mDone
        );
    }
}

class TransactionException extends Exception {
    TransactionException(String cause) {
        super(cause);
    }
}
