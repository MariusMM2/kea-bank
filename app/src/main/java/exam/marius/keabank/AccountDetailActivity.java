package exam.marius.keabank;

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
import exam.marius.keabank.model.Account;
import exam.marius.keabank.model.Transaction;
import exam.marius.keabank.util.StringUtils;
import exam.marius.keabank.util.ViewUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AccountDetailActivity extends UpNavActivity {
    private static final String TAG = "AccountDetailActivity";

    private static final String EXTRA_ACCOUNT = "exam.marius.extra.EXTRA_ACCOUNT";

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
        ViewUtils.bindAccount(mAccount, mAccountView);

        mTransactionsListView = findViewById(R.id.recycler_list);

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

        void bind(Transaction transaction, float accountBalance, List<Transaction> transactions) {
            mTransaction = transaction;

            mDescriptionTextView.setText(String.valueOf(mTransaction.getTitle()));

            float amount = -mTransaction.getAmount();
            mAmountTextView.setText(getResources().getString(R.string.amount, amount));
            mAmountTextView.setTextColor(getResources().getColor(amount > 0 ? R.color.green : R.color.red, getTheme()));

            mDateTextView.setText(StringUtils.wrapDate(mTransaction.getDate()));

            if (mTransaction.isDone()) {

                float accumulatedTransactionsAmount = transactions.subList(0, getAdapterPosition())
                        .stream()
                        .filter(Transaction::isDone)
                        .reduce(0f,
                                (totalAmount, transaction2) -> totalAmount + transaction2.getAmount(),
                                Float::sum);

                float balanceAfter = accountBalance + accumulatedTransactionsAmount;
                mBalanceAfterTextView.setText(getResources().getString(R.string.amount, balanceAfter));
            } else {
                mBalanceAfterTextView.setText(String.valueOf(mTransaction.getStatus()));
            }
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
        private List<Transaction> mTransactions;

        private TransactionAdapter(Account account) {
            mAccount = account;
            mTransactions = new ArrayList<>();
            mTransactions.addAll(mAccount.getTransactionList().stream().filter(Transaction::isClose).collect(Collectors.toList()));
            mTransactions.sort(Comparator.comparing(Transaction::getDate));
            Collections.reverse(mTransactions);
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
            Transaction transaction = mTransactions.get(i);
            transactionHolder.bind(transaction, mAccount.getAmount(), mTransactions);
        }

        @Override
        public int getItemCount() {
            return mTransactions.size();
        }
    }
}
