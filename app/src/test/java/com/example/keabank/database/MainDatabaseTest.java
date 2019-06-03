package com.example.keabank.database;

import com.example.keabank.model.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;

public class MainDatabaseTest {

    private final NemId mNemId1a = new NemId("Subject1", "123456");
    private final NemId mNemId1b = new NemId("Subject1", "1234567");
    private final NemId mNemId2 = new NemId("Foo2", "1");
    private final Customer mCustomer1 = new Customer("John", "Doe", "johndoe@email.com", "123456", Calendar.getInstance().getTime());
    private final Customer mCustomer2 = new Customer("Johnny", "Doe", "johndoe@email.com", "123456", Calendar.getInstance().getTime());
    private final List<Account> mAccountList = new ArrayList<>(
            Arrays.asList(
                    Account.newDefault(1000, mCustomer1.getId()),
                    Account.newBudget(1000, mCustomer1.getId())
            )
    );
    private final List<Bill> mBillList = new ArrayList<>(
            Arrays.asList(
                    new Bill("1", "1++", false, 10, Calendar.getInstance().getTime(), mCustomer1.getId()),
                    new Bill("2", "2++", false, 20, Calendar.getInstance().getTime(), mCustomer2.getId()),
                    new Bill("3", "3++", false, 30, Calendar.getInstance().getTime(), mCustomer1.getId())
            )
    );
    private final List<Transaction> mTransactionList = new ArrayList<>(
            Arrays.asList(
                    Transaction.beginTransaction().setSource(mAccountList.get(0)).setDestination(mAccountList.get(1)).setAmount(1000),
                    Transaction.beginTransaction().setSource(mAccountList.get(1)).setDestination(mAccountList.get(0)).setAmount(2000),
                    Transaction.beginTransaction().setSource(mAccountList.get(0)).setDestination(mBillList.get(0)).setAmount(mBillList.get(0).getAmount()),
                    Transaction.beginTransaction().setSource(mAccountList.get(1)).setDestination(mBillList.get(2)).setAmount(mBillList.get(2).getAmount()),
                    Transaction.beginTransaction().setSource(mBillList.get(1)).setDestination(mAccountList.get(0)).setAmount(mBillList.get(1).getAmount())
            )
    );
    private MainDatabase mMainDatabase;

    @Before
    public void setUp() {
        mMainDatabase = new MainDatabase(null);
        MainDatabase.DEBUG_NO_NEMID = false;
        MainDatabase.DEBUG_NO_PASSWORD = true;
        mNemId1a.setCustomerId(mCustomer1.getId());
    }

    @Test
    public void getInstance() {
        assertNotNull(MainDatabase.getInstance(null));
    }

    @Test
    public void tryLogin() {
        MainDatabase.DEBUG_NO_NEMID = true;

        assertTrue(mMainDatabase.tryLogin(new NemId("Foo", "f00")));

        addNemId1a();

        assertNotNull(mMainDatabase.mNemIdDb.read(databaseItem -> ((NemId) databaseItem).getUsername().equals(mNemId1a.getUsername())));
        assertNull(mMainDatabase.mNemIdDb.read(databaseItem -> ((NemId) databaseItem).getUsername().equals(mNemId2.getUsername())));

        assertTrue(mMainDatabase.tryLogin(mNemId1a));
        assertTrue(mMainDatabase.tryLogin(mNemId2));

        MainDatabase.DEBUG_NO_NEMID = false;

        assertTrue(mMainDatabase.tryLogin(mNemId1a));
        assertFalse(mMainDatabase.tryLogin(mNemId2));

        MainDatabase.DEBUG_NO_PASSWORD = false;

        assertTrue(mMainDatabase.tryLogin(mNemId1a));
        assertFalse(mMainDatabase.tryLogin(mNemId1b));
    }

    @Test
    public void getCustomer() {
        addNemId1a();

        try {
            mMainDatabase.getCustomer(mNemId2);
            fail("InvalidNemIDException not thrown");
        } catch (InvalidCustomerException e) {
            fail();
        } catch (InvalidNemIDException ignored) {

        }

        try {
            getCustomer1a();
            fail("InvalidNemIDException not thrown");
        } catch (InvalidCustomerException ignored) {

        } catch (InvalidNemIDException e) {
            fail(e.getMessage());
        }

        mMainDatabase.mCustomerDb.add(mCustomer1);

        Customer retrievedCustomer = null;

        try {
            retrievedCustomer = getCustomer1a();
        } catch (InvalidCustomerException | InvalidNemIDException e) {
            fail(e.getMessage());
        }

        assertNotNull(retrievedCustomer);

        assertEquals(0, retrievedCustomer.getAccountList().size());
        retrievedCustomer.getAccountList().forEach(account -> {
            assertEquals(0, account.getTransactionList().size());
        });

        addAccounts();

        try {
            retrievedCustomer = getCustomer1a();
        } catch (InvalidCustomerException | InvalidNemIDException e) {
            fail(e.getMessage());
        }

        assertEquals(mAccountList.size(), retrievedCustomer.getAccountList().size());
        retrievedCustomer.getAccountList().forEach(account -> {
            assertEquals(0, account.getTransactionList().size());
        });

        mTransactionList.forEach(transaction -> mMainDatabase.mTransactionDb.add(transaction));

        try {
            retrievedCustomer = getCustomer1a();
        } catch (InvalidCustomerException | InvalidNemIDException e) {
            fail(e.getMessage());
        }

        assertEquals(mAccountList.size(), retrievedCustomer.getAccountList().size());
        assertEquals(4, mAccountList.get(0).getTransactionList().size());
        assertEquals(3, mAccountList.get(1).getTransactionList().size());
    }

    @Test
    public void getBills() {
        mBillList.forEach(bill -> mMainDatabase.mBillDb.add(bill));

        List<Bill> bills1 = mMainDatabase.getBills(mCustomer1);
        assertEquals(2, bills1.size());
        List<Bill> bills2 = mMainDatabase.getBills(mCustomer2);
        assertEquals(1, bills2.size());
    }

    @Test
    public void findAccount() {
        addAccounts();
        assertEquals(mAccountList.get(0), mMainDatabase.findAccount(mAccountList.get(0).getId()));
        assertEquals(mAccountList.get(1), mMainDatabase.findAccount(mAccountList.get(1).getId()));
    }

    @Test
    public void persistData() {
        mBillList.forEach(bill -> mMainDatabase.mBillDb.add(bill));

        mMainDatabase.save();
        setUp();


        List<Bill> bills1 = mMainDatabase.getBills(mCustomer1);
        assertEquals(2, bills1.size());
        List<Bill> bills2 = mMainDatabase.getBills(mCustomer2);
        assertEquals(1, bills2.size());
    }

    @After
    public void tearDown() {
        removeDbFile(mMainDatabase.mAccountDb);
        removeDbFile(mMainDatabase.mBillDb);
        removeDbFile(mMainDatabase.mCustomerDb);
        removeDbFile(mMainDatabase.mNemIdDb);
        removeDbFile(mMainDatabase.mTransactionDb);
        //noinspection ResultOfMethodCallIgnored
        new File("database").delete();
    }

    private void removeDbFile(Database database) {
        //noinspection ResultOfMethodCallIgnored
        new File(((AbstractDatabase) database).ITEMS_FILE_NAME).delete();
    }

    private Customer getCustomer1a() throws InvalidNemIDException, InvalidCustomerException {
        return mMainDatabase.getCustomer(mNemId1a);
    }

    private void addNemId1a() {
        mMainDatabase.mNemIdDb.add(mNemId1a);
    }

    private void addAccounts() {
        mAccountList.forEach(account -> mMainDatabase.mAccountDb.add(account));
    }
}