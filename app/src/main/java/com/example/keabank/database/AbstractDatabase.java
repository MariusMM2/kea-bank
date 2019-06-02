package com.example.keabank.database;

import android.content.Context;
import com.example.keabank.model.DatabaseItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class AbstractDatabase implements Database {
    List<DatabaseItem> mItems;

    AbstractDatabase(Context context) {
        mItems = new ArrayList<>();
    }

    @Override
    public DatabaseItem[] readAll() {
        return mItems.toArray(new DatabaseItem[]{});
    }

    public DatabaseItem read(int i) {
        return mItems.get(i);
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
        // Save to serializable
    }

    void addAll(Collection<DatabaseItem> newItems) {
        mItems.addAll(newItems);
    }
}
