package exam.marius.keabank.model;

import android.os.Parcel;
import android.os.Parcelable;
import exam.marius.keabank.util.ParcelUtils;

import java.util.Objects;
import java.util.UUID;

public class NemId implements DatabaseItem, Parcelable {
    private UUID mId;
    private String mUsername, mPassword;
    private UUID mCustomerId;

    public NemId(UUID id, String username, String password, UUID customerId) {
        mId = id;
        mUsername = username;
        mPassword = password;
        mCustomerId = customerId;
    }

    public NemId(String username, String password) {
        mId = UUID.randomUUID();
        mUsername = username;
        mPassword = password;
        mCustomerId = UUID.randomUUID();
    }

    @Override
    public UUID getId() {
        return mId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NemId nemId = (NemId) o;
        return mId.equals(nemId.mId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mId);
    }

    public String getUsername() {
        return mUsername;
    }

    public String getPassword() {
        return mPassword;
    }

    public UUID getCustomerId() {
        return mCustomerId;
    }

    public void setCustomerId(UUID customerId) {
        mCustomerId = customerId;
    }

    protected NemId(Parcel in) {
        mId = ParcelUtils.readUuid(in);
        mUsername = in.readString();
        mPassword = in.readString();
        mCustomerId = ParcelUtils.readUuid(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeUuid(dest, mId);
        dest.writeString(mUsername);
        dest.writeString(mPassword);
        ParcelUtils.writeUuid(dest, mCustomerId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<NemId> CREATOR = new Creator<NemId>() {
        @Override
        public NemId createFromParcel(Parcel in) {
            return new NemId(in);
        }

        @Override
        public NemId[] newArray(int size) {
            return new NemId[size];
        }
    };

    public void setPassword(String password) {
        mPassword = password;
    }
}
