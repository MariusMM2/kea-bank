package exam.marius.keabank.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;
import exam.marius.keabank.util.ParcelUtils;
import exam.marius.keabank.util.StringUtils;
import exam.marius.keabank.util.TimeUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class Transaction implements DatabaseItem, Parcelable {

    private static final String TAG = "Transaction";
    private UUID mId;
    private TransactionTarget mSource; // X
    private TransactionTarget mDestination; // X
    private Type mType; // X
    private Status mStatus; // X
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
        this(UUID.randomUUID(), null, null, -1f, "", Status.IDLE, Type.NORMAL, Calendar.getInstance().getTime());
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

        mDestination = destination;

        Log.d(TAG, String.format("Set destination of transaction {%s} to {%s}", getId(), mDestination.getTitle()));

        return this;
    }

    public Transaction setAmount(float amount) {
        if (amount > 0) {
            mAmount = amount;
        } else {
            throw new IllegalArgumentException("Transaction can't process a negative amount");
        }

        Log.d(TAG, String.format("Set amount of transaction {%s} to {%s}", getId(), mAmount));

        return this;
    }

    public Transaction setType(Type type) {
        mType = type;

        Log.d(TAG, String.format("Set type of transaction {%s} to {%s}", getId(), mType));

        return this;
    }

    public Transaction setDate(Date date) {
        mDate = date;

        Log.d(TAG, String.format("Set date of transaction {%s} to {%s}", getId(), StringUtils.wrapDate(mDate)));

        return this;
    }

    public Transaction setTitle(String title) {
        mTitle = title;

        Log.d(TAG, String.format("Set title of transaction {%s} to {%s}", getId(), mTitle));

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
            setDefaultTitle();
            doTransaction();
        } else {
            throw new TransactionException("Transaction source has insufficient balance");
        }

        return this;
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

    public Transaction setMessage(String message) {
        mMessage = message;

        return this;
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
                this.mType,
                this.mDate
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return mId.equals(that.mId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mId);
    }

    protected Transaction(Parcel in) {
        mId = ParcelUtils.readUuid(in);
        mSource = in.readParcelable(TransactionTarget.class.getClassLoader());
        mDestination = in.readParcelable(TransactionTarget.class.getClassLoader());
        mAmount = in.readFloat();
        mMessage = in.readString();
        mType = ParcelUtils.readEnum(in, Type.class);
        mStatus = ParcelUtils.readEnum(in, Status.class);
        mTitle = in.readString();
        mDate = (Date) in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeUuid(dest, mId);

        mSource.prepareParcel();
        dest.writeParcelable(mSource, flags);
        mDestination.prepareParcel();
        dest.writeParcelable(mDestination, flags);

        dest.writeFloat(mAmount);
        if (getMessage().isEmpty()) {
            mMessage = mDestination.getDescription();
        }
        dest.writeString(mMessage);
        ParcelUtils.writeEnum(dest, mType);
        ParcelUtils.writeEnum(dest, mStatus);
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

    public Transaction setStatus(Status status) {
        mStatus = status;

        return this;
    }

    public boolean isDone() {
        return mStatus.equals(Status.DONE);
    }

    public boolean isClose() {
        return TimeUtils.weeksLeft(TimeUtils.getToday(), mDate) < 1;
    }

    public boolean commitOnTime() throws TransactionException {
        final boolean todayDueDate = TimeUtils.daysLeft(TimeUtils.getToday(), mDate) < 1;
        if (todayDueDate) {
            commit();
        }
        return todayDueDate;
    }

    public void setPending() {
        mStatus = Status.PENDING;
    }

    public enum Status {
        IDLE("Idle"),
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
        NORMAL("Normal"),
        PAYMENT_SERVICE("Payment Service"),
        MOBILEPAY("MobilePay");

        private final String mText;

        Type(String text) {
            mText = text;
        }

        public String getText() {
            return mText;
        }
    }
}

