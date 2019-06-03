package com.example.keabank.database;

import android.content.Context;

public class NemIdDatabase extends AbstractDatabase {
    NemIdDatabase(Context context) {
        super(context);
    }

    @Override
    String getItemsFileName() {
        return "nemids";
    }
}
