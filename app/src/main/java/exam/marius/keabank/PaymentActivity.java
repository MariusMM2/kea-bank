package exam.marius.keabank;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.widget.*;
import exam.marius.keabank.model.Account;
import exam.marius.keabank.model.Bill;
import exam.marius.keabank.model.Customer;
import exam.marius.keabank.model.Transaction;
import exam.marius.keabank.util.StringWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PaymentActivity extends UpNavActivity {

    static final int REQUEST_PAYMENT = 0x1;
    static final int RESULT_CODE_SUCCESS = 1;

    private static final String EXTRA_CUSTOMER = "com.example.extras.EXTRA_CUSTOMER";
    private static final String EXTRA_BILLS = "com.example.extras.EXTRA_BILLS";

    private Customer mCustomer;
    private List<Bill> mBillList;
    private Transaction mNewTransaction;

    private LinearLayout mBillDetailsLayout;
    private LinearLayout mRecurrentLayout;

    private AppCompatSpinner mSourcesSpinner;
    private AppCompatSpinner mBillsSpinner;
    private TextView mTitleField;
    private TextView mDescriptionField;
    private TextView mAmountField;
    private TextView mDueDateField;
    private CheckBox mRecurrentField;

    private SpinnerAdapter mSourcesAdapter;
    private SpinnerAdapter mBillsAdapter;

    static Intent newIntent(Context packageContext, Customer customer, List<Bill> billList) {
        Intent intent = new Intent(packageContext, PaymentActivity.class);
        intent.putExtra(EXTRA_CUSTOMER, (Parcelable) customer);
        intent.putParcelableArrayListExtra(EXTRA_BILLS, (ArrayList<? extends Parcelable>) billList);

        return intent;
    }

    static Customer getCustomer(Intent intent) {
        return intent.getParcelableExtra(EXTRA_CUSTOMER);
    }

    @SuppressWarnings("Duplicates")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        mCustomer = getCustomer(getIntent());
        mBillList = getIntent().getParcelableArrayListExtra(EXTRA_BILLS);

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

        // Bills Field
        List<String> billEntries = mBillList.stream().map(Bill::getTitle).collect(Collectors.toList());
        billEntries.add(0, getResources().getString(R.string.prompt_bill_select_bill));

        mBillsAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, billEntries);

        mBillsSpinner = findViewById(R.id.spinner_bills);
        mBillsSpinner.setAdapter(mBillsAdapter);
        mBillsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    setContents(null);
                } else {
                    setContents(mBillList.get(position - 1));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mBillsSpinner.setSelection(0);
            }
        });

        // Bill Details
        mBillDetailsLayout = findViewById(R.id.layout_bill_details);

        // Title Field
        mTitleField = findViewById(R.id.text_bill_title);

        // Description Field
        mDescriptionField = findViewById(R.id.text_bill_description);

        // Amount Field
        mAmountField = findViewById(R.id.text_bill_amount);

        // Due Date Field
        mDueDateField = findViewById(R.id.text_bill_due_date);

        // Recurrent Field
        mRecurrentLayout = findViewById(R.id.layout_recurrent);
        mRecurrentField = findViewById(R.id.checkbox_automated);

        setContents(null);
    }

    private void setContents(Bill bill) {
        int visibility = bill != null ? View.VISIBLE : View.GONE;

        mBillDetailsLayout.setVisibility(visibility);

        if (bill != null) {
            mTitleField.setText(bill.getTitle());
            mDescriptionField.setText(bill.getDescription());
            mAmountField.setText(String.valueOf(bill.getAmount()));
            mDueDateField.setText(StringWrapper.wrapDate(bill.getDueDate()));
            mRecurrentLayout.setVisibility(bill.isRecurrent() ? View.VISIBLE : View.INVISIBLE);
        }

        mRecurrentField.setChecked(false);
    }

    private void showCustomerErrorDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Error retrieving customer details")
                .setPositiveButton(android.R.string.ok, null)
                .setOnDismissListener(dialog -> finish())
                .create().show();
    }

    public void submitPayment(View view) {

    }
}
