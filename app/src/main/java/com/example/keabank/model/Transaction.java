package com.example.keabank.model;

import android.support.annotation.NonNull;

public class Transaction {
    private TransactionTarget mSource;
    private TransactionTarget mDestination;
    private float mAmount;
    private boolean mDone;

    private Transaction() {
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
}

class TransactionException extends Exception {
    TransactionException(String cause) {
        super(cause);
    }
}
