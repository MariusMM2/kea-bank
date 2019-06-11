# KEA Bank - A financial management app

This app provides simple solutions to common financial needs, such as automatic bill payments, 
monthly money transfers and saving money, by providing a simple to use and intuitive user interface.

## Structure and Logic

A NemID entity contains an id, an username, a password and associated customer id, if applicable.

A Customer entity contains an id, the first and last name, the birthdate, the chosen affiliate and the list of accounts.

An Account entity contains an id, a number, current balance, the customer id and a list of transactions.

A Bill entity contains an id, title and description, amount to pay, the due date, the customer id and whether the bill is 
recurrent or not.

A Transaction entity contains an id, the source and destination targets (Bill or Account), the type (normal, 
paymentservice or mobilepay), title and message, status, amount and date of the transaction. 

When a transaction is created, it will be executed after its date has passed. If the transaction date is less than 
a week ahead, it will be shown in the account details as "pending".

The state of a transaction is updated on each launch of the app. If the app is next launched after more than two months 
in the future, recurrent transactions will be recursively executed for each month until the current month.
Ex: A recurrent transaction is created on 10th of January. 
The next launch of the app is on 11th of April. 
In this case, new transactions will be created for 10th of February, March, April and May, and the former three will also be executed.

When creating a new customer, a Default and Budget account will be created for it. Three bills will also be added to 
that customer. If location permission is provided, the bank affiliate closest to the customer will be assigned*, otherwise
 the affiliate in Copenhagen will be assigned.

By default, the database contains two NemIDs, one Customer, two Accounts, three default Bills and 5 transactions for said customer. 

The database state can be reset by tapping the "DEBUG_RESETDB" button on the login activity.

## Usage

#### Create a customer

1. Start the app.
2. Tap the "Register" button on the login screen.
3. Fill out your details
4. Tap the edit (pencil) icon near the NemID field to add a new NemID or use an existing* one.
5. Tap the "Submit Registration" button.
6. Confirm the creation by tapping "Ok" on the pop-up.
7. You will be asked to provide location permission for the affiliate. You can either provide or deny it.
8. Your account has been created and you will be presented with the home screen.

#### Bill Payment

1. Start the app.
2. Log in**.
3. Tap the "Payments" button at the top of the screen.
4. Choose account to pay from.
5. Choose bill to pay.
6. If the bill is recurrent, you can choose to have it paid monthly by checking the "Register to Payment Service* checkbox.
7. Tap the confirm FAB.
8. Confirm the transaction in the new screen by tapping the FAB.
9. The transaction for that bill has been created and you will be presented with the home screen.

#### Money Transfer

1. Start the app.
2. Log in**.
3. Tap the "Transfer" button at the top of the screen.
4. Choose account to pay from.
5. Choose account to pay to from the dropdown or enter the account number.
6. Choose the desired date, message and amount.
7. Tap the confirm FAB.
8. If transferring to a foreign account or to your pension account, confirm your NemID in the pop-up.
9. If you're trying to withdraw from your pension account before the age of 77, you will be notified of this and no transaction will be made.
10. Confirm the transaction in the new screen by tapping the FAB.
11. The transaction has been created and you will be presented with the home screen.

#### Set up monthly transfers to Budget or Savings accounts

Same as above, but you must choose a Budget or Savings account as destination and check the "Set up as monthly transfer" checkbox.

#### Open a new account

1. Start the app.
2. Log in**.
3. Tap the "New Account" button at the top of the screen.
4. Choose account to create from the list.

#### Change/Reset Password

1. Start the app.
2. Log in**.
3. Tap your user name in the app bar.
4. Fill out the fields with your old and new password.
5. Tap the "Change Password" button.
6. Your new password has been saved.

### Notes:
\* Currently, the Odense affiliate will be assigned if location is provided.

\** The currently available credentials are:  
NemIDs:  
foobar98 - has a linked customer
foobar99 - has no linked customer (can be usen when creating a new customer)

For development purposes, password validation is disabled.
