package com.example.keabank;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.example.keabank.model.Account;
import com.example.keabank.model.Bill;
import com.example.keabank.model.Customer;
import com.example.keabank.model.Transaction;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("Duplicates")
public class HomeActivity extends AppCompatActivity {

    private Customer mCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getDebugCustomer();
    }

    private void getDebugCustomer() {
        mCustomer = new Customer("John", "Doe", "johndoe@email.com", "123456", Calendar.getInstance().getTime());
        final List<Account> accountList = new ArrayList<>(
                Arrays.asList(
                        Account.newDefault(3000, mCustomer.getId()),
                        Account.newBudget(1000, mCustomer.getId())
                )
        );
        final List<Bill> billList = new ArrayList<>(
                Arrays.asList(
                        new Bill("1", "1++", false, 10, Calendar.getInstance().getTime(), mCustomer.getId()),
                        new Bill("2", "2++", false, 20, Calendar.getInstance().getTime(), mCustomer.getId()),
                        new Bill("3", "3++", false, 30, Calendar.getInstance().getTime(), mCustomer.getId())
                )
        );
        final List<Transaction> transactionList = new ArrayList<>(
                Arrays.asList(
                        Transaction.beginTransaction().setSource(accountList.get(0)).setDestination(accountList.get(1)).setAmount(1000),
                        Transaction.beginTransaction().setSource(accountList.get(1)).setDestination(accountList.get(0)).setAmount(2000),
                        Transaction.beginTransaction().setSource(accountList.get(0)).setDestination(billList.get(0)).setAmount(billList.get(0).getAmount()),
                        Transaction.beginTransaction().setSource(accountList.get(1)).setDestination(billList.get(2)).setAmount(billList.get(2).getAmount()),
                        Transaction.beginTransaction().setSource(billList.get(1)).setDestination(accountList.get(0)).setAmount(billList.get(1).getAmount())
                )
        );

        accountList.forEach(account -> {
            List<Transaction> outGoingTransactions = transactionList.stream()
                    .filter(transaction -> transaction.getSource().getId().equals(account.getId()))
                    .collect(Collectors.toList());

            List<Transaction> incomingTransactions = transactionList.stream()
                    .filter(transaction -> transaction.getDestination().getId().equals(account.getId()))
                    .map(Transaction::reverse)
                    .collect(Collectors.toList());

            List<Transaction> allTransactions = new ArrayList<>();

            allTransactions.addAll(outGoingTransactions);
            allTransactions.addAll(incomingTransactions);
            allTransactions.sort(Comparator.comparing(Transaction::getDate));
            allTransactions.forEach(account::addTransaction);

            mCustomer.addAccount(account);
        });
    }


}
