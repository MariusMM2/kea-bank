package exam.marius.keabank;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TextInputEditText;
import android.view.View;
import android.widget.TextView;
import exam.marius.keabank.database.MainDatabase;
import exam.marius.keabank.model.Customer;
import exam.marius.keabank.model.NemId;
import exam.marius.keabank.util.StringUtils;
import exam.marius.keabank.util.ViewUtils;

import java.util.function.BiConsumer;

public class CustomerDetailActivity extends UpNavActivity {

    private static final String EXTRA_CUSTOMER = "exam.marius.extra.EXTRA_CUSTOMER";

    private TextView mNameTextView;
    private TextView mBirthDateTextView;

    private TextInputEditText mOldPasswordField;
    private TextInputEditText mNewPasswordField;
    private TextInputEditText mConfirmNewPasswordField;

    private Customer mCustomer;

    static Intent newIntent(Context packageContext, Customer customer) {
        Intent intent = new Intent(packageContext, CustomerDetailActivity.class);
        intent.putExtra(EXTRA_CUSTOMER, (Parcelable) customer);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_detail);

        mCustomer = getIntent().getParcelableExtra(EXTRA_CUSTOMER);

        mNameTextView = findViewById(R.id.text_name);
        mNameTextView.setText(getString(R.string.text_name, mCustomer.getFirstName() + " " + mCustomer.getLastName()));

        mBirthDateTextView = findViewById(R.id.text_birthdate);
        mBirthDateTextView.setText(getString(R.string.text_birthdate, StringUtils.wrapDate(mCustomer.getBirthDate())));

        mOldPasswordField = findViewById(R.id.field_oldpassword);

        mNewPasswordField = findViewById(R.id.field_newpassword);

        mConfirmNewPasswordField = findViewById(R.id.field_confirmnewpassword);

    }

    public void changePassword(View view) {
        // Input Validation
        final boolean[] validInput = {true};
        final View[] focusView = {null};

        final BiConsumer<TextView, String> errorMacro = ViewUtils.newViewInputError(validInput, focusView);

        // Confirm new password field
        String confirmNewPassword = mConfirmNewPasswordField.getText().toString();

        if (confirmNewPassword.isEmpty()) {
            errorMacro.accept(mConfirmNewPasswordField, getString(R.string.error_field_required));
        } else {
            int messageMinLength = getResources().getInteger(R.integer.field_password_min_length);
            int messageMaxLength = getResources().getInteger(R.integer.field_password_max_length);
            if (confirmNewPassword.length() <= messageMinLength) {
                errorMacro.accept(mConfirmNewPasswordField, getString(R.string.error_password_short, messageMinLength));
            } else if (confirmNewPassword.length() > messageMaxLength) {
                errorMacro.accept(mConfirmNewPasswordField, getString(R.string.error_password_long, messageMaxLength));
            }
        }

        // New password field
        String newPassword = mNewPasswordField.getText().toString();

        if (newPassword.isEmpty()) {
            errorMacro.accept(mNewPasswordField, getString(R.string.error_field_required));
        } else {
            int messageMinLength = getResources().getInteger(R.integer.field_password_min_length);
            int messageMaxLength = getResources().getInteger(R.integer.field_password_max_length);
            if (newPassword.length() <= messageMinLength) {
                errorMacro.accept(mNewPasswordField, getString(R.string.error_password_short, messageMinLength));
            } else if (newPassword.length() > messageMaxLength) {
                errorMacro.accept(mNewPasswordField, getString(R.string.error_password_long, messageMaxLength));
            }
        }

        // Old password field
        String oldPassword = mOldPasswordField.getText().toString();

        if (oldPassword.isEmpty()) {
            errorMacro.accept(mOldPasswordField, getString(R.string.error_field_required));
        } else {
            int messageMinLength = getResources().getInteger(R.integer.field_password_min_length);
            int messageMaxLength = getResources().getInteger(R.integer.field_password_max_length);
            if (oldPassword.length() <= messageMinLength) {
                errorMacro.accept(mOldPasswordField, getString(R.string.error_password_short, messageMinLength));
            } else if (oldPassword.length() > messageMaxLength) {
                errorMacro.accept(mOldPasswordField, getString(R.string.error_password_long, messageMaxLength));
            }
        }
        // END Input Validation

        if (validInput[0]) {
            NemId nemId = MainDatabase.getInstance(this).getNemId(mCustomer);
            if (!nemId.getPassword().equals(oldPassword)) {
                errorMacro.accept(mOldPasswordField, getString(R.string.error_incorrect_password));
            } else {
                if (!newPassword.equals(confirmNewPassword)) {
                    errorMacro.accept(mConfirmNewPasswordField, getString(R.string.error_passwords_no_match));
                } else {
                    nemId.setPassword(newPassword);
                    MainDatabase.getInstance(this).updateNemId(nemId);
                }
            }
        } else {
            focusView[0].requestFocus();
        }
    }
}