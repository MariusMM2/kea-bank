package com.example.keabank.database;

import android.content.Context;

public class CustomerDatabase extends AbstractDatabase {
    private static CustomerDatabase sInstance;

    private CustomerDatabase(Context context) {
        super(context);
    }

    public static CustomerDatabase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new CustomerDatabase(context);
        }
        return sInstance;
    }
}
