package com.example.keabank.database;

import android.content.Context;
import android.support.annotation.NonNull;
import com.example.keabank.model.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings({"SpellCheckingInspection", "unchecked"})
public class MainDatabase {
    static boolean DEBUG_NO_NEMID = false;
    static boolean DEBUG_NO_PASSWORD = true;
    private static MainDatabase sInstance;
    Database mAccountDb, mBillDb, mCustomerDb, mNemIdDb, mTransactionDb;

    MainDatabase(Context context) {
        mAccountDb = new AccountDatabase(context);
        mBillDb = new BillDatabase(context);
        mCustomerDb = new CustomerDatabase(context);
        mNemIdDb = new NemIdDatabase(context);
        mTransactionDb = new TransactionDatabase(context);
    }

    public static MainDatabase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MainDatabase(context);
        }

        return sInstance;
    }

    public boolean tryLogin(NemId nemId) {
        if (DEBUG_NO_NEMID) {
            return true;
        }

        NemId retrievedNemId = (NemId) mNemIdDb.read(databaseItem -> ((NemId) databaseItem).getUsername().equals(nemId.getUsername()));

        if (retrievedNemId != null) {
            if (DEBUG_NO_PASSWORD) {
                return true;
            } else {
                return retrievedNemId.getPassword().equals(nemId.getPassword());
            }
        } else {
            return false;
        }

    }

    public Customer getCustomer(@NonNull NemId nemId) throws InvalidNemIDException, InvalidCustomerException {
        NemId retrievedNemId = (NemId) mNemIdDb.read(item -> item.getId().equals(nemId.getId()));

        if (retrievedNemId == null) throw new InvalidNemIDException();

        Customer retrievedCustomer = (Customer) mCustomerDb.read(item -> item.getId().equals(retrievedNemId.getCustomerId()));

        if (retrievedCustomer == null) throw new InvalidCustomerException();

        retrievedCustomer.removeAccounts();

        List<Account> retrievedAccounts = (List<Account>) (Object) mAccountDb.readMultiple(item -> ((Account) item).getCustomerId().equals(retrievedCustomer.getId()));

        retrievedAccounts.forEach(account -> {
            List<Transaction> outGoingTransactions = (List<Transaction>) (Object) mTransactionDb.readMultiple(item -> {
                Transaction transaction = ((Transaction) item);
                return transaction.getSource().getId().equals(account.getId());
            });

            outGoingTransactions.forEach(account::addTransaction);

            List<Transaction> incomingTransactions = ((List<Transaction>) (Object) mTransactionDb.readMultiple(item -> {
                Transaction transaction = ((Transaction) item);
                return transaction.getDestination().getId().equals(account.getId());
            })).stream().map(Transaction::reverse).collect(Collectors.toList());

            incomingTransactions.forEach(account::addTransaction);

            retrievedCustomer.addAccount(account);
        });

        return retrievedCustomer;
    }

    public List<Bill> getBills(@NonNull Customer customer) {
        return (List<Bill>) (Object) mBillDb.readMultiple(item -> ((Bill) item).getCustomerId().equals(customer.getId()));
    }

    public Account findAccount(UUID accountId) {
        return (Account) mAccountDb.read(item -> item.getId().equals(accountId));
    }

    public void save() {
        mAccountDb.save();
        mBillDb.save();
        mCustomerDb.save();
        mNemIdDb.save();
        mTransactionDb.save();
    }
}

class InvalidNemIDException extends Exception {

}

class InvalidCustomerException extends Exception {

}
