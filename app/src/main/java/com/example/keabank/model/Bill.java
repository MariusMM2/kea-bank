package com.example.keabank.model;

import java.util.Date;

public class Bill {
    private String mTitle, mDescription;
    private boolean mAutomated, mRecurrent, mPendingPayment;
    private float mAmount;
    private Date mDueDate;
    private Customer mLinkedCustomer;

}
