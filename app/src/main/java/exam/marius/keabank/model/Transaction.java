package exam.marius.keabank.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;
import exam.marius.keabank.util.ParcelHelper;
import exam.marius.keabank.util.StringWrapper;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class Transaction implements DatabaseItem, Parcelable {
    private static final String TAG = "Transaction";
    private UUID mId;
    private TransactionTarget mSource; // X
    private TransactionTarget mDestination; // X
    private Type mType; // X
    private Status mStatus; // X
    private String mSourceDetails; // X
    private String mTitle; // X
    private float mAmount; // X
    private Date mDate; // X
    private String mMessage; // X

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
        Transaction transaction = new Transaction();
        Log.i(TAG, String.format("Started Transaction {%s}", transaction.getId()));
        return transaction;
    }

    public Transaction setSource(@NonNull TransactionTarget source) {
        if (source == null) {
            throw new IllegalArgumentException("Transaction source is null");
        }

        mSource = source;

        Log.i(TAG, String.format("Set source of transaction {%s} to {%s}", getId(), mSource.getTitle()));

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

        Log.i(TAG, String.format("Set destination of transaction {%s} to {%s}", getId(), mDestination.getTitle()));

        return this;
    }

    public Transaction setAmount(float amount) {
        if (amount > 0) {
            mAmount = amount;
        } else {
            throw new IllegalArgumentException("Transaction can't process a negative amount");
        }

        Log.i(TAG, String.format("Set amount of transaction {%s} to {%s}", getId(), mAmount));

        return this;
    }

    public void setType(Type type) {
        mType = type;

        Log.i(TAG, String.format("Set type of transaction {%s} to {%s}", getId(), mType));
    }

    public void setDate(Date date) {
        mDate = date;

        Log.i(TAG, String.format("Set date of transaction {%s} to {%s}", getId(), StringWrapper.wrapDate(mDate)));
    }

    public void setTitle(String title) {
        mTitle = title;

        Log.i(TAG, String.format("Set title of transaction {%s} to {%s}", getId(), mTitle));
    }

    public void commit() throws TransactionException {
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
            setDefaultTitle();
            doTransaction();
        } else {
            throw new TransactionException("Transaction source has insufficient balance");
        }
    }

    public void setDefaultTitle() {
        setTitle(mDestination.getTitle() != null ? mDestination.getTitle() : mMessage.substring(0, 20));
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
        if (getMessage().isEmpty()) {
            mMessage = mDestination.getDescription();
        }
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

    public void setStatus(Status status) {
        mStatus = status;
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
