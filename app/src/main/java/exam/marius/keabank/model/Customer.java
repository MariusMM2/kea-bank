package exam.marius.keabank.model;

import android.os.Parcel;
import android.os.Parcelable;
import exam.marius.keabank.util.ParcelUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

public class Customer implements DatabaseItem, Parcelable {
    private UUID mId;
    private String mFirstName, mLastName;
    private Date mBirthDate;
    private transient List<Account> mAccountList;
    private Affiliate mAffiliate;

    // Called at deserialization,
    // instantiates any transient fields
    // to a default value
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        mAccountList = new ArrayList<>();
        in.defaultReadObject();
    }

    public Customer(String firstName, String lastName, Date birthDate) {
        mId = UUID.randomUUID();
        mFirstName = firstName;
        mLastName = lastName;
        mBirthDate = birthDate;
        mAccountList = new ArrayList<>();
        mAffiliate = Affiliate.COPENHAGEN;
    }

    @Override
    public UUID getId() {
        return mId;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public Date getBirthDate() {
        return mBirthDate;
    }

    public List<Account> getAccountList() {
        return mAccountList;
    }

    public void addAccount(Account account) {
        mAccountList.add(account);
    }

    public void setAffiliate(Affiliate affiliate) {
        mAffiliate = affiliate;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "mId=" + mId +
                ", mFirstName='" + mFirstName + '\'' +
                ", mLastName='" + mLastName + '\'' +
                ", mBirthDate=" + mBirthDate +
                ", mAccountList=" + mAccountList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return mId.equals(customer.mId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mId);
    }

    // Parcelable packaging and marshalling logic
    protected Customer(Parcel in) {
        mId = ParcelUtils.readUuid(in);
        mFirstName = in.readString();
        mLastName = in.readString();
        mBirthDate = new Date(in.readLong());
        mAccountList = ParcelUtils.readList(in, Account.class);
        mAffiliate = ParcelUtils.readEnum(in, Affiliate.class);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeUuid(dest, mId);
        dest.writeString(mFirstName);
        dest.writeString(mLastName);
        dest.writeLong(mBirthDate.getTime());
        ParcelUtils.writeList(dest, mAccountList);
        ParcelUtils.writeEnum(dest, mAffiliate);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Customer> CREATOR = new Creator<Customer>() {
        @Override
        public Customer createFromParcel(Parcel in) {
            return new Customer(in);
        }

        @Override
        public Customer[] newArray(int size) {
            return new Customer[size];
        }
    };

    public Affiliate getAffiliate() {
        return mAffiliate;
    }

    public enum Affiliate {
        COPENHAGEN("Copenhagen"),
        ODENSE("Odense");

        private final String mText;

        Affiliate(String text) {
            mText = text;
        }

        public String getText() {
            return mText;
        }
    }
}
