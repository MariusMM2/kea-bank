package exam.marius.keabank;

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
import exam.marius.keabank.database.MainDatabase;
import exam.marius.keabank.model.*;
import exam.marius.keabank.util.StringWrapper;

import java.util.*;
import java.util.stream.Collectors;

public class TransferActivity extends UpNavActivity {
    private static final String TAG = "TransferActivity";

    static final int REQUEST_TRANSACTION = 1;

    private static final String EXTRA_CUSTOMER = "com.example.extras.EXTRA_CUSTOMER";

    private Customer mCustomer;
    private Transaction mNewTransaction;

    private DatePickerDialog mDueDateDialog;

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
        mDueDateField = findViewById(R.id.edit_transaction_date);
        mDueDateDialog = new DatePickerDialog(this);
        mDueDateField.setOnTouchListener((v, event) -> {
            mDueDateDialog.show();

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
        boolean validInput = true;

        // Source selection
        TransactionTarget source = null;
        try {
            source = mCustomer.getAccountList().get(mSourcesSpinner.getSelectedItemPosition());
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(this, "Error: Source Account not found", Toast.LENGTH_SHORT).show();
            Log.e(TAG, String.format("submitTransaction: %s", Log.getStackTraceString(e.getCause())));
            validInput = false;
        }

        // Destination selection
        TransactionTarget destination = null;
        if (mDestinationsSpinner.getSelectedItemPosition() == 0) {
            // User had "Enter Account ID selected"
            destination = MainDatabase.getInstance(this).findAccount(null);
        } else {
            try {
                destination = mCustomer.getAccountList().get(mDestinationsSpinner.getSelectedItemPosition() - 1);
            } catch (IndexOutOfBoundsException e) {
                Toast.makeText(this, "Error: Source Account not found", Toast.LENGTH_SHORT).show();
                Log.e(TAG, String.format("submitTransaction: %s", Log.getStackTraceString(e.getCause())));
                validInput = false;
            }
        }

        if (validInput && source.getId().equals(destination.getId())) {
            Toast.makeText(this, "Source and Destination account are the same", Toast.LENGTH_SHORT).show();
            return;
        }

        // Type selection
        Transaction.Type type = null;
        try {
            type = Transaction.Type.values()[mTypesSpinner.getSelectedItemPosition()];
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(this, "Error: Type selection failed", Toast.LENGTH_SHORT).show();
            Log.e(TAG, String.format("submitTransaction: %s", Log.getStackTraceString(e.getCause())));
            validInput = false;
        }

        // Date selection
        Date date = null;
        try {
            date = mDueDateDialog.getDate();
        } catch (NullPointerException e) {
            Toast.makeText(this, "Error: Date selection failed", Toast.LENGTH_SHORT).show();
            Log.e(TAG, String.format("submitTransaction: %s", Log.getStackTraceString(e.getCause())));
            validInput = false;
        }

        // Message selection
        String message = mMessageField.getText().toString();
        int messageMinLength = getResources().getInteger(R.integer.transaction_message_min_length);
        int messageMaxLength = getResources().getInteger(R.integer.transaction_message_max_length);
        if (message.length() < messageMinLength) {
            Toast.makeText(this, String.format("Message too short! Must have at least %d letters", messageMinLength), Toast.LENGTH_SHORT).show();
            validInput = false;
        } else if (message.length() > messageMaxLength) {
            Toast.makeText(this, String.format("Message too long! Must have at most %d letters", messageMaxLength), Toast.LENGTH_SHORT).show();
            validInput = false;
        }

        // Amount selection
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

        if (validInput) {
            try {
                mNewTransaction.setSource(source)
                        .setDestination(destination)
                        .setType(type)
                        .setDate(date)
                        .setMessage(message)
                        .setAmount(amount)
                        .setStatus(Transaction.Status.PENDING)
                        .commit();
            } catch (TransactionException e) {
                Toast.makeText(this, "Unexpected Error: " + e.getStackTrace()[0], Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Log.i(TAG, String.format("submitTransaction: %s", mNewTransaction.toString()));
    }

    class DatePickerDialog extends android.app.DatePickerDialog {
        private long mLastOpenTime;
        private Date mDate;

        DatePickerDialog(@NonNull Context context) {
            super(context);
            mLastOpenTime = 0;
            setCanceledOnTouchOutside(true);
            setOnDateSetListener((view, year, month, dayOfMonth) -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                mDate = calendar.getTime();
                mDueDateField.setText(StringWrapper.wrapDate(mDate, true));
            });

            mDate = new Date();
            mDueDateField.setText(StringWrapper.wrapDate(mDate, true));
        }

        Date getDate() {
            return new Date(mDate.getTime());
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
