package com.example.keabank;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.example.keabank.model.Customer;

public class AccountActivity extends AppCompatActivity {

    private Customer mCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mCustomer = HomeActivity.getDebugCustomer();
    }
}
