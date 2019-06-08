package exam.marius.keabank.model;

import android.os.Parcel;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.fail;

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
            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {

            }

            @Override
            public UUID getId() {
                return null;
            }

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

            @Override
            public String getTitle() {
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void prepareParcel() {

            }
        };
        mTransactionTargetNat = new TransactionTarget() {
            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {

            }

            @Override
            public UUID getId() {
                return null;
            }

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

            @Override
            public String getTitle() {
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void prepareParcel() {

            }
        };
    }

    @Test
    public void setSource() {
        try {
            //noinspection ConstantConditions
            mTransaction1.setSource(null);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException ignored) {

        }

        mTransaction1.setSource(mTransactionTargetInt);

        try {
            mTransaction1.setSource(mTransactionTargetNat);
            fail("IllegalStateException not thrown");
        } catch (IllegalStateException ignored) {

        }
    }

    @Test
    public void setDestination() {
        try {
            //noinspection ConstantConditions
            mTransaction1.setDestination(null);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException ignored) {

        }

        mTransaction1.setDestination(mTransactionTargetInt);

        try {
            mTransaction1.setDestination(mTransactionTargetNat);
            fail("IllegalStateException not thrown");
        } catch (IllegalStateException ignored) {

        }
    }

    @Test
    public void setAmount() {
        try {
            mTransaction1.setAmount(0);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException ignored) {

        }
        try {
            mTransaction1.setAmount(-100);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException ignored) {

        }

        mTransaction1.setAmount(1);

        try {
            mTransaction1.setAmount(2);
            fail("IllegalStateException not thrown");
        } catch (IllegalStateException ignored) {

        }
    }

    @Test
    public void commit() {
        try {
            mTransaction1.commit();
            fail("TransactionException not thrown");
        } catch (TransactionException ignored) {
        }

        try {
            mTransaction1.setSource(mTransactionTargetInt)
                    .commit();
            fail("TransactionException not thrown");
        } catch (TransactionException ignored) {
        }

        try {
            mTransaction1.setDestination(mTransactionTargetInt)
                    .commit();
            fail("TransactionException not thrown");
        } catch (TransactionException ignored) {
        }

        try {
            mTransaction2.setSource(mTransactionTargetNat)
                    .setDestination(mTransactionTargetNat)
                    .setAmount(mTransactionTargetNat.getAmount() + 1)
                    .commit();
            fail("TransactionException not thrown");
        } catch (TransactionException ignored) {
        }

        try {
            mTransaction1.setAmount(1000)
                    .commit();
        } catch (TransactionException e) {
            fail(e.getMessage());
        }

        try {
            mTransaction1.commit();
            fail("TransactionException not thrown");
        } catch (TransactionException ignored) {
        }

        try {
            mTransaction3.setSource(mTransactionTargetInt)
                    .setDestination(mTransactionTargetNat)
                    .setAmount(mTransactionTargetInt.getAmount() + 100)
                    .commit();
        } catch (TransactionException e) {
            fail(e.getMessage());
        }
    }
}