package com.example.keabank.model;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class Customer implements DatabaseItem, Serializable {
    private UUID mId;
    private String mFirstName, mLastName, mEmail, mPassword;
    private Date mBirthDate;
    private transient List<Account> mAccountList;

    public Customer(UUID id, String firstName, String lastName, String email, String password, Date birthDate) {
        mId = id;
        mFirstName = firstName;
        mLastName = lastName;
        mEmail = email;
        mPassword = password;
        mBirthDate = birthDate;
        mAccountList = new ArrayList<>();
    }

    public Customer(String firstName, String lastName, String email, String password, Date birthDate) {
        this(UUID.randomUUID(), firstName, lastName, email, password, birthDate);
    }

    public static Customer getDummyCustomer() {
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

    @Override
    public UUID getId() {
        return mId;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getPassword() {
        return mPassword;
    }

    public Date getBirthDate() {
        return mBirthDate;
    }

    public List<Account> getAccountList() {
        return mAccountList;
    }

    public void addAccount(Account account) {
        mAccountList.add(account);
    }

    public void removeAccounts() {
        mAccountList = new ArrayList<>();
    }
}
