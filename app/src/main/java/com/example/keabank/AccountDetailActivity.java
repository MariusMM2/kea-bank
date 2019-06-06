package com.example.keabank;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.keabank.model.Account;
import com.example.keabank.model.Transaction;
import com.example.keabank.util.ModelBinding;
import com.example.keabank.util.StringWrapper;

import java.util.List;

public class AccountDetailActivity extends UpNavActivity {
    private static final String TAG = "AccountDetailActivity";

    private static final String EXTRA_ACCOUNT = "com.example.extras.EXTRA_ACCOUNT";

    private View mAccountView;
    private RecyclerView mTransactionsListView;
    private TransactionAdapter mTransactionAdapter;

    private Account mAccount;

    static Intent newIntent(Context packageContext, Account account) {
        Intent intent = new Intent(packageContext, AccountDetailActivity.class);
        intent.putExtra(EXTRA_ACCOUNT, (Parcelable) account);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_detail);

        mAccount = getIntent().getParcelableExtra(EXTRA_ACCOUNT);

        mAccountView = findViewById(R.id.include);
        ModelBinding.bindAccount(mAccount, mAccountView);

        mTransactionsListView = findViewById(R.id.list_transactions);

        mTransactionAdapter = new TransactionAdapter(mAccount);
        mTransactionsListView.setAdapter(mTransactionAdapter);
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
            itemView.setOnClickListener(this);
            mDescriptionTextView = itemView.findViewById(R.id.text_description);
            mAmountTextView = itemView.findViewById(R.id.text_amount);
            mDateTextView = itemView.findViewById(R.id.text_date);
            mBalanceAfterTextView = itemView.findViewById(R.id.text_balance_after);
            Log.d(TAG, "Created TransactionHolder");
        }

        private void bind(Transaction transaction, Account account) {
            mParentAccount = account;
            mTransaction = transaction;

            mDescriptionTextView.setText(String.valueOf(mTransaction.getTitle()));

            float amount = -mTransaction.getAmount();
            mAmountTextView.setText(getResources().getString(R.string.amount, amount));
            mAmountTextView.setTextColor(getResources().getColor(amount > 0 ? R.color.green : R.color.red, getTheme()));

            mDateTextView.setText(StringWrapper.wrapDate(mTransaction.getDate()));

            float accumulatedTransactionsAmount = mParentAccount.getTransactionList().subList(0, getAdapterPosition())
                    .stream()
                    .reduce(0f,
                            (transaction1, transaction2) -> transaction1 + transaction2.getAmount(),
                            Float::sum);

            float balanceAfter = mParentAccount.getAmount() + accumulatedTransactionsAmount;
            mBalanceAfterTextView.setText(getResources().getString(R.string.amount, balanceAfter));


            Log.d(TAG, String.format("Bound transaction %s to holder", mTransaction.getTitle()));
        }

        @Override
        public void onClick(View v) {
            Intent i = TransactionDetailActivity.newIntent(AccountDetailActivity.this, mTransaction);
            startActivity(i);
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
            LayoutInflater layoutInflater = LayoutInflater.from(AccountDetailActivity.this);
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
