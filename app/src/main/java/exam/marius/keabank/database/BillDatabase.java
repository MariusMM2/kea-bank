package exam.marius.keabank.database;

import android.content.Context;

class BillDatabase extends AbstractDatabase {
    BillDatabase(Context context) {
        super(context);
    }

    @Override
    String getItemsFileName() {
        return "bills";
    }
}
