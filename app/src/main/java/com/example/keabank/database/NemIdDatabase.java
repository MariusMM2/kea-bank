package com.example.keabank.database;

import android.content.Context;

public class NemIdDatabase extends AbstractDatabase {
    private static NemIdDatabase sInstance;

    private NemIdDatabase(Context context) {
        super(context);
    }

    public static NemIdDatabase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new NemIdDatabase(context);
        }
        return sInstance;
    }
}
