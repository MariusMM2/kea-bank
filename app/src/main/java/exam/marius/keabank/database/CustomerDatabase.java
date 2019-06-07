package exam.marius.keabank.database;

import android.content.Context;
import exam.marius.keabank.model.Customer;

import java.util.Calendar;

class CustomerDatabase extends AbstractDatabase<Customer> {
    CustomerDatabase(Context context) {
        super(context);
        Calendar.getInstance().set(1990, 7, 31);
    }

    @Override
    String getItemsFileName() {
        return "customers";
    }
}
