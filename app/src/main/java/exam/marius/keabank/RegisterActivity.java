package exam.marius.keabank;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import exam.marius.keabank.database.MainDatabase;
import exam.marius.keabank.model.Customer;
import exam.marius.keabank.model.NemId;
import exam.marius.keabank.util.StringUtils;
import exam.marius.keabank.util.TimeUtils;
import exam.marius.keabank.util.ViewUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.function.BiConsumer;

public class RegisterActivity extends UpNavActivity {
    private static final String TAG = "RegisterActivity";

    private DatePickerDialog mBirthDateDialog;

    private TextInputEditText mFirstNameField;
    private TextInputEditText mLastNameField;
    private EditText mBirthDateField;
    private EditText mNemIdField;
    private TextView mNemIdInfoTextView;
    private ImageView mNemIdInfoImageView;

    private Customer mCustomer;
    private NemId mNemId;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFirstNameField = findViewById(R.id.edit_firstname);

        mLastNameField = findViewById(R.id.edit_lastname);

        mBirthDateField = findViewById(R.id.edit_birthdate);
        mBirthDateDialog = new DatePickerDialog();
        mBirthDateField.setOnTouchListener((v, event) -> {
            mBirthDateDialog.show();

            return true;
        });

        mNemIdField = findViewById(R.id.edit_nemid);
        mNemIdField.setOnTouchListener((v, event) -> true);

        mNemIdInfoTextView = findViewById(R.id.text_nemid_info);
        mNemIdInfoTextView.setVisibility(View.INVISIBLE);
        mNemIdInfoImageView = findViewById(R.id.image_nemid_info);
        mNemIdInfoImageView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NemIdActivity.REQUEST_NEMID) {
            if (resultCode == NemIdActivity.RESULT_NEW_NEMID) {
                mNemId = NemIdActivity.getNemId(data);
                showNewNemIdInfo();
            } else if (resultCode == NemIdActivity.RESULT_OK) {
                mNemId = NemIdActivity.getNemId(data);
                showExistingNemIdInfo();
            }
        }
    }

    private void showNewNemIdInfo() {
        mNemIdInfoTextView.setVisibility(View.VISIBLE);
        mNemIdInfoImageView.setVisibility(View.VISIBLE);

        mNemIdInfoTextView.setText(R.string.info_new_nemid);

        mNemIdField.setText(mNemId.getUsername());
    }

    private void showExistingNemIdInfo() {
        mNemIdInfoTextView.setVisibility(View.VISIBLE);
        mNemIdInfoImageView.setVisibility(View.VISIBLE);

        mNemIdInfoTextView.setText(R.string.info_existing_nemid_attachment);

        mNemIdField.setText(mNemId.getUsername());
    }

    public void startNemIdDialog(View view) {
        Intent intent = NemIdActivity.newIntent(this, null);
        startActivityForResult(intent, NemIdActivity.REQUEST_NEMID);
    }

    public void validateInput(View view) {
        // Input Validation
        final boolean[] validInput = {true};
        final View[] focusView = {null};

        final BiConsumer<TextView, String> errorMacro = ViewUtils.newViewInputError(validInput, focusView);

        // NemID selection
        String nemIdUsername = mNemIdField.getText().toString();

        if (nemIdUsername.isEmpty()) {
            errorMacro.accept(mNemIdField, getString(R.string.error_field_required));
        }

        // Last Name selection
        String lastName = mLastNameField.getText().toString();

        if (lastName.isEmpty()) {
            errorMacro.accept(mLastNameField, getString(R.string.error_field_required));
        } else {
            int messageMinLength = getResources().getInteger(R.integer.name_min_length);
            int messageMaxLength = getResources().getInteger(R.integer.name_max_length);
            if (lastName.length() <= messageMinLength) {
                errorMacro.accept(mLastNameField, getString(R.string.error_name_short, messageMinLength));
            } else if (lastName.length() > messageMaxLength) {
                errorMacro.accept(mLastNameField, getString(R.string.error_name_long, messageMaxLength));
            }
        }

        // First Name selection
        String firstName = mFirstNameField.getText().toString();

        if (firstName.isEmpty()) {
            errorMacro.accept(mFirstNameField, getString(R.string.error_field_required));
        } else {
            int messageMinLength = getResources().getInteger(R.integer.name_min_length);
            int messageMaxLength = getResources().getInteger(R.integer.name_max_length);
            if (firstName.length() <= messageMinLength) {
                errorMacro.accept(mFirstNameField, getString(R.string.error_name_short, messageMinLength));
            } else if (firstName.length() > messageMaxLength) {
                errorMacro.accept(mFirstNameField, getString(R.string.error_name_long, messageMaxLength));
            }
        }

        // Date selection
        Date date = mBirthDateDialog.getDate();
        // END Input Validation

        if (validInput[0]) {

            new AlertDialog.Builder(this)
                    .setTitle("Confirm Creation")
                    .setMessage(String.format("Are you sure you want to create this account:\n" +
                                    "Name: %s %s\n" +
                                    "Birth Date: %s\n" +
                                    "NemID Username: %s?",
                            firstName,
                            lastName,
                            StringUtils.wrapDate(date),
                            mNemId.getUsername()))
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        Customer customer = new Customer(firstName, lastName, date);

                        mNemId.setCustomerId(customer.getId());

                        MainDatabase.getInstance(this).addCustomer(customer);
                        MainDatabase.getInstance(this).addNemId(mNemId);

                        Intent i = HomeActivity.newIntent(this, customer);
                        startActivity(i);
                        finish();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();

        } else {
            focusView[0].requestFocus();
        }
    }

    class DatePickerDialog extends android.app.DatePickerDialog {
        private Date mDate;

        DatePickerDialog() {
            super(RegisterActivity.this);
            setCanceledOnTouchOutside(true);
            final long maxDate = TimeUtils.removeYears(new Date(System.currentTimeMillis()), 18).getTime();
            getDatePicker().setMaxDate(maxDate);
            setOnDateSetListener((view, year, month, dayOfMonth) -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                mDate = calendar.getTime();
                mBirthDateField.setText(StringUtils.wrapDate(mDate, false));
            });

            mDate = new Date(maxDate);
            mBirthDateField.setText(StringUtils.wrapDate(mDate, false));
        }

        Date getDate() {
            return new Date(mDate.getTime());
        }
    }
}
