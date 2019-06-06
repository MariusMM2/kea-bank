package com.example.keabank;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import com.example.keabank.model.Account;
import com.example.keabank.model.Customer;
import com.example.keabank.model.Transaction;

public class TransferActivity extends UpNavActivity {
    private static final String TAG = "TransferActivity";

    static final int REQUEST_TRANSACTION = 1;

    private static final String EXTRA_CUSTOMER = "com.example.extras.EXTRA_CUSTOMER";

    private Customer mCustomer;
    private Transaction newTransaction;


    static Intent newIntent(Context packageContext, Customer customer) {
        Intent intent = new Intent(packageContext, TransferActivity.class);
        intent.putExtra(EXTRA_CUSTOMER, (Parcelable) customer);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        mCustomer = getIntent().getParcelableExtra(EXTRA_CUSTOMER);

//        List<String> accountNames = mCustomer.getAccountList().stream().map(Account::getTitle).collect(Collectors.toList());

        SpinnerAdapter adapter = new ArrayAdapter<Account>(this, android.R.layout.simple_spinner_item, mCustomer.getAccountList()) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView label = (TextView) super.getView(position, convertView, parent);
                label.setText(getItem(position).getTitle());

                return label;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView label = (TextView) super.getView(position, convertView, parent);
                label.setText(getItem(position).getTitle());

                return label;
            }
        };

        AppCompatSpinner spinner = findViewById(R.id.categorySpinner);
        spinner.setAdapter(adapter);
    }
}
