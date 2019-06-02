package com.example.keabank.model;

import java.util.UUID;

public class NemId implements DatabaseItem {
    private UUID mId;
    private String mUsername, mPassword;
    private Customer mLinkedCustomer;

    public NemId(UUID id, String username, String password, Customer linkedCustomer) {
        mId = id;
        mUsername = username;
        mPassword = password;
        mLinkedCustomer = linkedCustomer;
    }

    public NemId(String username, String password) {
        mId = UUID.randomUUID();
        mUsername = username;
        mPassword = password;
    }

    @Override
    public UUID getId() {
        return mId;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getPassword() {
        return mPassword;
    }

    public Customer getCustomer() {
        return mLinkedCustomer;
    }

    public void setCustomer(Customer customer) {
        mLinkedCustomer = customer;
    }
}
