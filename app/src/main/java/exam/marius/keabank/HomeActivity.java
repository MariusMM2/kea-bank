package exam.marius.keabank;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import exam.marius.keabank.database.MainDatabase;
import exam.marius.keabank.model.Account;
import exam.marius.keabank.model.Customer;
import exam.marius.keabank.util.ModelBinding;

import java.util.List;

@SuppressWarnings("Duplicates")
public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";

    private RecyclerView mAccountsList;
    private AccountAdapter mAccountAdapter;

    private Customer mCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mCustomer = MainDatabase.getDummyCustomer();

        mAccountsList = findViewById(R.id.list_accounts);

        mAccountAdapter = new AccountAdapter(mCustomer);
        mAccountsList.setAdapter(mAccountAdapter);
        mAccountAdapter.notifyDataSetChanged();
    }

    public void startTransferActivity(View view) {
        Intent i = TransferActivity.newIntent(this, mCustomer);
        startActivityForResult(i, TransferActivity.REQUEST_TRANSACTION);
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

            ModelBinding.bindAccount(mAccount, mTypeTextView, mAmountTextView, mIdTextView);

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
    }

}
