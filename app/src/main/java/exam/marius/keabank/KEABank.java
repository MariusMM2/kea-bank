package exam.marius.keabank;

import android.app.Application;
import exam.marius.keabank.util.StringWrapper;

public class KEABank extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        StringWrapper.init(this.getApplicationContext());
    }
}
