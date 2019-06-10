package exam.marius.keabank;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import exam.marius.keabank.database.MainDatabase;
import exam.marius.keabank.model.Account;
import exam.marius.keabank.model.Customer;
import exam.marius.keabank.model.Transaction;
import exam.marius.keabank.util.StringUtils;
import exam.marius.keabank.util.TimeUtils;
import exam.marius.keabank.util.ViewUtils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class TransferActivity extends UpNavActivity {
    private static final String TAG = "TransferActivity";

    private static final String EXTRA_CUSTOMER = "exam.marius.extra.EXTRA_CUSTOMER";

    private Customer mCustomer;
    private Transaction mNewTransaction;

    private DatePickerDialog mDueDateDialog;

    private AppCompatSpinner mSourcesSpinner;
    private EditText mDestinationEditText;
    private AppCompatSpinner mDestinationsSpinner;
    private EditText mDueDateField;
    private EditText mMessageField;
    private EditText mAmountField;


    private SpinnerAdapter mSourcesAdapter;
    private SpinnerAdapter mDestinationsAdapter;
    private SpinnerAdapter mTypesAdapter;
    private int mChosenDestination;


    static Intent newIntent(Context packageContext, Customer customer) {
        Intent intent = new Intent(packageContext, TransferActivity.class);
        intent.putExtra(EXTRA_CUSTOMER, (Parcelable) customer);

        return intent;
    }

    @SuppressWarnings("Duplicates")
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

        mSourcesSpinner = findViewById(R.id.spinner_accounts);
        mSourcesSpinner.setAdapter(mSourcesAdapter);

        // Destination Accounts Field
        List<String> destinationEntries = new ArrayList<>();
        destinationEntries.add(getResources().getString(R.string.item_manual_account_id));
        destinationEntries.addAll(accountTitles);

        mDestinationsAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, destinationEntries);

        mDestinationEditText = findViewById(R.id.edit_destination);
        mDestinationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int selectedItemPosition = mDestinationsSpinner.getSelectedItemPosition();
                if (selectedItemPosition != 0) {
                    String sString = s.toString();
                    String itemString = mCustomer.getAccountList().get(selectedItemPosition - 1).getNumber();
                    if (!sString.equals(itemString)) {
                        mChosenDestination = 0;
                        mDestinationsSpinner.setSelection(0);
                    }
                }
            }
        });

        mDestinationsSpinner = findViewById(R.id.spinner_destinations);
        mDestinationsSpinner.setAdapter(mDestinationsAdapter);
        mDestinationsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    if (mChosenDestination != 0) {
                        mDestinationEditText.setText("");
                        mChosenDestination = -1;
                    }
                } else {
                    mDestinationEditText.setText(mCustomer.getAccountList().get(position - 1).getNumber());
                    mChosenDestination = position;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TransactionDetailActivity.REQUEST_CONFIRM_TRANSACTION) {
            if (resultCode == RESULT_OK) {
                Intent i = HomeActivity.newIntent(this, mNewTransaction);

                setResult(RESULT_OK, i);

                finish();

                Log.d(TAG, String.format("onActivityResult: %s", mNewTransaction.toString()));
            }
        }
    }

    private void showCustomerErrorDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Error retrieving customer details")
                .setPositiveButton(android.R.string.ok, null)
                .setOnDismissListener(dialog -> finish())
                .create().show();
    }

    public void submitTransfer(View view) {
        // Input Validation
        final boolean[] validInput = {true};
        final View[] focusView = {null};

        final BiConsumer<TextView, String> errorMacro = ViewUtils.newViewInputError(validInput, focusView);

        // Amount selection
        float amount = -1;
        try {
            if (mAmountField.getText().toString().isEmpty()) {
                errorMacro.accept(mAmountField, getString(R.string.error_field_required));
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
            errorMacro.accept(mMessageField, getString(R.string.error_field_required));
        } else {
            int messageMinLength = getResources().getInteger(R.integer.transaction_message_min_length);
            int messageMaxLength = getResources().getInteger(R.integer.transaction_message_max_length);
            if (message.length() < messageMinLength) {
                errorMacro.accept(mMessageField, getString(R.string.transaction_error_message_short, messageMinLength));
            } else if (message.length() > messageMaxLength) {
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
            Log.e(TAG, String.format("submitTransfer: %s", Log.getStackTraceString(e.getCause())));
        }

        // Type selection
        Transaction.Type type = Transaction.Type.NORMAL;

        // Destination selection
        Account destination = null;
        if (mDestinationsSpinner.getSelectedItemPosition() == 0) {
            // User had "Enter Account ID" selected
            String destinationNumber = mDestinationEditText.getText().toString();
            if (destinationNumber.isEmpty()) {
                errorMacro.accept(mDestinationEditText, getString(R.string.error_field_required));
            } else {
                destination = MainDatabase.getInstance(this).getAccount(destinationNumber);

                if (destination == null) {
                    errorMacro.accept(mDestinationEditText, getString(R.string.transaction_error_destination_invalid));
                }
            }
        } else {
            try {
                destination = mCustomer.getAccountList().get(mDestinationsSpinner.getSelectedItemPosition() - 1);
            } catch (IndexOutOfBoundsException | NoSuchElementException e) {
                validInput[0] = false;
                Toast.makeText(this, getString(R.string.transaction_error_destination_internal), Toast.LENGTH_SHORT).show();
                Log.e(TAG, String.format("submitTransfer: %s", Log.getStackTraceString(e.getCause())));
            }
        }

        // Source selection
        Account source = null;
        try {
            source = mCustomer.getAccountList().get(mSourcesSpinner.getSelectedItemPosition());
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(this, getString(R.string.transaction_error_source), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Customer source account exception: ", e);
            validInput[0] = false;
        }

        if (source != null && destination != null) {
            if (source.getId().equals(destination.getId())) {
                errorMacro.accept(mDestinationEditText, getString(R.string.transaction_error_same_target));
            }
        }
        // END Input Validation

        if (validInput[0]) {

            mNewTransaction.setSource(source)
                    .setDestination(destination)
                    .setType(type)
                    .setDate(date)
                    .setMessage(message)
                    .setAmount(amount)
                    .setStatus(Transaction.Status.IDLE);

            Intent i = TransactionDetailActivity.newIntent(this, mNewTransaction);
            startActivityForResult(i, TransactionDetailActivity.REQUEST_CONFIRM_TRANSACTION);
            return;
        } else {
            focusView[0].requestFocus();
        }

        Log.d(TAG, String.format("submitTransfer: %s", mNewTransaction.toString()));
    }

    public void debugFillFields(View view) {
        mDestinationsSpinner.setSelection(2);
        mMessageField.setText("This is a generated message");
        mAmountField.setText("1234");
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
                mDueDateField.setText(StringUtils.wrapDate(mDate, true));
            });

            mDate = TimeUtils.getToday();
            mDueDateField.setText(StringUtils.wrapDate(mDate, true));
        }

        Date getDate() {
            return new Date(mDate.getTime());
        }
    }
}
