package exam.marius.keabank.database;

import android.content.Context;
import exam.marius.keabank.model.Transaction;

class TransactionDatabase extends AbstractDatabase<Transaction> {
    TransactionDatabase(Context context) {
        super(context);
    }

    @Override
    String getItemsFileName() {
        return "transactions";
    }
}
