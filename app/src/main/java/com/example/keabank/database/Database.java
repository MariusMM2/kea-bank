package com.example.keabank.database;

import com.example.keabank.model.DatabaseItem;

public interface Database {
    DatabaseItem[] readAll();

    DatabaseItem read(int i);

    int size();

    void update(int index, DatabaseItem item);

    void save();
}
