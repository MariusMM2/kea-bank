package com.example.keabank;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;
import com.example.keabank.model.Transaction;

public class TransactionDetailActivity extends AppCompatActivity {
    private static final String TAG = "TransactionDetailActivi";

    private TextView mTypeTextView;
    private TextView mStatusTextView;
    private TextView mAccountTextView;
    private TextView mTitleTextView;
    private TextView mAmountTextView;
    private TextView mDateTextView;
    private TextView mMessageTextView;
    private TextView mIdTextView;

    private Transaction mTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        mTransaction = Customer.getDummyCustomer().getAccountList().get(0).getTransactionList().get(0);

        mTypeTextView = findViewById(R.id.text_type);
        mStatusTextView = findViewById(R.id.text_status);
        mAccountTextView = findViewById(R.id.text_account);
        mTitleTextView = findViewById(R.id.text_title);
        mAmountTextView = findViewById(R.id.text_amount);
        mDateTextView = findViewById(R.id.text_date);
        mMessageTextView = findViewById(R.id.text_message);
        mIdTextView = findViewById(R.id.text_id);

        mTypeTextView.setText(mTransaction.getType().getText());
        mStatusTextView.setText(String.valueOf(mTransaction.getStatus().getText()));
        mAccountTextView.setText(mTransaction.getSourceDetails());
        mTitleTextView.setText(mTransaction.getTitle());
        mAmountTextView.setText(String.valueOf(-mTransaction.getAmount()));
        mDateTextView.setText(DateFormat.format(
                getResources().getString(R.string.date_format),
                mTransaction.getDate()));

        if (mTransaction.getMessage().isEmpty()) {
            mLabelMessageTextView.setVisibility(View.GONE);
            mMessageTextView.setVisibility(View.GONE);
        }

        mMessageTextView.setText(mTransaction.getMessage());
        mIdTextView.setText(mTransaction.getId().toString());
    }
}
