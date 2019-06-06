package com.example.keabank;

import android.app.Application;
import com.example.keabank.util.StringWrapper;

public class KEABank extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        StringWrapper.init(this.getApplicationContext());
    }
}
