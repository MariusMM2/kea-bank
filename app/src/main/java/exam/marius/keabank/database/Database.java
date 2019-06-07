package exam.marius.keabank.database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import exam.marius.keabank.model.DatabaseItem;

import java.util.List;
import java.util.function.Predicate;

public interface Database {
    void add(DatabaseItem item);

    List<DatabaseItem> readMultiple(@NonNull Predicate<DatabaseItem> filter);

    List<DatabaseItem> readAll();

    @Nullable
    DatabaseItem read(@NonNull Predicate<DatabaseItem> query);

    int size();

    void update(int index, DatabaseItem item);

    void save();

    void load();
}
