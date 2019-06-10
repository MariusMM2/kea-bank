package exam.marius.keabank;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.TextView;
import exam.marius.keabank.model.Transaction;
import exam.marius.keabank.util.StringUtils;

public class TransactionDetailActivity extends UpNavActivity {
    private static final String TAG = "TransactionDetailActivi";

    static final int REQUEST_CONFIRM_TRANSACTION = 0x4;

    private static final String EXTRA_TRANSACTION = "exam.marius.extra.EXTRA_TRANSACTION";

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
        intent.putExtra(EXTRA_TRANSACTION, (Parcelable) transaction);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        if (getCallingActivity() != null) {
            // Activity was called for confirmation of a transaction,
            // so show "Confirm" button
            ConstraintLayout sendTransactionButton = findViewById(R.id.btn_send_transaction);
            sendTransactionButton.setVisibility(View.VISIBLE);
        }

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
        mAccountTextView.setText(mTransaction.getSource().getTitle());
        mTitleTextView.setText(mTransaction.getTitle());
        mAmountTextView.setText(String.valueOf(-mTransaction.getAmount()));
        mDateTextView.setText(StringUtils.wrapDate(mTransaction.getDate()));

        if (mTransaction.getMessage().isEmpty()) {
            mLabelMessageTextView.setVisibility(View.GONE);
            mMessageTextView.setVisibility(View.GONE);
        }

        mMessageTextView.setText(mTransaction.getMessage());
        mIdTextView.setText(mTransaction.getId().toString());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getCallingActivity() != null) {
            // User pressed the Up button when confirming a transaction,
            // Cancel transaction
            setResult(RESULT_CANCELED);
        }
    }

    public void submitTransaction(View view) {
        setResult(RESULT_OK);
        finish();
    }
}
