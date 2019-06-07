package exam.marius.keabank.database;

import android.content.Context;
import exam.marius.keabank.model.Bill;

class BillDatabase extends AbstractDatabase<Bill> {
    BillDatabase(Context context) {
        super(context);
    }

    @Override
    String getItemsFileName() {
        return "bills";
    }
}
