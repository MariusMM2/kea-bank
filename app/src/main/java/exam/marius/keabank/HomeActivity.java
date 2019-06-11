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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import exam.marius.keabank.database.MainDatabase;
import exam.marius.keabank.model.Account;
import exam.marius.keabank.model.Bill;
import exam.marius.keabank.model.Customer;
import exam.marius.keabank.model.Transaction;
import exam.marius.keabank.util.ViewUtils;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("Duplicates")
public class HomeActivity extends AppCompatActivity {
    static final int REQUEST_TRANSACTION = 0x2;
    private static final String TAG = "HomeActivity";

    private static final String EXTRA_CUSTOMER = "exam.marius.extra.EXTRA_CUSTOMER";
    private static final String EXTRA_TRANSACTION = "exam.marius.extra.EXTRA_TRANSACTION";

    private SwipeRefreshLayout mAccountsRefresh;
    private RecyclerView mAccountsListView;
    private AccountAdapter mAccountAdapter;

    private Customer mCustomer;

    static Intent newIntent(Context packageContext, Transaction transaction) {
        Intent intent = new Intent(packageContext, HomeActivity.class);
        intent.putExtra(EXTRA_TRANSACTION, (Parcelable) transaction);

        return intent;
    }

    static Intent newIntent(Context packageContext, Customer customer) {
        Intent intent = new Intent(packageContext, HomeActivity.class);
        intent.putExtra(EXTRA_CUSTOMER, (Parcelable) customer);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mCustomer = getIntent().getParcelableExtra(EXTRA_CUSTOMER);

        mAccountsRefresh = findViewById(R.id.refresh_accounts);
        mAccountsListView = findViewById(R.id.recycler_list);

        mAccountAdapter = new AccountAdapter(mCustomer);
        mAccountsListView.setAdapter(mAccountAdapter);
        mAccountAdapter.notifyDataSetChanged();
        mAccountsRefresh.setOnRefreshListener(this::dbRefresh);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);

        // Sets the user name in the ActionBar
        MenuItem menuItem = menu.findItem(R.id.action_customer_details);
        String displayName = String.format("%s %s", mCustomer.getFirstName(), mCustomer.getLastName());
        if (displayName.length() > getResources().getInteger(R.integer.display_name_max_length)) {
            displayName = mCustomer.getFirstName();
        }
        menuItem.setTitle(displayName);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_customer_details) {
            Intent intent = CustomerDetailActivity.newIntent(this, mCustomer);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_logout) {
            // Logs the user out and returns to the Login Activity
            returnToLogin();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void returnToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void startTransferActivity(View view) {
        Intent i = TransferActivity.newIntent(this, mCustomer);
        startActivityForResult(i, REQUEST_TRANSACTION);
    }

    public void startPaymentActivity(View view) {
        List<Bill> billList = MainDatabase.getInstance(this).getOpenBills(mCustomer);
        Intent i = PaymentActivity.newIntent(this, mCustomer, billList);
        startActivityForResult(i, REQUEST_TRANSACTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TRANSACTION) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Transaction transaction = data.getParcelableExtra(EXTRA_TRANSACTION);
                    MainDatabase.getInstance(this).addTransaction(transaction);
                }
                dbRefresh();
            }
        }
    }

    private void doDbRefresh() {
        mAccountsRefresh.setRefreshing(true);
        dbRefresh();
    }

    private void dbRefresh() {
        mCustomer = MainDatabase.getInstance(this).getCustomer(mCustomer.getId());
        mAccountAdapter.setCustomer(mCustomer);
        mAccountAdapter.notifyDataSetChanged();
        new Handler().postDelayed(() -> mAccountsRefresh.setRefreshing(false), 500);
    }

    public void newAccountDialog(View view) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose account to add");

        String[] accounts = Arrays.stream(Account.Type.values()).map(Account.Type::getText).toArray(String[]::new);

        builder.setItems(accounts, (dialog, which) -> {
            final Account account = new Account(0, Account.Type.values()[which], mCustomer.getId());
            MainDatabase.getInstance(HomeActivity.this).addAccount(account);
            doDbRefresh();
        });

        builder.show();
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
