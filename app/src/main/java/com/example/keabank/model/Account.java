package com.example.keabank.model;

import java.util.UUID;

public class Account {
    private UUID mId;
    private float mAmount;
    private Type mType;
    private Customer mLinkedCustomer;

    private enum Type {
        SAVINGS,
        BUDGET,
        PENSION,
        BUSINESS,
        DEFAULT
    }
}
