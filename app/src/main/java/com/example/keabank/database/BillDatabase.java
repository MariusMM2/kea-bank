package com.example.keabank.database;

import android.content.Context;

public class BillDatabase extends AbstractDatabase {
    private static BillDatabase sInstance;

    private BillDatabase(Context context) {
        super(context);
    }

    public static BillDatabase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new BillDatabase(context);
        }
        return sInstance;
    }
}
