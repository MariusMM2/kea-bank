package com.example.keabank.database;

import android.content.Context;

public class BillDatabase extends AbstractDatabase {
    BillDatabase(Context context) {
        super(context);
    }

    @Override
    String getItemsFileName() {
        return "bills";
    }
}
