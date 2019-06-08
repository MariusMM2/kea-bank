package exam.marius.keabank.model;

import android.os.Parcel;
import android.os.Parcelable;
import exam.marius.keabank.util.ParcelUtils;

import java.util.*;

public class Customer implements DatabaseItem, Parcelable {
    private UUID mId;
    private String mFirstName, mLastName, mEmail, mPassword;
    private Date mBirthDate;
    private transient List<Account> mAccountList;

    public Customer(UUID id, String firstName, String lastName, String email, String password, Date birthDate) {
        mId = id;
        mFirstName = firstName;
        mLastName = lastName;
        mEmail = email;
        mPassword = password;
        mBirthDate = birthDate;
        mAccountList = new ArrayList<>();
    }

    public Customer(String firstName, String lastName, String email, String password, Date birthDate) {
        this(UUID.randomUUID(), firstName, lastName, email, password, birthDate);
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

    public String getEmail() {
        return mEmail;
    }

    public String getPassword() {
        return mPassword;
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

    public void removeAccounts() {
        mAccountList = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Customer{" +
                "mId=" + mId +
                ", mFirstName='" + mFirstName + '\'' +
                ", mLastName='" + mLastName + '\'' +
                ", mEmail='" + mEmail + '\'' +
                ", mPassword='" + mPassword + '\'' +
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
        mEmail = in.readString();
        mPassword = in.readString();
        mAccountList = ParcelUtils.readList(in, Account.class);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeUuid(dest, mId);
        dest.writeString(mFirstName);
        dest.writeString(mLastName);
        dest.writeString(mEmail);
        dest.writeString(mPassword);
        ParcelUtils.writeList(dest, mAccountList);
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
}
