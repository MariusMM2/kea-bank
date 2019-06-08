package exam.marius.keabank.model;

import android.os.Parcel;
import exam.marius.keabank.util.ParcelUtils;

import java.util.Date;
import java.util.UUID;

public class Bill implements TransactionTarget {
    private final float mAmount;
    private UUID mId;
    private String mTitle, mDescription;
    private boolean mAutomated, mRecurrent, mPendingPayment;
    private Date mDueDate;
    private UUID mCustomerId;

    public Bill(UUID id, String title, String description, boolean automated, boolean recurrent, boolean pendingPayment, float amount, Date dueDate, UUID customerId) {
        mId = id;
        mTitle = title;
        mDescription = description;
        mAutomated = automated;
        mRecurrent = recurrent;
        mPendingPayment = pendingPayment;
        mAmount = amount;
        mDueDate = dueDate;
        mCustomerId = customerId;
    }

    public Bill(String title, String description, boolean recurrent, float amount, Date dueDate, UUID customerId) {
        mId = UUID.randomUUID();
        mTitle = title;
        mDescription = description;
        mRecurrent = recurrent;
        mAutomated = false;
        mAmount = amount;
        mPendingPayment = false;
        mDueDate = dueDate;
        mCustomerId = customerId;
    }

    protected Bill(Parcel in) {
        mAmount = in.readFloat();
        mId = ParcelUtils.readUuid(in);
        mTitle = in.readString();
        mDescription = in.readString();
        mAutomated = in.readByte() != 0;
        mRecurrent = in.readByte() != 0;
        mPendingPayment = in.readByte() != 0;
        mDueDate = (Date) in.readSerializable();
        mCustomerId = ParcelUtils.readUuid(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(mAmount);
        ParcelUtils.writeUuid(dest, mId);
        dest.writeString(mTitle);
        dest.writeString(mDescription);
        dest.writeByte((byte) (mAutomated ? 1 : 0));
        dest.writeByte((byte) (mRecurrent ? 1 : 0));
        dest.writeByte((byte) (mPendingPayment ? 1 : 0));
        dest.writeSerializable(mDueDate);
        ParcelUtils.writeUuid(dest, mCustomerId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Bill> CREATOR = new Creator<Bill>() {
        @Override
        public Bill createFromParcel(Parcel in) {
            return new Bill(in);
        }

        @Override
        public Bill[] newArray(int size) {
            return new Bill[size];
        }
    };

    @Override
    public UUID getId() {
        return mId;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getDescription() {
        return mDescription == null ? "" : mDescription;
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

    public boolean isOpen() {
        return !mAutomated && !mPendingPayment;
    }

    public Date getDueDate() {
        return mDueDate;
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

    public UUID getCustomerId() {
        return mCustomerId;
    }

    public void setCustomerId(UUID customerId) {
        mCustomerId = customerId;
    }
}
