package com.example.keabank;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.keabank.model.Account;
import com.example.keabank.model.Customer;
import com.example.keabank.model.Transaction;

import java.util.List;

public class AccountTransactionsActivity extends AppCompatActivity {
    private static final String TAG = "AccountTransactionsActivity";

    private Account mAccount;

    private RecyclerView mTransactionsList;
    private TransactionAdapter mTransactionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_transactions);

        mAccount = Customer.getDummyCustomer().getAccountList().get(0);

        mTransactionsList = findViewById(R.id.list_transactions);

        mTransactionAdapter = new TransactionAdapter(mAccount);
        mTransactionsList.setAdapter(mTransactionAdapter);
        mTransactionAdapter.notifyDataSetChanged();
    }

    /**
     * ViewHolder for a Transaction.
     * Holds the description, the date, the amount
     * and the balance after the transaction.
     */

    private class TransactionHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Account mParentAccount;
        private Transaction mTransaction;

        private TextView mDescriptionTextView;
        private TextView mAmountTextView;
        private TextView mDateTextView;
        private TextView mBalanceAfterTextView;

        TransactionHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_transaction, parent, false));
            mDescriptionTextView = itemView.findViewById(R.id.text_description);
            mAmountTextView = itemView.findViewById(R.id.text_amount);
            mDateTextView = itemView.findViewById(R.id.text_date);
            mBalanceAfterTextView = itemView.findViewById(R.id.text_balance_after);
            Log.d(TAG, "Created TransactionHolder");
        }

        private void bind(Transaction transaction, Account account) {
            mParentAccount = account;
            mTransaction = transaction;

            mDescriptionTextView.setText(String.valueOf(mTransaction.getText()));

            float amount = -mTransaction.getAmount();
            mAmountTextView.setText(getResources().getString(R.string.amount, amount));
            mAmountTextView.setTextColor(getResources().getColor(amount > 0 ? R.color.green : R.color.red, getTheme()));

            mDateTextView.setText(DateFormat.format(
                    getResources().getString(R.string.date_format),
                    mTransaction.getDate()));

            float accumulatedTransactionsAmount = mParentAccount.getTransactionList().subList(0, getAdapterPosition())
                    .stream()
                    .reduce(0f,
                            (transaction1, transaction2) -> transaction1 + transaction2.getAmount(),
                            Float::sum);

            float balanceAfter = mParentAccount.getAmount() + accumulatedTransactionsAmount;
            mBalanceAfterTextView.setText(getResources().getString(R.string.amount, balanceAfter));


            Log.d(TAG, String.format("Bound transaction %s to holder", mTransaction.getText()));
        }

        @Override
        public void onClick(View v) {

        }
    }

    private class TransactionAdapter extends RecyclerView.Adapter<TransactionHolder> {

        private Account mAccount;

        private TransactionAdapter(Account account) {
            mAccount = account;
        }

        @NonNull
        @Override
        public TransactionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(AccountTransactionsActivity.this);
            return new TransactionHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull TransactionHolder transactionHolder, int i) {

        }

        @Override
        public void onBindViewHolder(@NonNull TransactionHolder transactionHolder, int i, @NonNull List payloads) {
            Transaction transaction = mAccount.getTransactionList().get(i);
            transactionHolder.bind(transaction, mAccount);
        }

        @Override
        public int getItemCount() {
            return mAccount.getTransactionList().size();
        }
    }
}
