package com.example.keabank;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.example.keabank.model.Account;
import com.example.keabank.model.Customer;
import com.example.keabank.model.Transaction;
import com.example.keabank.util.StringWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class TransferActivity extends UpNavActivity {
    private static final String TAG = "TransferActivity";

    static final int REQUEST_TRANSACTION = 1;

    private static final String EXTRA_CUSTOMER = "com.example.extras.EXTRA_CUSTOMER";

    private Customer mCustomer;
    private Transaction mNewTransaction;

    private DatePickerDialog mTransactionDateDialog;

    private AppCompatSpinner mSourcesSpinner;
    private EditText mDestinationEditText;
    private AppCompatSpinner mDestinationsSpinner;
    private AppCompatSpinner mTypesSpinner;
    private EditText mDueDateField;
    private EditText mMessageField;
    private EditText mAmountField;


    private SpinnerAdapter mSourcesAdapter;
    private SpinnerAdapter mDestinationsAdapter;
    private SpinnerAdapter mTypesAdapter;


    static Intent newIntent(Context packageContext, Customer customer) {
        Intent intent = new Intent(packageContext, TransferActivity.class);
        intent.putExtra(EXTRA_CUSTOMER, (Parcelable) customer);

        return intent;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        mCustomer = getIntent().getParcelableExtra(EXTRA_CUSTOMER);

        if (mCustomer == null) {
            showCustomerErrorDialog();
            return;
        }

        mNewTransaction = Transaction.beginTransaction();

        // Source Accounts Field
        List<String> accountTitles = mCustomer.getAccountList().stream().map(Account::getTitle).collect(Collectors.toList());
        mSourcesAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, accountTitles);

        mSourcesSpinner = findViewById(R.id.spinner_source_accounts);
        mSourcesSpinner.setAdapter(mSourcesAdapter);
        mSourcesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mNewTransaction.setSource(mCustomer.getAccountList().get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Destination Accounts Field
        List<String> sourceEntries = new ArrayList<>();
        sourceEntries.add(getResources().getString(R.string.manual_account_item));
        sourceEntries.addAll(accountTitles);

        mDestinationsAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, sourceEntries);

        mDestinationEditText = findViewById(R.id.edit_destination);

        mDestinationsSpinner = findViewById(R.id.spinner_destinations);
        mDestinationsSpinner.setAdapter(mDestinationsAdapter);
        mDestinationsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) mDestinationEditText.setText("");
                else {
                    mDestinationEditText.setText(String.format("%x",
                            mCustomer.getAccountList().get(position - 1).getId().getMostSignificantBits()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mDestinationsSpinner.setSelection(0);
            }
        });

        // Transaction Types Field
        List<String> types = Arrays.stream(Transaction.Type.values()).map(Transaction.Type::getText).collect(Collectors.toList());
        mTypesAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, types);

        mTypesSpinner = findViewById(R.id.spinner_transaction_type);
        mTypesSpinner.setAdapter(mTypesAdapter);
        mTypesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mNewTransaction.setType(Transaction.Type.values()[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mDestinationsSpinner.setSelection(0);
            }
        });

        // Due Date Field
        mTransactionDateDialog = new DatePickerDialog(this);

        mDueDateField = findViewById(R.id.edit_transaction_date);
        mDueDateField.setOnTouchListener((v, event) -> {
            mTransactionDateDialog.show();

            return true;
        });

        // Message Field
        mMessageField = findViewById(R.id.edit_message);

        // Amount Field
        mAmountField = findViewById(R.id.edit_amount);
    }

    private void showCustomerErrorDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Error retrieving customer details")
                .setPositiveButton(android.R.string.ok, null)
                .setOnDismissListener(dialog -> finish())
                .create().show();
    }

    public void submitTransaction(View view) {
        mNewTransaction.setMessage(mMessageField.getText().toString());
        float amount;
        try {
            amount = Float.parseFloat(mAmountField.getText().toString());
        } catch (IllegalStateException ei) {
            Toast.makeText(this, "Amount cannot have a negative value", Toast.LENGTH_SHORT).show();
            return;
        } catch (NumberFormatException en) {
            Toast.makeText(this, "Amount is invalid", Toast.LENGTH_SHORT).show();
            return;
        }

        mNewTransaction.setAmount(amount);
        mNewTransaction.setStatus(Transaction.Status.PENDING);

        Log.i(TAG, String.format("submitTransaction: %s", mNewTransaction.toString()));
    }

    class DatePickerDialog extends android.app.DatePickerDialog {
        private long mLastOpenTime;

        DatePickerDialog(@NonNull Context context) {
            super(context);
            mLastOpenTime = 0;
            setCanceledOnTouchOutside(true);
            setOnDateSetListener((view, year, month, dayOfMonth) -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                mNewTransaction.setDate(calendar.getTime());
                mDueDateField.setText(StringWrapper.wrapDate(mNewTransaction.getDate()));
            });
        }

        @Override
        public void show() {
            if (SystemClock.elapsedRealtime() - mLastOpenTime >= 500) {
                mLastOpenTime = SystemClock.elapsedRealtime();

                getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

                super.show();
            }
        }
    }
}
