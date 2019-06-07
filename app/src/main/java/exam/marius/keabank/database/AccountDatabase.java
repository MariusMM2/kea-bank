package exam.marius.keabank.database;

import android.content.Context;
import exam.marius.keabank.model.Account;

class AccountDatabase extends AbstractDatabase<Account> {
    AccountDatabase(Context context) {
        super(context);
    }

    @Override
    String getItemsFileName() {
        return "accounts";
    }
}
