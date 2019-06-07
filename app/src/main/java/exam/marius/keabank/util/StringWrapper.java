package exam.marius.keabank.util;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import exam.marius.keabank.R;

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
        return wrapDate(date, false);
    }

    public static CharSequence wrapDate(Date date, boolean showIfCurrentDay) {
        CharSequence charSequence = DateFormat.format(
                sContext.getResources().getString(R.string.date_format),
                date);

        if (showIfCurrentDay && charSequence.equals(wrapDate(new Date()))) {
            charSequence = sContext.getResources().getString(R.string.date_today);
        }

        return charSequence;
    }
}
