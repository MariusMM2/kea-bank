package com.example.keabank.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import com.example.keabank.util.ParcelHelper;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class Transaction implements DatabaseItem, Parcelable {
    private UUID mId;
    private TransactionTarget mSource;
    private TransactionTarget mDestination;
    private Type mType;
    private Status mStatus;
    private String mSourceDetails;
    private String mTitle;
    private float mAmount;
    private Date mDate;
    private String mMessage;

    private Transaction(UUID id, TransactionTarget source, TransactionTarget destination, float amount, String message, Status status, Type type, Date date) {
        mId = id;
        mSource = source;
        mDestination = destination;
        mAmount = amount;
        mMessage = message;
        mStatus = status;
        mType = type;
        mDate = date;
    }

    private Transaction() {
        this(UUID.randomUUID(), null, null, -1f, "", Status.STOPPED, Type.NORMAL, Calendar.getInstance().getTime());
    }

    public static final Creator<Transaction> CREATOR = new Creator<Transaction>() {
        @Override
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };

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

    public Transaction setType(Type type) {
        mType = type;
        return this;
    }

    public Transaction setTitle(String title) {
        mTitle = title;
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

        if (mStatus == Status.DONE) {
            throw new TransactionException("Transaction has already finished");
        }

        if (mSource.canGoNegative() || mSource.canSubtractAmount(mAmount)) {
            setTitle(mDestination.getTitle() != null ? mDestination.getTitle() : mMessage.substring(0, 20));
            doTransaction();
        } else {
            throw new TransactionException("Transaction source has insufficient balance");
        }

        return this;
    }

    private void doTransaction() {
        mSource.subtract(mAmount);
        mDestination.increase(mAmount);
        mStatus = Status.DONE;
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

    public Status getStatus() {
        return mStatus;
    }

    public String getMessage() {
        return mMessage == null ? "" : mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getSourceDetails() {
        return mSourceDetails;
    }

    public Type getType() {
        return mType;
    }

    public Date getDate() {
        return mDate;
    }

    public Transaction reverse() {
        return new Transaction(this.mId,
                this.mDestination,
                this.mSource,
                -this.mAmount,
                this.mMessage,
                this.mStatus,
                Type.NORMAL, this.mDate
        );
    }

    public String getTitle() {
        return mTitle;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "mId=" + mId +
                ", mSource=" + mSource +
                ", mDestination=" + mDestination +
                ", mAmount=" + mAmount +
                ", mMessage='" + mMessage + '\'' +
                ", mStatus=" + mStatus +
                ", mDate=" + mDate +
                '}';
    }

    protected Transaction(Parcel in) {
        mId = ParcelHelper.readUuid(in);
        mAmount = in.readFloat();
        mMessage = in.readString();
        mType = ParcelHelper.readEnum(in, Type.class);
        mStatus = ParcelHelper.readEnum(in, Status.class);
        mSourceDetails = in.readString();
        mTitle = in.readString();
        mDate = (Date) in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelHelper.writeUuid(dest, mId);
        dest.writeFloat(mAmount);
        dest.writeString(mMessage);
        ParcelHelper.writeEnum(dest, mType);
        ParcelHelper.writeEnum(dest, mStatus);
        if (mSourceDetails == null || mSourceDetails.isEmpty()) {
            mSourceDetails = mSource.getTitle();
        }
        dest.writeString(mSourceDetails);
        if (mTitle == null || mTitle.isEmpty()) {
            mTitle = mDestination.getTitle();
        }
        dest.writeString(mTitle);
        dest.writeSerializable(mDate);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public enum Status {
        STOPPED("Stopped"),
        PENDING("Pending"),
        CANCELED("Canceled"),
        DONE("Done");

        private final String mText;

        Status(String text) {
            mText = text;
        }

        public String getText() {
            return mText;
        }
    }

    public enum Type {
        PAYMENT_SERVICE("Payment Service"),
        MOBILEPAY("MobilePay"),
        NORMAL("Normal");

        private final String mText;

        Type(String text) {
            mText = text;
        }

        public String getText() {
            return mText;
        }
    }
}

class TransactionException extends Exception {
    TransactionException(String cause) {
        super(cause);
    }
}
