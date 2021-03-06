package exam.marius.keabank.database;

import android.content.Context;
import android.support.annotation.NonNull;
import exam.marius.keabank.model.*;
import exam.marius.keabank.util.TimeUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static exam.marius.keabank.database.AbstractDatabase.sFilesDir;

@SuppressWarnings({"SpellCheckingInspection", "WeakerAccess"})
public class MainDatabase {
    private static final String TAG = "MainDatabase";

    static boolean DEBUG_NO_NEMID = false;
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

        if (DEBUG_NO_PERSIST || mNemIdDb.readAll().size() == 0) {
            createDummyData();
        }

        doUpdate();
    }

    public static MainDatabase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MainDatabase(context);
        }

        return sInstance;
    }

    public NemId tryLogin(NemId nemId) {
        if (DEBUG_NO_NEMID) {
            return mNemIdDb.readAll().get(0);
        }

        NemId retrievedNemId;
        try {
            retrievedNemId = mNemIdDb.read(databaseItem -> databaseItem.getUsername().equals(nemId.getUsername()));
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }

        if (DEBUG_NO_PASSWORD) {
            return retrievedNemId;
        } else {
            if (retrievedNemId.getPassword().equals(nemId.getPassword())) {
                return retrievedNemId;
            } else {
                return null;
            }
        }

    }

    public NemId getNemId(Customer customer) {
        return mNemIdDb.read(nemId -> nemId.getCustomerId().equals(customer.getId()));
    }

    public Customer getCustomer(@NonNull NemId nemId) throws InvalidCustomerException {
        NemId retrievedNemId = mNemIdDb.read(item -> item.getId().equals(nemId.getId()));

        Customer retrievedCustomer = mCustomerDb.read(item -> item.getId().equals(retrievedNemId.getCustomerId()));

        if (retrievedCustomer == null) throw new InvalidCustomerException();

        getAccounts(retrievedCustomer);

        return retrievedCustomer;
    }

    private void getAccounts(Customer customer) {
        List<Account> retrievedAccounts = mAccountDb.readMultiple(item -> item.getCustomerId().equals(customer.getId()));

        retrievedAccounts.forEach(account -> {
            List<Transaction> transactionList = getTransactions(account);

            transactionList.forEach(account::addTransaction);

            customer.addAccount(account);
        });
    }

    public Customer getCustomer(@NonNull UUID uuid) {
        final Customer customer = mCustomerDb.read(customer1 -> customer1.getId().equals(uuid));
        getAccounts(customer);
        return customer;
    }

    public List<Bill> getOpenBills(@NonNull Customer customer) {
        final List<Bill> bills = mBillDb.readMultiple(bill -> bill.getCustomerId().equals(customer.getId()))
                .stream()
                .filter(Bill::isOpen)
                .collect(Collectors.toList());
        return bills;
    }

    public Account getAccount(UUID accountId) {
        return mAccountDb.read(item -> item.getId().equals(accountId));
    }

    public Account getAccount(String accountNumber) {
        return mAccountDb.read(account -> account.getNumber().equals(accountNumber));
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

    public void addNemId(NemId nemId) {
        mNemIdDb.add(nemId);
    }

    public void addCustomer(Customer customer) {
        List<Account> accounts = customer.getAccountList();
        accounts.forEach(account -> mAccountDb.add(account));

        mCustomerDb.add(customer);
        addPresetBills(customer);
    }

    public void addAccount(Account account) {
        mAccountDb.add(account);
    }

    public void addTransaction(Transaction newTransaction) {
        mTransactionDb.add(newTransaction);
        doUpdate(newTransaction);
    }

    public void doUpdate() {
        List<Transaction> allTransactions = mTransactionDb.readMultiple(transaction -> !transaction.isDone());
        allTransactions.forEach(this::doUpdate);
    }

    public void doUpdate(Transaction transaction) {
        final Consumer<TransactionTarget> targetConsumer = target -> {

            if (target instanceof Bill) {
                Bill targetBill = (Bill) target;
                if (targetBill.isRecurrent()) {
                    if (transaction.getType().equals(Transaction.Type.PAYMENT_SERVICE)) {
                        targetBill.setAutomated(true);
                    }
                    final Bill nextBill = targetBill.next();
                    if (transaction.getType().equals(Transaction.Type.PAYMENT_SERVICE)) {
                        Transaction nextTransaction = Transaction.beginTransaction();
                        if (targetBill.equals(transaction.getDestination())) {
                            nextTransaction
                                    .setSource(transaction.getSource())
                                    .setDestination(nextBill);
                        } else if (targetBill.equals(transaction.getSource())) {
                            nextTransaction
                                    .setSource(nextBill)
                                    .setDestination(transaction.getDestination());
                        }

                        nextTransaction.setTitle(transaction.getTitle())
                                .setDate(nextBill.getDueDate())
                                .setMessage(transaction.getMessage())
                                .setAmount(nextBill.getAmount())
                                .setType(transaction.getType())
                                .setStatus(Transaction.Status.IDLE);
                        mTransactionDb.add(nextTransaction);
                        mBillDb.add(nextBill);
                        doUpdate(nextTransaction);
                    } else {
                        mBillDb.add(nextBill);
                    }
                }
            }

            updateTransactionTarget(target);
        };

        if (transaction.isClose()) {
            boolean hasCommited = false;
            try {
                hasCommited = transaction.commitOnTime();
            } catch (TransactionException e) {
                e.printStackTrace();
            }

            if (hasCommited) {
                targetConsumer.accept(transaction.getSource());
                targetConsumer.accept(transaction.getDestination());
                if (transaction.getSource() instanceof Account && transaction.getDestination() instanceof Account) {
                    if (transaction.getType().equals(Transaction.Type.PAYMENT_SERVICE)) {
                        Transaction nextTransaction = Transaction.beginTransaction();
                        nextTransaction.setSource(transaction.getSource())
                                .setDestination(transaction.getDestination())
                                .setTitle(transaction.getTitle())
                                .setDate(TimeUtils.addMonths(transaction.getDate(), 1))
                                .setMessage(transaction.getMessage())
                                .setAmount(transaction.getAmount())
                                .setType(Transaction.Type.PAYMENT_SERVICE)
                                .setStatus(Transaction.Status.IDLE);
                        mTransactionDb.add(nextTransaction);
                        doUpdate(nextTransaction);
                    }

                }
            } else {
                transaction.setPending();
            }

            mTransactionDb.update(transaction);
        } else {
            TransactionTarget destination = transaction.getDestination();
            if (destination instanceof Bill) {
                Bill destinationBill = (Bill) destination;
                if (transaction.getType().equals(Transaction.Type.PAYMENT_SERVICE)) {
                    destinationBill.setAutomated(true);
                    updateTransactionTarget(destinationBill);
                }
            }
        }
    }

    private void updateTransactionTarget(TransactionTarget target) {
        if (target instanceof Account) {
            mAccountDb.update((Account) target);
        } else if (target instanceof Bill) {
            mBillDb.update((Bill) target);
        }
    }

    public void createDummyData() {
        //Clear database
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

        //Fill database
        Customer customer = new Customer("John", "Doe", Calendar.getInstance().getTime());
        final List<NemId> nemIdList = new ArrayList<>(
                Arrays.asList(
                        new NemId(UUID.randomUUID(), "foobar98", "f00b4r98", customer.getId()),
                        new NemId("foobar99", "f00b4r98")
                )
        );

        final List<Account> accountList = new ArrayList<>(
                Arrays.asList(
                        Account.newDefault(3000, customer.getId()),
                        Account.newBudget(1000, customer.getId()),
                        Account.newSavings(0, customer.getId())
                )
        );

        List<Bill> billList = addPresetBills(customer);

        final List<Transaction> transactionList = new ArrayList<>(
                Arrays.asList(
                        Transaction.beginTransaction().setSource(accountList.get(0)).setDestination(accountList.get(1)).setAmount(1000),
                        Transaction.beginTransaction().setSource(accountList.get(1)).setDestination(accountList.get(0)).setAmount(2000),
                        Transaction.beginTransaction().setSource(accountList.get(0)).setDestination(billList.get(0)).setAmount(billList.get(0).getAmount()),
                        Transaction.beginTransaction().setSource(accountList.get(1)).setDestination(billList.get(2)).setAmount(billList.get(2).getAmount()),
                        Transaction.beginTransaction().setSource(billList.get(1)).setDestination(accountList.get(0)).setAmount(billList.get(1).getAmount())
                )
        );

        accountList.forEach(account -> mAccountDb.add(account));
        mCustomerDb.add(customer);
        nemIdList.forEach(nemId -> mNemIdDb.add(nemId));
        transactionList.forEach(this::addTransaction);
    }

    private List<Bill> addPresetBills(Customer customer) {

        final List<Bill> billList = new ArrayList<>(
                Arrays.asList(
                        new Bill("Bill 1", "1++", true, 10, Calendar.getInstance().getTime(), customer.getId()),
                        new Bill("Bill 2", "2++", false, 20, Calendar.getInstance().getTime(), customer.getId()),
                        new Bill("Bill 3", "3++", true, 30, Calendar.getInstance().getTime(), customer.getId())
                )
        );

        billList.forEach(bill -> mBillDb.add(bill));

        return billList;
    }

    public void updateNemId(NemId nemId) {
        mNemIdDb.update(nemId);
    }
}

