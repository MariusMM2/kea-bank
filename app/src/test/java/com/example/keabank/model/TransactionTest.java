package com.example.keabank.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TransactionTest {

    private Transaction mTransaction1;
    private Transaction mTransaction2;
    private Transaction mTransaction3;

    private TransactionTarget mTransactionTargetInt;
    private TransactionTarget mTransactionTargetNat;

    @Before
    public void startup() {
        mTransaction1 = Transaction.beginTransaction();
        mTransaction2 = Transaction.beginTransaction();
        mTransaction3 = Transaction.beginTransaction();

        mTransactionTargetInt = new TransactionTarget() {
            private float amount = 30;

            @Override
            public float getAmount() {
                return amount;
            }

            @Override
            public boolean canSubtractAmount(float amount) {
                return this.amount - amount > 0;
            }

            @Override
            public void subtract(float amount) {
                this.amount = this.amount - amount;
            }

            @Override
            public void increase(float amount) {
                this.amount = this.amount + amount;
            }

            @Override
            public boolean canGoNegative() {
                return true;
            }
        };
        mTransactionTargetNat = new TransactionTarget() {
            private float amount = 200;

            @Override
            public float getAmount() {
                return amount;
            }

            @Override
            public boolean canSubtractAmount(float amount) {
                return this.amount - amount > 0;
            }

            @Override
            public void subtract(float amount) {
                this.amount = this.amount - amount;
            }

            @Override
            public void increase(float amount) {
                this.amount = this.amount + amount;
            }

            @Override
            public boolean canGoNegative() {
                return false;
            }
        };
    }

    @Test
    public void setSource() {
        try {
            //noinspection ConstantConditions
            mTransaction1.setSource(null);
            Assert.fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException ignored) {

        }

        mTransaction1.setSource(mTransactionTargetInt);

        try {
            mTransaction1.setSource(mTransactionTargetNat);
            Assert.fail("IllegalStateException not thrown");
        } catch (IllegalStateException ignored) {

        }
    }

    @Test
    public void setDestination() {
        try {
            //noinspection ConstantConditions
            mTransaction1.setDestination(null);
            Assert.fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException ignored) {

        }

        mTransaction1.setDestination(mTransactionTargetInt);

        try {
            mTransaction1.setDestination(mTransactionTargetNat);
            Assert.fail("IllegalStateException not thrown");
        } catch (IllegalStateException ignored) {

        }
    }

    @Test
    public void setAmount() {
        try {
            mTransaction1.setAmount(0);
            Assert.fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException ignored) {

        }
        try {
            mTransaction1.setAmount(-100);
            Assert.fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException ignored) {

        }

        mTransaction1.setAmount(1);

        try {
            mTransaction1.setAmount(2);
            Assert.fail("IllegalStateException not thrown");
        } catch (IllegalStateException ignored) {

        }
    }

    @Test
    public void commit() {
        try {
            mTransaction1.commit();
            Assert.fail("TransactionException not thrown");
        } catch (TransactionException ignored) {
        }

        try {
            mTransaction1.setSource(mTransactionTargetInt)
                    .commit();
            Assert.fail("TransactionException not thrown");
        } catch (TransactionException ignored) {
        }

        try {
            mTransaction1.setDestination(mTransactionTargetInt)
                    .commit();
            Assert.fail("TransactionException not thrown");
        } catch (TransactionException ignored) {
        }

        try {
            mTransaction2.setSource(mTransactionTargetNat)
                    .setDestination(mTransactionTargetNat)
                    .setAmount(mTransactionTargetNat.getAmount() + 1)
                    .commit();
            Assert.fail("TransactionException not thrown");
        } catch (TransactionException ignored) {
        }

        try {
            mTransaction1.setAmount(1000)
                    .commit();
        } catch (TransactionException e) {
            Assert.fail(e.getMessage());
        }

        try {
            mTransaction1.commit();
            Assert.fail("TransactionException not thrown");
        } catch (TransactionException ignored) {
        }

        try {
            mTransaction3.setSource(mTransactionTargetInt)
                    .setDestination(mTransactionTargetNat)
                    .setAmount(mTransactionTargetInt.getAmount() + 100)
                    .commit();
        } catch (TransactionException e) {
            Assert.fail(e.getMessage());
        }
    }
}