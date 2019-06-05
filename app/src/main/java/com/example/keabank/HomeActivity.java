package com.example.keabank;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.keabank.database.MainDatabase;
import com.example.keabank.model.Account;
import com.example.keabank.model.Customer;
import com.example.keabank.util.ModelBinding;

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
            mTypeTextView = itemView.findViewById(R.id.text_type);
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
            startActivity(i);
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
