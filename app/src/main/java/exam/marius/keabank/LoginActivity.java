package exam.marius.keabank;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import exam.marius.keabank.database.InvalidCustomerException;
import exam.marius.keabank.database.MainDatabase;
import exam.marius.keabank.model.Customer;
import exam.marius.keabank.model.NemId;
import exam.marius.keabank.util.AnimUtils;
import exam.marius.keabank.util.ViewUtils;

import java.util.function.BiConsumer;

/**
 * A login screen that offers login via NemID.
 */
public class LoginActivity extends AppCompatActivity {
    private UserLoginTask mAuthTask = null;

    // UI references.
    private TextInputEditText mUsernameField;
    private TextInputEditText mPasswordField;
    private ImageView mLogoView;
    private View mProgressView;
    private Button mSignButton;
    private Button mRegisterButton;

    private BiConsumer<TextView, String> mErrorMacro;
    private boolean[] mValidInput;
    private View[] mFocusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.

        mUsernameField = findViewById(R.id.username);

        mPasswordField = findViewById(R.id.password);
        mPasswordField.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });

        mLogoView = findViewById(R.id.logo);
        mProgressView = findViewById(R.id.login_progress);

        mSignButton = findViewById(R.id.confirm_button);
        mSignButton.setText(R.string.action_sign_in);
        mSignButton.setOnClickListener(view -> attemptLogin());
        mRegisterButton = findViewById(R.id.register_button);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        mValidInput = new boolean[]{true};
        mFocusView = new View[]{null};

        mErrorMacro = ViewUtils.newViewInputError(mValidInput, mFocusView);

        // Password Field
        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mErrorMacro.accept(mPasswordField, getString(R.string.error_field_required));
        } else {
            int passwordMinLength = getResources().getInteger(R.integer.field_password_min_length);
            int passwordMaxLength = getResources().getInteger(R.integer.field_password_max_length);
            if (password.length() < passwordMinLength) {
                mErrorMacro.accept(mPasswordField, getString(R.string.error_password_short, passwordMinLength));
            } else if (password.length() > passwordMaxLength) {
                mErrorMacro.accept(mPasswordField, getString(R.string.error_password_long, passwordMaxLength));
            }
        }

        // Username Field
        String username = mUsernameField.getText().toString();
        if (TextUtils.isEmpty(username)) {
            mErrorMacro.accept(mUsernameField, getString(R.string.error_field_required));
        } else {
            int usernameMinLength = getResources().getInteger(R.integer.field_username_min_length);
            int usernameMaxLength = getResources().getInteger(R.integer.field_username_max_length);
            if (username.length() < usernameMinLength) {
                mErrorMacro.accept(mUsernameField, getString(R.string.error_username_short, usernameMinLength));
            } else if (username.length() > usernameMaxLength) {
                mErrorMacro.accept(mUsernameField, getString(R.string.error_username_Long, usernameMaxLength));
            }
        }

        if (mValidInput[0]) {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.execute((Void) null);
        } else {
            mFocusView[0].requestFocus();
        }
    }

    /**
     * Shows the progress UI and hides the login and register buttons and the KEA logo.
     */
    private void showProgress(final boolean show) {
        int duration = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        mSignButton.setEnabled(!show);
        AnimUtils.fade(mSignButton, duration, !show).start();
        AnimUtils.fade(mRegisterButton, duration, !show).start();
        AnimUtils.fade(mLogoView, duration, !show).start();
        AnimUtils.fade(mProgressView, duration, show).start();
    }

    public void startRegisterActivity(View view) {
        Intent i = new Intent(this, RegisterActivity.class);
        startActivity(i);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private Customer mCustomer;
        private boolean mInvalidNemId;
        private boolean mNoCustomer;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
            mInvalidNemId = false;
            mNoCustomer = false;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                // Simulate network access.
                int duration = 1000;
                long before = System.currentTimeMillis();

                boolean result = true;

                // Attempt to validate the NemID
                NemId actualNemId = MainDatabase.getInstance(LoginActivity.this).tryLogin(new NemId(mEmail, mPassword));
                if (actualNemId == null) {
                    mInvalidNemId = true;
                    result = false;
                } else {

                    // Attempt to retrieve the associated Customer
                    try {
                        mCustomer = MainDatabase.getInstance(LoginActivity.this).getCustomer(actualNemId);
                    } catch (InvalidCustomerException e) {
                        mNoCustomer = true;
                        result = false;
                    }
                }

                long after = System.currentTimeMillis();

                Thread.sleep(duration - (after - before));

                return result;
            } catch (InterruptedException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                Intent i = HomeActivity.newIntent(LoginActivity.this, mCustomer);
                startActivity(i);
                finish();
            } else {
                if (mInvalidNemId) {
                    mErrorMacro.accept(mUsernameField, getString(R.string.error_account_not_found));
                }

                if (mNoCustomer) {
                    mErrorMacro.accept(mUsernameField, getString(R.string.error_nemid_no_customer));
                }

                showProgress(false);
                mFocusView[0].requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

