package exam.marius.keabank.util;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import exam.marius.keabank.R;

import java.util.Date;
import java.util.Random;

public class StringUtils {
    private static final String TAG = "StringUtils";
    private static Context sContext;
    private static Random sRandom = new Random();

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

    public static String getRandomNumerical(int digitCount) {
        long l1 = sRandom.nextLong();
        long l2 = l1 < 0 ? l1 + Long.MAX_VALUE : l1;

        return String.valueOf(l2).substring(0, digitCount);
    }
}
