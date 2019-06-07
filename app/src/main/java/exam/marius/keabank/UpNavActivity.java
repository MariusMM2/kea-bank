package exam.marius.keabank;

import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public abstract class UpNavActivity extends AppCompatActivity {
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
