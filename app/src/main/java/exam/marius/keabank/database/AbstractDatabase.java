package exam.marius.keabank.database;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import exam.marius.keabank.model.DatabaseItem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

abstract class AbstractDatabase<T extends DatabaseItem> implements Database<T> {
    private static final String TAG = "AbstractDatabase";


    static File sFilesDir;

    private final String ITEMS_DIRECTORY = sFilesDir.getAbsolutePath() + File.separator + "database";
    final String ITEMS_FILE_NAME = ITEMS_DIRECTORY +
            File.separator + getItemsFileName() + ".bin";
    private List<T> mItems;

    AbstractDatabase(Context context) {
        if (MainDatabase.DEBUG_NO_PERSIST) {
            mItems = new ArrayList<>();
            save();
        } else {
            load();
        }
    }

    abstract String getItemsFileName();

    @Override
    public void add(T item) {
        load();
        mItems.add(item);
        save();
    }

    public List<T> readMultiple(@NonNull Predicate<T> filter) {
        load();
        return mItems.stream().filter(filter).collect(Collectors.toList());
    }

    @Override
    public List<T> readAll() {
        load();
        return mItems;
    }

    @Override
    public T read(@NonNull Predicate<T> query) {
        load();
        List<T> databaseItems = mItems.stream().filter(query).collect(Collectors.toList());

        if (databaseItems.size() == 0) {
            return null;
        } else {
            return databaseItems.get(0);
        }
    }

    @Override
    public int size() {
        load();
        return mItems.size();
    }

    @Override
    public void update(T item) {
        load();

        int index = mItems.indexOf(item);

        if (index == -1) {
            throw new ArrayIndexOutOfBoundsException("Item not found: " + item.toString());
        }

        mItems.set(index, item);

        save();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void save() {
        File saveDirectory = new File(ITEMS_DIRECTORY);
        saveDirectory.mkdir();
        File saveFile = new File(ITEMS_FILE_NAME);
        if (!saveFile.exists()) {
            try {
                saveFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (
                FileOutputStream fos = new FileOutputStream(ITEMS_FILE_NAME);
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(mItems);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "save: saved list: " + mItems.toString());
    }

    @Override
    public void load() {
        try (
                FileInputStream fis = new FileInputStream(ITEMS_FILE_NAME);
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            //noinspection unchecked
            mItems = (List<T>) ois.readObject();

        } catch (IOException | ClassNotFoundException e) {
            mItems = new ArrayList<>();
            Log.e(TAG, "load: unable to load list", e);
        }

        Log.d(TAG, "load: loaded list: " + mItems.toString());
    }
}
