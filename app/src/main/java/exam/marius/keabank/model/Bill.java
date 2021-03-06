package exam.marius.keabank.model;

import android.os.Parcel;
import exam.marius.keabank.util.ParcelUtils;
import exam.marius.keabank.util.TimeUtils;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class Bill implements TransactionTarget {
    private final UUID mId;
    private final String mTitle, mDescription;
    private final float mAmount;
    private final Date mDueDate;
    private final UUID mCustomerId;
    private boolean mAutomated, mRecurrent, mPendingPayment, mDone;

    public Bill(String title, String description, boolean recurrent, float amount, Date dueDate, UUID customerId) {
        this(title, description, false, recurrent, amount, dueDate, customerId);
    }

    public Bill(String title, String description, boolean automated, boolean recurrent, float amount, Date dueDate, UUID customerId) {
        mId = UUID.randomUUID();
        mTitle = title;
        mDescription = description;
        mAutomated = automated;
        mRecurrent = recurrent;
        mPendingPayment = false;
        mDone = false;
        mAmount = amount;
        mDueDate = dueDate;
        mCustomerId = customerId;
    }

    public void setAutomated(boolean automated) {
        mAutomated = automated;
    }

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

    public UUID getCustomerId() {
        return mCustomerId;
    }

    @Override
    public void prepareParcel() {

    }

    public boolean isRecurrent() {
        return mRecurrent;
    }

    public boolean isOpen() {
        return !mDone && !mAutomated;
    }

    public Date getDueDate() {
        return mDueDate;
    }

    @Override
    public float getAmount() {
        return mAmount;
    }

    @Override
    public boolean canGoNegative() {
        return true;
    }

    @Override
    public boolean canSubtractAmount(float amount) {
        return true;
    }

    @Override
    public void subtract(float amount) {
        onTransaction();
    }

    @Override
    public void increase(float amount) {
        onTransaction();
    }

    private void onTransaction() {
        mDone = true;
        mPendingPayment = false;
    }

    public Bill next() {
        return new Bill(
                mTitle,
                mDescription,
                mAutomated,
                mRecurrent,
                mAmount,
                TimeUtils.addMonths(mDueDate, 1),
                mCustomerId
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bill bill = (Bill) o;
        return mId.equals(bill.mId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mId);
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
}
