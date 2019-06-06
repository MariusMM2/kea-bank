package com.example.keabank;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.example.keabank.model.Transaction;
import com.example.keabank.util.StringWrapper;

public class TransactionDetailActivity extends UpNavActivity {
    private static final String TAG = "TransactionDetailActivi";
    private static final String EXTRA_TRANSACTION = "com.example.extras.EXTRA_TRANSACTION";

    private TextView mLabelMessageTextView;

    private TextView mTypeTextView;
    private TextView mStatusTextView;
    private TextView mAccountTextView;
    private TextView mTitleTextView;
    private TextView mAmountTextView;
    private TextView mDateTextView;
    private TextView mMessageTextView;
    private TextView mIdTextView;

    private Transaction mTransaction;

    static Intent newIntent(Context packageContext, Transaction transaction) {
        Intent intent = new Intent(packageContext, TransactionDetailActivity.class);
        intent.putExtra(EXTRA_TRANSACTION, transaction);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        mTransaction = getIntent().getParcelableExtra(EXTRA_TRANSACTION);

        mLabelMessageTextView = findViewById(R.id.text_label_message);

        mTypeTextView = findViewById(R.id.text_destination);
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
        mDateTextView.setText(StringWrapper.wrapDate(mTransaction.getDate()));

        if (mTransaction.getMessage().isEmpty()) {
            mLabelMessageTextView.setVisibility(View.GONE);
            mMessageTextView.setVisibility(View.GONE);
        }

        mMessageTextView.setText(mTransaction.getMessage());
        mIdTextView.setText(mTransaction.getId().toString());
    }

    public void openDateDialog(View view) {
    }
}
