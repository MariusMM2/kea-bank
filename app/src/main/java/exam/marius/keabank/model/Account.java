package exam.marius.keabank.model;

import android.os.Parcel;
import exam.marius.keabank.util.ParcelUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Account implements TransactionTarget {
    private UUID mId;
    private float mAmount;
    private Type mType;
    private UUID mCustomerId;
    private transient List<Transaction> mTransactionList;

    // Called at deserialization,
    // instantiates any transient fields
    // to a default value
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        mTransactionList = new ArrayList<>();
        in.defaultReadObject();
    }

    public Account(float amount, Type type, UUID customerId) {
        mId = UUID.randomUUID();
        mAmount = amount;
        mType = type;
        mCustomerId = customerId;
        mTransactionList = new ArrayList<>();
    }

    public static Account newSavings(float amount, UUID customerId) {
        return new Account(amount, Type.SAVINGS, customerId);
    }

    public static Account newBudget(float amount, UUID customerId) {
        return new Account(amount, Type.BUDGET, customerId);
    }

    public static Account newPension(float amount, UUID customerId) {
        return new Account(amount, Type.PENSION, customerId);
    }

    public static Account newBusiness(float amount, UUID customerId) {
        return new Account(amount, Type.BUSINESS, customerId);
    }

    public static Account newDefault(float amount, UUID customerId) {
        return new Account(amount, Type.DEFAULT, customerId);
    }

    @Override
    public UUID getId() {
        return mId;
    }

    public String getIdNumber() {
        return String.format("%x", mId.getMostSignificantBits());
    }

    public Type getType() {
        return mType;
    }

    @Override
    public float getAmount() {
        return mAmount;
    }


    // Always True for credit accounts
    // getAmount() - amount > 0 for debit accounts
    @Override
    public boolean canSubtractAmount(float amount) {
        if (canGoNegative()) {
            return true;
        } else {
            return getAmount() - amount > 0;
        }
    }

    @Override
    public void subtract(float amount) {
        mAmount -= amount;
    }

    @Override
    public void increase(float amount) {
        mAmount += amount;
    }

    @Override
    public boolean canGoNegative() {
        return true;
    }

    @Override
    public String getTitle() {
        return String.format("%s - %x", getType().getText(), mId.getMostSignificantBits());
    }

    @Override
    public String getDescription() {
        return getTitle();
    }

    @Override
    public void prepareParcel() {
        mTransactionList = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return mId.equals(account.mId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mId);
    }

    public UUID getCustomerId() {
        return mCustomerId;
    }

    public void setCustomerId(UUID customerId) {
        mCustomerId = customerId;
    }

    public List<Transaction> getTransactionList() {
        return mTransactionList;
    }

    public void addTransaction(Transaction transactions) {
        mTransactionList.add(transactions);
    }

    public enum Type {
        SAVINGS("Savings"),
        BUDGET("Budget"),
        PENSION("Pension"),
        BUSINESS("Business"),
        DEFAULT("Default");

        private final String mText;

        Type(String text) {
            mText = text;
        }

        public String getText() {
            return mText;
        }
    }

    protected Account(Parcel in) {
        mId = ParcelUtils.readUuid(in);
        mAmount = in.readFloat();
        mType = ParcelUtils.readEnum(in, Type.class);
        mCustomerId = ParcelUtils.readUuid(in);
        mTransactionList = ParcelUtils.readList(in, Transaction.class);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeUuid(dest, mId);
        dest.writeFloat(mAmount);
        ParcelUtils.writeEnum(dest, mType);
        ParcelUtils.writeUuid(dest, mCustomerId);
        ParcelUtils.writeList(dest, mTransactionList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Account> CREATOR = new Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel in) {
            return new Account(in);
        }

        @Override
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };
}
