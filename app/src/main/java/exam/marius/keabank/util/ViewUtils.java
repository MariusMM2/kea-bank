package exam.marius.keabank.util;

import android.view.View;
import android.widget.TextView;
import exam.marius.keabank.R;
import exam.marius.keabank.model.Account;

public class ViewUtils {
    public static void bindAccount(Account account, View accountView) {
        TextView typeTextView = accountView.findViewById(R.id.text_destination);
        TextView amountTextView = accountView.findViewById(R.id.text_amount);
        TextView idTextView = accountView.findViewById(R.id.text_id);
        bindAccount(account, typeTextView, amountTextView, idTextView);
    }

    public static void bindAccount(Account account, TextView typeTextView, TextView amountTextView, TextView idTextView) {
        typeTextView.setText(String.valueOf(account.getType().getText()));

        float amount = account.getAmount();
        amountTextView.setText(typeTextView.getResources().getString(R.string.amount, amount));

        idTextView.setText(account.getId().toString());
    }
}
