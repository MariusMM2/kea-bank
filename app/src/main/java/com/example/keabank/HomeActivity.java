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
import com.example.keabank.model.Account;
import com.example.keabank.model.Bill;
import com.example.keabank.model.Customer;
import com.example.keabank.model.Transaction;

import java.util.*;
import java.util.stream.Collectors;

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
        mCustomer = getDebugCustomer();

        mAccountsList = findViewById(R.id.list_accounts);

        mAccountAdapter = new AccountAdapter();
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
        private TextView mIdTextView;
        private TextView mAmountTextView;

        AccountHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_account, parent, false));
            mTypeTextView = itemView.findViewById(R.id.text_type);
            mIdTextView = itemView.findViewById(R.id.text_id);
            mAmountTextView = itemView.findViewById(R.id.text_amount);
            Log.d(TAG, "Created AccountHolder");
        }

        private void bind(Account account) {
            mAccount = account;
            mTypeTextView.setText(String.valueOf(account.getType()));
            mIdTextView.setText(account.getId().toString());
            float amount = account.getAmount();
            mAmountTextView.setText(getResources().getString(R.string.amount, amount));
            Log.d(TAG, String.format("Bound account %s to holder", mAccount.getType()));
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(HomeActivity.this, AccountActivity.class);
            startActivity(i);
        }
    }

    private class AccountAdapter extends RecyclerView.Adapter<AccountHolder> {

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

    public static Customer getDebugCustomer() {
        Customer customer = new Customer("John", "Doe", "johndoe@email.com", "123456", Calendar.getInstance().getTime());
        final List<Account> accountList = new ArrayList<>(
                Arrays.asList(
                        Account.newDefault(3000, customer.getId()),
                        Account.newBudget(1000, customer.getId())
                )
        );
        final List<Bill> billList = new ArrayList<>(
                Arrays.asList(
                        new Bill("1", "1++", false, 10, Calendar.getInstance().getTime(), customer.getId()),
                        new Bill("2", "2++", false, 20, Calendar.getInstance().getTime(), customer.getId()),
                        new Bill("3", "3++", false, 30, Calendar.getInstance().getTime(), customer.getId())
                )
        );
        final List<Transaction> transactionList = new ArrayList<>(
                Arrays.asList(
                        Transaction.beginTransaction().setSource(accountList.get(0)).setDestination(accountList.get(1)).setAmount(1000),
                        Transaction.beginTransaction().setSource(accountList.get(1)).setDestination(accountList.get(0)).setAmount(2000),
                        Transaction.beginTransaction().setSource(accountList.get(0)).setDestination(billList.get(0)).setAmount(billList.get(0).getAmount()),
                        Transaction.beginTransaction().setSource(accountList.get(1)).setDestination(billList.get(2)).setAmount(billList.get(2).getAmount()),
                        Transaction.beginTransaction().setSource(billList.get(1)).setDestination(accountList.get(0)).setAmount(billList.get(1).getAmount())
                )
        );

        accountList.forEach(account -> {
            List<Transaction> outGoingTransactions = transactionList.stream()
                    .filter(transaction -> transaction.getSource().getId().equals(account.getId()))
                    .collect(Collectors.toList());

            List<Transaction> incomingTransactions = transactionList.stream()
                    .filter(transaction -> transaction.getDestination().getId().equals(account.getId()))
                    .map(Transaction::reverse)
                    .collect(Collectors.toList());

            List<Transaction> allTransactions = new ArrayList<>();

            allTransactions.addAll(outGoingTransactions);
            allTransactions.addAll(incomingTransactions);
            allTransactions.sort(Comparator.comparing(Transaction::getDate));
            allTransactions.forEach(account::addTransaction);

            customer.addAccount(account);
        });

        return customer;
    }
}
