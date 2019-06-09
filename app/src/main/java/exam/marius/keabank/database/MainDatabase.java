package exam.marius.keabank.database;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import exam.marius.keabank.model.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static exam.marius.keabank.database.AbstractDatabase.sFilesDir;

@SuppressWarnings({"SpellCheckingInspection", "WeakerAccess"})
public class MainDatabase {
    private static final String TAG = "MainDatabase";

    static boolean DEBUG_NO_NEMID = true;
    static boolean DEBUG_NO_PASSWORD = true;
    static boolean DEBUG_NO_PERSIST = false;
    private static MainDatabase sInstance;
    AccountDatabase mAccountDb;
    BillDatabase mBillDb;
    CustomerDatabase mCustomerDb;
    NemIdDatabase mNemIdDb;
    TransactionDatabase mTransactionDb;

    MainDatabase(Context context) {

        if (sFilesDir == null) {
            sFilesDir = context.getFilesDir();
        }

        mAccountDb = new AccountDatabase(context);
        mBillDb = new BillDatabase(context);
        mCustomerDb = new CustomerDatabase(context);
        mNemIdDb = new NemIdDatabase(context);
        mTransactionDb = new TransactionDatabase(context);

        if (DEBUG_NO_PERSIST) {
            try {
                createDummyData();
            } catch (TransactionException e) {
                Log.e(TAG, "MainDatabase: Unable to generate dummy data: ", e);
                return;
            }

        }

        doUpdate();
    }

    public static MainDatabase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MainDatabase(context);
        }

        return sInstance;
    }

    @SuppressWarnings("WeakerAccess")
    public boolean tryLogin(NemId nemId) {
        if (DEBUG_NO_NEMID) {
            return true;
        }

        NemId retrievedNemId = mNemIdDb.read(databaseItem -> databaseItem.getUsername().equals(nemId.getUsername()));

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

    public Customer getDummyCustomer() {
        NemId nemId = mNemIdDb.readAll().get(0);
        Customer customer = null;
        try {
            customer = getCustomer(nemId);
        } catch (InvalidNemIDException | InvalidCustomerException ignored) {

        }
        return customer;
    }

    @SuppressWarnings("WeakerAccess")
    public Customer getCustomer(@NonNull NemId nemId) throws InvalidNemIDException, InvalidCustomerException {
        NemId retrievedNemId = mNemIdDb.read(item -> item.getId().equals(nemId.getId()));

        if (retrievedNemId == null) throw new InvalidNemIDException();

        Customer retrievedCustomer = mCustomerDb.read(item -> item.getId().equals(retrievedNemId.getCustomerId()));

        if (retrievedCustomer == null) throw new InvalidCustomerException();

        retrievedCustomer.removeAccounts();

        List<Account> retrievedAccounts = mAccountDb.readMultiple(item -> item.getCustomerId().equals(retrievedCustomer.getId()));

        retrievedAccounts.forEach(account -> {
            List<Transaction> transactionList = getTransactions(account);

            transactionList.forEach(account::addTransaction);

            retrievedCustomer.addAccount(account);
        });

        return retrievedCustomer;
    }

    public List<Bill> getBills(@NonNull Customer customer) {
        return mBillDb.readMultiple(item -> item.getCustomerId().equals(customer.getId()));
    }

    public Account getAccount(UUID accountId) {
        return mAccountDb.read(item -> item.getId().equals(accountId));
    }

    public Account getAccount(String accountNumber) {
        return mAccountDb.read(account -> account.getNumber().equals(accountNumber));
    }

    void save() {
        mAccountDb.save();
        mBillDb.save();
        mCustomerDb.save();
        mNemIdDb.save();
        mTransactionDb.save();
    }

    public void addTransaction(Transaction newTransaction) {
        mTransactionDb.add(newTransaction);
        doUpdate(newTransaction);
    }

    public List<Transaction> getTransactions(Account account) {
        List<Transaction> outGoingTransactions = mTransactionDb.readMultiple(item ->
                item.getSource().getId().equals(account.getId()));

        List<Transaction> incomingTransactions = mTransactionDb.readMultiple(item ->
                item.getDestination().getId().equals(account.getId()))
                .stream()
                .map(Transaction::reverse)
                .collect(Collectors.toList());

        outGoingTransactions.addAll(incomingTransactions);

        return outGoingTransactions;
    }

    public void doUpdate() {
        List<Transaction> allTransactions = mTransactionDb.readMultiple(transaction1 -> !transaction1.isDone());
        allTransactions.forEach(this::doUpdate);
    }

    public void doUpdate(Transaction transaction) {
        if (transaction.isClose()) {
            boolean hasCommited = false;
            try {
                hasCommited = transaction.commitOnTime();
            } catch (TransactionException e) {
                e.printStackTrace();
            }

            if (hasCommited) {

                final Consumer<TransactionTarget> targetConsumer = target -> {
                    updateTransactionTarget(target);

                    if (target instanceof Bill) {
                        Bill targetBill = (Bill) target;
                        mBillDb.add(targetBill.next());
                    }
                };

                targetConsumer.accept(transaction.getSource());
                targetConsumer.accept(transaction.getDestination());
            } else {
                transaction.setPending();
            }

            mTransactionDb.update(transaction);
        }
    }

    private void updateTransactionTarget(TransactionTarget target) {
        if (target instanceof Account) {
            mAccountDb.update((Account) target);
        } else if (target instanceof Bill) {
            mBillDb.update((Bill) target);
        }
    }

    public void createDummyData() throws TransactionException {
        Customer customer = new Customer("John", "Doe", "johndoe@email.com", "123456", Calendar.getInstance().getTime());
        final List<NemId> nemIdList = new ArrayList<>(
                Collections.singletonList(
                        new NemId(UUID.randomUUID(), "foobar98", "f00b4r98", customer.getId())
                )
        );

        final List<Account> accountList = new ArrayList<>(
                Arrays.asList(
                        Account.newDefault(3000, customer.getId()),
                        Account.newBudget(1000, customer.getId()),
                        Account.newSavings(0, customer.getId())
                )
        );
        final List<Bill> billList = new ArrayList<>(
                Arrays.asList(
                        new Bill("Bill 1", "1++", true, 10, Calendar.getInstance().getTime(), customer.getId()),
                        new Bill("Bill 2", "2++", false, 20, Calendar.getInstance().getTime(), customer.getId()),
                        new Bill("Bill 3", "3++", true, 30, Calendar.getInstance().getTime(), customer.getId())
                )
        );
        final List<Transaction> transactionList = new ArrayList<>(
                Arrays.asList(
                        Transaction.beginTransaction().setSource(accountList.get(0)).setDestination(accountList.get(1)).setAmount(1000).commit(),
                        Transaction.beginTransaction().setSource(accountList.get(1)).setDestination(accountList.get(0)).setAmount(2000).commit(),
                        Transaction.beginTransaction().setSource(accountList.get(0)).setDestination(billList.get(0)).setAmount(billList.get(0).getAmount()),
                        Transaction.beginTransaction().setSource(accountList.get(1)).setDestination(billList.get(2)).setAmount(billList.get(2).getAmount()).commit(),
                        Transaction.beginTransaction().setSource(billList.get(1)).setDestination(accountList.get(0)).setAmount(billList.get(1).getAmount()).commit()
                )
        );

        boolean oldFlag = DEBUG_NO_PERSIST;

        if (!oldFlag) {
            DEBUG_NO_PERSIST = true;
        }
        mAccountDb = new AccountDatabase(null);
        mBillDb = new BillDatabase(null);
        mCustomerDb = new CustomerDatabase(null);
        mNemIdDb = new NemIdDatabase(null);
        mTransactionDb = new TransactionDatabase(null);

        if (!oldFlag) {
            DEBUG_NO_PERSIST = false;
        }

        accountList.forEach(account -> mAccountDb.add(account));
        billList.forEach(bill -> mBillDb.add(bill));
        mCustomerDb.add(customer);
        nemIdList.forEach(nemId -> mNemIdDb.add(nemId));
        transactionList.forEach(transaction -> mTransactionDb.add(transaction));
    }
}

class InvalidNemIDException extends Exception {

}

class InvalidCustomerException extends Exception {

}
