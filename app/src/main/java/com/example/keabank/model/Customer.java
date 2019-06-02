package com.example.keabank.model;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Customer {
    private UUID mId;
    private String mFirstName, mLastName, mCPR, mEmail, mPassword;
    private Date mBirthDate;
    private List<Account> mAccountList;
}
