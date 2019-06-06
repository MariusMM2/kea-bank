package com.example.keabank.util;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import com.example.keabank.R;

import java.util.Date;

public class StringWrapper {
    private static final String TAG = "StringWrapper";
    private static Context sContext;

    public static void init(Context context) {
        if (sContext == null) {
            sContext = context;
            Log.d(TAG, "init: added Context reference");
        }
    }

    public static CharSequence wrapDate(Date date) {
        return DateFormat.format(
                sContext.getResources().getString(R.string.date_format),
                date);
    }
}
