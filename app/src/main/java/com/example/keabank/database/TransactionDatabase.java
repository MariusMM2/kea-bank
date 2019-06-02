package com.example.keabank.database;

import android.content.Context;

public class TransactionDatabase extends AbstractDatabase {
    private static TransactionDatabase sInstance;

    private TransactionDatabase(Context context) {
        super(context);
    }

    public static TransactionDatabase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TransactionDatabase(context);
        }
        return sInstance;
    }
}
