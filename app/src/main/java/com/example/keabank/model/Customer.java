package com.example.keabank.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Customer implements DatabaseItem, Serializable {
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
}
