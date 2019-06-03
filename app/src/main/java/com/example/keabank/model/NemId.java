package com.example.keabank.model;

import java.util.UUID;

public class NemId implements DatabaseItem {
    private UUID mId;
    private String mUsername, mPassword;
    private UUID mCustomerId;

    public NemId(UUID id, String username, String password, UUID customerId) {
        mId = id;
        mUsername = username;
        mPassword = password;
        mCustomerId = customerId;
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

    public UUID getCustomerId() {
        return mCustomerId;
    }

    public void setCustomerId(UUID customerId) {
        mCustomerId = customerId;
    }
}
