package exam.marius.keabank.database;

import android.content.Context;

class TransactionDatabase extends AbstractDatabase {
    TransactionDatabase(Context context) {
        super(context);
    }

    @Override
    String getItemsFileName() {
        return "transactions";
    }
}
