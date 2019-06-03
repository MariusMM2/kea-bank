package com.example.keabank.database;

import android.content.Context;

public class AccountDatabase extends AbstractDatabase {
    AccountDatabase(Context context) {
        super(context);
    }

    @Override
    String getItemsFileName() {
        return "accounts";
    }
}
