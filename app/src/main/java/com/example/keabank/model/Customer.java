package com.example.keabank.model;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Customer implements DatabaseItem {
    private UUID mId;
    private String mFirstName, mLastName, mCPR, mEmail, mPassword;
    private Date mBirthDate;
    private List<Account> mAccountList;

    public Customer(UUID id, String firstName, String lastName, String CPR, String email, String password, Date birthDate, List<Account> accountList) {
        mId = id;
        mFirstName = firstName;
        mLastName = lastName;
        mCPR = CPR;
        mEmail = email;
        mPassword = password;
        mBirthDate = birthDate;
        mAccountList = accountList;
    }

    public Customer(String firstName, String lastName, String CPR, String email, String password, Date birthDate) {
        mId = UUID.randomUUID();
        mFirstName = firstName;
        mLastName = lastName;
        mCPR = CPR;
        mEmail = email;
        mPassword = password;
        mBirthDate = birthDate;
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

    public String getCPR() {
        return mCPR;
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
}
