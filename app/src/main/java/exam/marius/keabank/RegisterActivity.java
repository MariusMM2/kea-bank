package exam.marius.keabank;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import exam.marius.keabank.database.MainDatabase;
import exam.marius.keabank.model.Account;
import exam.marius.keabank.model.Customer;
import exam.marius.keabank.model.NemId;
import exam.marius.keabank.util.StringUtils;
import exam.marius.keabank.util.TimeUtils;
import exam.marius.keabank.util.ViewUtils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class RegisterActivity extends UpNavActivity {
    private static final String TAG = "RegisterActivity";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private static final String INSTANCE_CUSTOMER = "INSTANCE_CUSTOMER";
    private static final String INSTANCE_NEMID = "INSTANCE_NEMID";

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

        if (savedInstanceState != null) {
            mCustomer = savedInstanceState.getParcelable(INSTANCE_CUSTOMER);
            mNemId = savedInstanceState.getParcelable(INSTANCE_NEMID);
        }

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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(INSTANCE_CUSTOMER, mCustomer);
        outState.putParcelable(INSTANCE_NEMID, mNemId);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (permissions.length == 1 &&
                    permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setClosestAffiliate();

            } else {
                setDefaultAffiliate();
            }

            finishRegistration();
        }
    }

    private void setClosestAffiliate() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnCompleteListener(this, locationTask -> {
            Location location = locationTask.getResult();
            if (location != null) {
                Log.i(TAG, "setClosestAffiliate: found device location: " + location.toString());
                List<Float> distances = Arrays
                        .stream(Customer.Affiliate.values())
                        .map(value -> value.getLocation().distanceTo(location))
                        .collect(Collectors.toList());

                Log.d(TAG, "setClosestAffiliate: distances: " + distances.toString());

                Float smallestDistance = distances
                        .stream()
                        .min(Float::compareTo)
                        .orElse(-1f);

                Log.d(TAG, "setClosestAffiliate: smallestDistance: " + smallestDistance);

                if (smallestDistance == -1f) {
                    Log.w(TAG, "setClosestAffiliate: unable to find distance, setting default");
                    setDefaultAffiliate();
                } else {
                    Customer.Affiliate affiliate = Customer.Affiliate.values()[distances.indexOf(smallestDistance)];
                    Log.i(TAG, "setClosestAffiliate: found closest: " + affiliate);
                    mCustomer.setAffiliate(affiliate);
                }
            } else {
                Log.w(TAG, "setClosestAffiliate: location is null");
                setDefaultAffiliate();
            }

            finishRegistration();
        });
    }

    private void setDefaultAffiliate() {
        Log.i(TAG, "setDefaultAffiliate: Setting default affiliate");
        mCustomer.setAffiliate(Customer.Affiliate.COPENHAGEN);
        finishRegistration();
    }

    private void finishRegistration() {
        Toast.makeText(this, getString(R.string.action_affiliate_set, mCustomer.getAffiliate().getText()), Toast.LENGTH_SHORT).show();
        MainDatabase.getInstance(this).addCustomer(mCustomer);
        MainDatabase.getInstance(this).addNemId(mNemId);

        Intent i = HomeActivity.newIntent(this, mCustomer);
        startActivity(i);
        finish();
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
                    .setMessage(getString(R.string.dialog_confirm_customer_register,
                            firstName,
                            lastName,
                            StringUtils.wrapDate(date),
                            mNemId.getUsername()))
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        mCustomer = new Customer(firstName, lastName, date);

                        mNemId.setCustomerId(mCustomer.getId());

                        mCustomer.addAccount(Account.newDefault(1000, mCustomer.getId()));
                        mCustomer.addAccount(Account.newBudget(0, mCustomer.getId()));

                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {
                            Log.i(TAG, "validateInput: Location permission given");
                            GoogleApiAvailability instance = GoogleApiAvailability.getInstance();
                            int googleServicesResult = instance.isGooglePlayServicesAvailable(this);
                            if (googleServicesResult == ConnectionResult.SUCCESS) {
                                Log.i(TAG, "validateInput: Google Services is available");
                                setClosestAffiliate();
                            } else {
                                Log.i(TAG, "validateInput: Google services is unavailable");
                                instance.getErrorDialog(this, googleServicesResult, 0, dialog1 -> setDefaultAffiliate()).show();
                            }
                        } else {
                            // Show rationale and request permission.
                            Log.i(TAG, "validateInput: Location permission not given, requesting permission");
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                        }
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
