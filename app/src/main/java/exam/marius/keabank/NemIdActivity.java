package exam.marius.keabank;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
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

public class NemIdActivity extends AppCompatActivity implements AsyncTaskCallback {

    static final int REQUEST_NEMID = 0x1;
    static final int REQUEST_NEMID_CONFIRMATION = 0x2;

    static final int RESULT_NEW_NEMID = 0x2;
    public static final int RESULT_EXISTING_CUSTOMER = 0x4;


    private static final String EXTRA_CUSTOMER = "exam.marius.extra.EXTRA_CUSTOMER";
    private static final String EXTRA_NEMID = "exam.marius.extra.EXTRA_NEMID";

    private TextInputEditText mUsernameField;
    private TextInputEditText mPasswordField;
    private ImageView mLogoView;
    private View mProgressView;
    private Button mConfirmButton;

    private BiConsumer<TextView, String> mErrorMacro;
    private boolean[] mValidInput;
    private TextView[] mFocusView;

    private UserLoginTask mAuthTask;

    private Customer mCustomer;

    static Intent newIntent(Context packageContext, Customer customer) {
        Intent intent = new Intent(packageContext, NemIdActivity.class);
        intent.putExtra(EXTRA_CUSTOMER, (Parcelable) customer);
        return intent;
    }

    static NemId getNemId(Intent intent) {
        return intent.getParcelableExtra(EXTRA_NEMID);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_nem_id);
        setFinishOnTouchOutside(false);

        mCustomer = getIntent().getParcelableExtra(EXTRA_CUSTOMER);

        mUsernameField = findViewById(R.id.username);
        mPasswordField = findViewById(R.id.password);
        mPasswordField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                NemIdActivity.this.attemptValidate();
                return true;
            }
            return false;
        });

        mLogoView = findViewById(R.id.logo);
        mProgressView = findViewById(R.id.login_progress);

        mConfirmButton = findViewById(R.id.confirm_button);
        mConfirmButton.setOnClickListener(v -> attemptValidate());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAuthTask = null;
    }

    private void attemptValidate() {
        if (mAuthTask != null) {
            return;
        }

        mValidInput = new boolean[]{true};
        mFocusView = new TextView[]{null};

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
            mAuthTask = new UserLoginTask(this, username, password);
            mAuthTask.execute((Void) null);
        } else {
            mFocusView[0].requestFocus();
        }
    }

    @Override
    public void onAsyncTaskComplete(NemId actualNemId, boolean validNemId, boolean noCustomer) {
        if (mCustomer == null) {
            // Return the associated NemID
            if (validNemId) {
                if (noCustomer) {
                    // NemID found, no associated customer found, return OK
                    Intent intent = new Intent();
                    final NemId nemId = new NemId(mUsernameField.getText().toString(), mPasswordField.getText().toString());
                    intent.putExtra(EXTRA_NEMID, (Parcelable) nemId);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    mErrorMacro.accept(mUsernameField, getResources().getString(R.string.error_existing_customer));
                    mFocusView[0].requestFocus();
                }
            } else {
                // No NemID found, return special result
                Intent intent = new Intent();
                final NemId nemId = new NemId(mUsernameField.getText().toString(), mPasswordField.getText().toString());
                intent.putExtra(EXTRA_NEMID, (Parcelable) nemId);
                setResult(RESULT_NEW_NEMID, intent);
                finish();
            }
        } else {
            // Return the associated Customer
            if (validNemId) {
                if (!noCustomer) {
                    if (actualNemId.getCustomerId().equals(mCustomer.getId())) {
                        //Success
                        Intent intent = new Intent();
                        setResult(RESULT_OK);
                        finish();
                    }
                } else {
                    mErrorMacro.accept(mUsernameField, getString(R.string.error_invalid_nemid));
                    mFocusView[0].requestFocus();
                }
            } else {
                mErrorMacro.accept(mUsernameField, getString(R.string.error_invalid_nemid));
                mFocusView[0].requestFocus();
            }
        }
    }

    /**
     * Shows the progress UI and hides the login and register buttons and the KEA logo.
     */
    private void showProgress(final boolean show) {
        int duration = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        mConfirmButton.setEnabled(!show);
        AnimUtils.fade(mConfirmButton, duration, !show).start();
        AnimUtils.fade(mLogoView, duration, !show).start();
        AnimUtils.fade(mProgressView, duration, show).start();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Void> {

        private final String mEmail;
        private final String mPassword;
        private Customer mCustomer;
        private boolean mValidNemId;
        private boolean mNoCustomer;
        private AsyncTaskCallback mCallback;
        private NemId mActualNemId;

        UserLoginTask(AsyncTaskCallback callback, String email, String password) {
            mCallback = callback;
            mEmail = email;
            mPassword = password;
            mValidNemId = false;
            mNoCustomer = false;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Simulate network access.

                // Attempt to validate the NemID
                mActualNemId = MainDatabase.getInstance(NemIdActivity.this).tryLogin(new NemId(mEmail, mPassword));
                if (mActualNemId == null) {
                    mValidNemId = false;
                } else {

                    mValidNemId = true;
                    // Attempt to retrieve the associated Customer
                    try {
                        MainDatabase.getInstance(NemIdActivity.this).getCustomer(mActualNemId);
                        mNoCustomer = false;
                    } catch (InvalidCustomerException e) {
                        mNoCustomer = true;
                    }
                }

                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mAuthTask = null;
            showProgress(false);
            mCallback.onAsyncTaskComplete(mActualNemId, mValidNemId, mNoCustomer);
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

interface AsyncTaskCallback {
    void onAsyncTaskComplete(NemId actualNemId, boolean validNemId, boolean noCustomer);
}
