package exam.marius.keabank;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import exam.marius.keabank.database.MainDatabase;
import exam.marius.keabank.model.*;
import exam.marius.keabank.util.ViewUtils;

import java.util.List;

@SuppressWarnings("Duplicates")
public class HomeActivity extends AppCompatActivity {
    static final int REQUEST_TRANSACTION = 0x2;
    private static final String TAG = "HomeActivity";

    private static final String EXTRA_CUSTOMER = "exam.marius.extras.EXTRA_CUSTOMER";
    private static final String EXTRA_TRANSACTION = "exam.marius.extras.EXTRA_TRANSACTION";

    private SwipeRefreshLayout mAccountsRefresh;
    private RecyclerView mAccountsList;
    private AccountAdapter mAccountAdapter;

    private Customer mCustomer;

    static Intent newIntent(Context packageContext, Customer customer, Transaction transaction) {
        Intent intent = new Intent(packageContext, HomeActivity.class);
        intent.putExtra(EXTRA_CUSTOMER, (Parcelable) customer);
        intent.putExtra(EXTRA_TRANSACTION, (Parcelable) transaction);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent intent = getIntent();
        Customer intentCustomer = intent.getParcelableExtra(EXTRA_CUSTOMER);
        if (intentCustomer != null) {
            mCustomer = intentCustomer;

            Transaction transaction = intent.getParcelableExtra(EXTRA_TRANSACTION);

            if (transaction != null) {
                TransactionTarget source = transaction.getSource();
                if (source instanceof Account) {
                    int sourceIndex = mCustomer.getAccountList().indexOf(source);
                    if (sourceIndex != -1) {
                        mCustomer.getAccountList().get(sourceIndex).setAmount(source.getAmount());
                        mCustomer.getAccountList().get(sourceIndex).addTransaction(transaction);
                    }
                }

                TransactionTarget destination = transaction.getDestination();
                if (destination instanceof Account) {
                    int destinationIndex = mCustomer.getAccountList().indexOf(destination);
                    if (destinationIndex != -1) {
                        mCustomer.getAccountList().get(destinationIndex).setAmount(destination.getAmount());
                        mCustomer.getAccountList().get(destinationIndex).addTransaction(transaction.reverse());
                    }
                }
            }
            Log.i(TAG, "onCreate: Customer instance found: " + mCustomer.toString());
        } else {
            mCustomer = MainDatabase.getInstance(this).getDummyCustomer();
            Log.i(TAG, "onCreate: no Customer instance found, retrieved from database: " + mCustomer.toString());
        }

        mAccountsRefresh = findViewById(R.id.refresh_accounts);
        mAccountsList = findViewById(R.id.list_accounts);

        mAccountAdapter = new AccountAdapter(mCustomer);
        mAccountsList.setAdapter(mAccountAdapter);
        mAccountAdapter.notifyDataSetChanged();
        mAccountsRefresh.setOnRefreshListener(() -> {
            mCustomer = MainDatabase.getInstance(HomeActivity.this).getDummyCustomer();
            mAccountAdapter.setCustomer(mCustomer);
            mAccountAdapter.notifyDataSetChanged();
            new Handler().postDelayed(() -> mAccountsRefresh.setRefreshing(false), 500);
        });
    }

    public void startTransferActivity(View view) {
        Intent i = TransferActivity.newIntent(this, mCustomer);
        startActivityForResult(i, REQUEST_TRANSACTION);
    }

    public void startPaymentActivity(View view) {
        List<Bill> billList = MainDatabase.getInstance(this).getBills(mCustomer);
        Intent i = PaymentActivity.newIntent(this, mCustomer, billList);
        startActivityForResult(i, REQUEST_TRANSACTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TRANSACTION) {
            if (resultCode == RESULT_OK) {
                setIntent(data);
                recreate();

                if (data != null) {
                    Transaction transaction = data.getParcelableExtra(EXTRA_TRANSACTION);
                    MainDatabase.getInstance(this).addTransaction(transaction);
                }
            }
        }
    }

    /**
     * ViewHolder for an Account.
     * Holds the type, the id and the amount.
     */
    private class AccountHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Account mAccount;

        private TextView mTypeTextView;
        private TextView mAmountTextView;
        private TextView mIdTextView;

        AccountHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_account, parent, false));
            itemView.setOnClickListener(this);
            mTypeTextView = itemView.findViewById(R.id.text_destination);
            mIdTextView = itemView.findViewById(R.id.text_id);
            mAmountTextView = itemView.findViewById(R.id.text_amount);
            Log.d(TAG, "Created AccountHolder");
        }

        private void bind(Account account) {
            mAccount = account;

            ViewUtils.bindAccount(mAccount, mTypeTextView, mAmountTextView, mIdTextView);

            Log.d(TAG, String.format("Bound account %s to holder", mAccount.getType().getText()));
        }

        @Override
        public void onClick(View v) {
            Intent i = AccountDetailActivity.newIntent(HomeActivity.this, mAccount);

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(HomeActivity.this,
                    itemView.findViewById(R.id.item_account), "item");

            startActivity(i, options.toBundle());
        }
    }

    private class AccountAdapter extends RecyclerView.Adapter<AccountHolder> {

        private Customer mCustomer;

        private AccountAdapter(Customer customer) {
            mCustomer = customer;
        }

        @NonNull
        @Override
        public AccountHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(HomeActivity.this);
            return new AccountHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull AccountHolder accountHolder, int i) {

        }

        @Override
        public void onBindViewHolder(@NonNull AccountHolder accountHolder, int i, @NonNull List<Object> payloads) {
            Account account = mCustomer.getAccountList().get(i);
            accountHolder.bind(account);
        }

        @Override
        public int getItemCount() {
            return mCustomer.getAccountList().size();
        }

        void setCustomer(Customer customer) {
            mCustomer = customer;
        }
    }

}
