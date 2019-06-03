package com.example.keabank.database;

import android.content.Context;
import android.support.annotation.NonNull;
import com.example.keabank.model.DatabaseItem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

abstract class AbstractDatabase implements Database {
    final String ITEMS_FILE_NAME = "database" + File.separator + getItemsFileName() + ".bin";
    private List<DatabaseItem> mItems;

    AbstractDatabase(Context context) {
        load();
    }

    abstract String getItemsFileName();

    @Override
    public void add(DatabaseItem item) {
        mItems.add(item);
    }

    @Override
    public List<DatabaseItem> readMultiple(@NonNull Predicate filter) {
        //noinspection unchecked
        return (List<DatabaseItem>) mItems.stream().filter(filter).collect(Collectors.toList());
    }

    @Override
    public List<DatabaseItem> readAll() {
        return mItems;
    }

    @Override
    public DatabaseItem read(@NonNull Predicate<DatabaseItem> query) {
        Object[] databaseItems = mItems.stream().filter(query).toArray();

        if (databaseItems.length == 0) {
            return null;
        } else {
            return (DatabaseItem) databaseItems[0];
        }
    }

    @Override
    public int size() {
        return mItems.size();
    }

    @Override
    public void update(int index, DatabaseItem item) {
        mItems.set(index, item);
    }

    @Override
    public void save() {
        File saveDirectory = new File("database");
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
    }

    @Override
    public void load() {
        try (
                FileInputStream fis = new FileInputStream(ITEMS_FILE_NAME);
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            //noinspection unchecked
            mItems = (List<DatabaseItem>) ois.readObject();

        } catch (IOException | ClassNotFoundException e) {
            mItems = new ArrayList<>();
        }
    }
}
