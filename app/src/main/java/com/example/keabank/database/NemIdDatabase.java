package com.example.keabank.database;

import android.content.Context;

class NemIdDatabase extends AbstractDatabase {
    NemIdDatabase(Context context) {
        super(context);
    }

    @Override
    String getItemsFileName() {
        return "nemids";
    }
}
