package exam.marius.keabank.database;

import android.content.Context;
import android.support.annotation.NonNull;
import exam.marius.keabank.model.*;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"SpellCheckingInspection", "unchecked"})
public class MainDatabase {
    static boolean DEBUG_NO_NEMID = true;
    static boolean DEBUG_NO_PASSWORD = true;
    private static MainDatabase sInstance;
    Database mAccountDb, mBillDb, mCustomerDb, mNemIdDb, mTransactionDb;
    private static Customer mDummyCustomer = createDummyCustomer();

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

    public static Customer createDummyCustomer() {
        Customer customer = new Customer("John", "Doe", "johndoe@email.com", "123456", Calendar.getInstance().getTime());
        final List<Account> accountList = new ArrayList<>(
                Arrays.asList(
                        Account.newDefault(3000, customer.getId()),
                        Account.newBudget(1000, customer.getId())
                )
        );
        final List<Bill> billList = new ArrayList<>(
                Arrays.asList(
                        new Bill("Bill 1", "1++", false, 10, Calendar.getInstance().getTime(), customer.getId()),
                        new Bill("Bill 2", "2++", false, 20, Calendar.getInstance().getTime(), customer.getId()),
                        new Bill("Bill 3", "3++", false, 30, Calendar.getInstance().getTime(), customer.getId())
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
            Collections.reverse(allTransactions);
            allTransactions.forEach(account::addTransaction);

            customer.addAccount(account);
        });

        return customer;
    }

    public static Customer getDummyCustomer() {
        return mDummyCustomer;
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
