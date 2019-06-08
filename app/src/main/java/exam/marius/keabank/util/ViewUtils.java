package exam.marius.keabank.util;

import android.view.View;
import android.widget.TextView;
import exam.marius.keabank.R;
import exam.marius.keabank.model.Account;

import java.util.function.BiConsumer;

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

        idTextView.setText(account.getNumber());
    }

    public static BiConsumer<TextView, String> newViewInputError(boolean[] validInput, View[] focusView) {
        if (validInput.length != 1) {
            throw new IllegalArgumentException("validInput must be an one element array");
        } else if (focusView.length != 1) {
            throw new IllegalArgumentException("focusView must be an one element array");
        } else {
            return new BiConsumer<TextView, String>() {
                final boolean[] mValidInput = validInput;
                final View[] mFocusView = focusView;

                @Override
                public void accept(TextView targetView, String errorMessage) {
                    mValidInput[0] = false;
                    if (targetView != null && errorMessage != null) {
                        targetView.setError(errorMessage);
                        mFocusView[0] = targetView;
                    }
                }
            };
        }
    }
}
