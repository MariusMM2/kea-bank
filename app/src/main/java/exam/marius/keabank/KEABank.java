package exam.marius.keabank;

import android.app.Application;
import exam.marius.keabank.util.StringUtils;

public class KEABank extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        StringUtils.init(this.getApplicationContext());
    }
}
