package com.example.keabank.database;

import android.content.Context;

public class AccountDatabase extends AbstractDatabase {
    private static AccountDatabase sInstance;

    private AccountDatabase(Context context) {
        super(context);
    }

    public static AccountDatabase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AccountDatabase(context);
        }
        return sInstance;
    }
}
