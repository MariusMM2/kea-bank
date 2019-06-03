package com.example.keabank.database;

import android.content.Context;

import java.util.Calendar;

public class CustomerDatabase extends AbstractDatabase {
    CustomerDatabase(Context context) {
        super(context);
        Calendar.getInstance().set(1990, 7, 31);
    }

    @Override
    String getItemsFileName() {
        return "customers";
    }
}
