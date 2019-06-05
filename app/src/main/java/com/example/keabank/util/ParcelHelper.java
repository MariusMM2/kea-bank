package com.example.keabank.util;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A wrapping class for certain Parcel read/write operations
 */
public class ParcelHelper {
    private ParcelHelper() {
    }

    /**
     * Writes an UUID object to a target Parcel
     *
     * @param dest The target parcel
     * @param uuid The UUID to be written
     */
    public static void writeUuid(Parcel dest, UUID uuid) {
        dest.writeLong(uuid.getMostSignificantBits());
        dest.writeLong(uuid.getLeastSignificantBits());
    }

    /**
     * Reads the next UUID Object from a given Parcel
     *
     * @param in The Parcel to read from
     * @return The read UUID object
     */
    public static UUID readUuid(Parcel in) {
        long mostSigBits = in.readLong();
        long leastSigBits = in.readLong();
        return new UUID(mostSigBits, leastSigBits);
    }

    /**
     * Writes a List object to a target Parcel
     *
     * @param dest The target parcel
     * @param list The List to be written
     */
    public static void writeList(Parcel dest, List list) {
        if (list == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(list);
        }
    }

    /**
     * Reads the next List object from a given Parcel
     * as an ArrayList
     *
     * @param in        The Parcel to read from
     * @param itemClass The class of the items in the list
     * @param <T>       The type of the items in the list
     * @return The read ArrayList
     */
    public static <T extends Parcelable> List<T> readList(Parcel in, Class<T> itemClass) {
        List<T> list;
        if (in.readByte() == 0x01) {
            list = new ArrayList<>();
            in.readList(list, itemClass.getClassLoader());
        } else {
            list = null;
        }
        return list;
    }

    /**
     * Writes a List of UUIDs object to a target Parcel
     *
     * @param dest The target parcel
     * @param list The List to be written
     */
    public static void writeUuidList(Parcel dest, List<UUID> list) {
        if (list == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(list.size());

            for (UUID uuid : list) {
                ParcelHelper.writeUuid(dest, uuid);
            }
        }
    }

    /**
     * Reads the next List of UUIDs object from a given Parcel
     * as an ArrayList
     *
     * @param in The Parcel to read from
     * @return The read ArrayList
     */
    public static List<UUID> readUuidList(Parcel in) {
        List<UUID> list = new ArrayList<>();
        if (in.readByte() == 0x01) {
            int listSize = in.readInt();

            for (int i = 0; i < listSize; i++) {
                UUID uuid = ParcelHelper.readUuid(in);
                list.add(uuid);
            }
        }
        return list;
    }

    /**
     * @param enumVariable
     * @param <T>
     */
    public static <T extends Enum<T>> void writeEnum(Parcel dest, T enumVariable) {
        dest.writeString(enumVariable.name());
    }

    public static <T extends Enum<T>> T readEnum(Parcel in, Class<T> enumType) {
        return T.valueOf(enumType, in.readString());
    }
}
