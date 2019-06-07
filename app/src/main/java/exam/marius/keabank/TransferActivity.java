package exam.marius.keabank;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
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
import java.util.function.BiConsumer;
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
                    mDestinationEditText.setText(mCustomer.getAccountList().get(position - 1).getIdNumber());
                }
                mDestinationEditText.setError(null);
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
        // Input Validation
        final boolean[] validInput = {true};
        final View[] focusView = {null};

        final BiConsumer<TextView, String> errorMacro = (targetView, targetError) -> {
            validInput[0] = false;
            if (targetView != null && targetError != null) {
                targetView.setError(targetError);
                focusView[0] = targetView;
            }
        };

        // Amount selection
        float amount = -1;
        try {
            if (mAmountField.getText().toString().isEmpty()) {
                errorMacro.accept(mAmountField, getString(R.string.transaction_error_empty_field, mAmountField.getHint()));
            } else {
                amount = Float.parseFloat(mAmountField.getText().toString());
                if (amount == 0) {
                    errorMacro.accept(mAmountField, getString(R.string.transaction_error_amount_invalid));
                }
            }
        } catch (IllegalStateException ei) {
            errorMacro.accept(mAmountField, getString(R.string.transaction_error_amount_negative));
        } catch (NumberFormatException en) {
            errorMacro.accept(mAmountField, getString(R.string.transaction_error_amount_invalid));
        }

        // Message selection
        String message = mMessageField.getText().toString();

        if (message.isEmpty()) {
//            validInput[0] = false;
//            mMessageField.setError(getString(R.string.transaction_error_empty_field, mMessageField.getHint()));
//            focusView[0] = mMessageField;
            errorMacro.accept(mMessageField, getString(R.string.transaction_error_empty_field, mMessageField.getHint()));
        } else {
            int messageMinLength = getResources().getInteger(R.integer.transaction_message_min_length);
            int messageMaxLength = getResources().getInteger(R.integer.transaction_message_max_length);
            if (message.length() < messageMinLength) {
//            validInput[0] = false;
//            mMessageField.setError(getString(R.string.transaction_error_message_short, messageMinLength));
//            focusView[0] = mMessageField;
                errorMacro.accept(mMessageField, getString(R.string.transaction_error_message_short, messageMinLength));
            } else if (message.length() > messageMaxLength) {
//            validInput[0] = false;
//            mMessageField.setError(getString(R.string.transaction_error_message_long, messageMaxLength));
//            focusView[0] = mMessageField;
                errorMacro.accept(mMessageField, getString(R.string.transaction_error_message_long, messageMaxLength));
            }
        }

        // Date selection
        Date date = null;
        try {
            date = mDueDateDialog.getDate();
        } catch (NullPointerException e) {
            validInput[0] = false;
            Toast.makeText(this, getString(R.string.transaction_error_date), Toast.LENGTH_SHORT).show();
            Log.e(TAG, String.format("submitTransaction: %s", Log.getStackTraceString(e.getCause())));
        }

        // Type selection
        Transaction.Type type = null;
        try {
            type = Transaction.Type.values()[mTypesSpinner.getSelectedItemPosition()];
        } catch (IndexOutOfBoundsException e) {
            validInput[0] = false;
            Toast.makeText(this, getString(R.string.transaction_error_type), Toast.LENGTH_SHORT).show();
            Log.e(TAG, String.format("submitTransaction: %s", Log.getStackTraceString(e.getCause())));
        }

        // Destination selection
        TransactionTarget destination = null;
        if (mDestinationsSpinner.getSelectedItemPosition() == 0) {
            // User had "Enter Account ID" selected
            if (mDestinationEditText.getText().toString().isEmpty()) {
                errorMacro.accept(mDestinationEditText, getString(R.string.transaction_error_empty_field, ((TextView) findViewById(R.id.text_destination_label)).getText()));
            } else {
                destination = MainDatabase.getInstance(this).findAccount(null);

                if (destination == null) {
//                validInput[0] = false;
//                mDestinationEditText.setError(getString(R.string.transaction_error_destination_invalid));
//                focusView[0] = mDestinationEditText;
                    errorMacro.accept(mDestinationEditText, getString(R.string.transaction_error_destination_invalid));
                }
            }
        } else {
            try {
                destination = mCustomer.getAccountList().get(mDestinationsSpinner.getSelectedItemPosition() - 1);
            } catch (IndexOutOfBoundsException | NoSuchElementException e) {
                validInput[0] = false;
                Toast.makeText(this, getString(R.string.transaction_error_destination_internal), Toast.LENGTH_SHORT).show();
                Log.e(TAG, String.format("submitTransaction: %s", Log.getStackTraceString(e.getCause())));
            }
        }

        // Source selection
        TransactionTarget source = null;
        try {
            source = mCustomer.getAccountList().get(mSourcesSpinner.getSelectedItemPosition());
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(this, getString(R.string.transaction_error_source), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Customer source account exception:", e);
            validInput[0] = false;
        }

        if (source != null && destination != null) {
            if (source.getId().equals(destination.getId())) {
//                mDestinationEditText.setError(getString(R.string.transaction_error_same_target));
//                validInput[0] = false;
//                focusView[0] = mDestinationEditText;
                errorMacro.accept(mDestinationEditText, getString(R.string.transaction_error_same_target));
            }
        }
        // END Input Validation

        if (validInput[0]) {
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
                Toast.makeText(this, getString(R.string.transaction_error_unexpected, e.getStackTrace()[0]), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "submitTransaction: ", e);
                return;
            }
        } else {
            focusView[0].requestFocus();
        }

        Log.d(TAG, String.format("submitTransaction: %s", mNewTransaction.toString()));
    }

    class DatePickerDialog extends android.app.DatePickerDialog {
        private Date mDate;

        DatePickerDialog(@NonNull Context context) {
            super(context);
            setCanceledOnTouchOutside(true);
            getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
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
    }
}
