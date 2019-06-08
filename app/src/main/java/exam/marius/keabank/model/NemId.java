package exam.marius.keabank.model;

import java.util.Objects;
import java.util.UUID;

public class NemId implements DatabaseItem {
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
}
