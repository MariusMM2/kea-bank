package exam.marius.keabank.database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import exam.marius.keabank.model.DatabaseItem;

import java.util.List;
import java.util.function.Predicate;

public interface Database<T extends DatabaseItem> {
    void add(T item);

    List<T> readMultiple(@NonNull Predicate<T> filter);

    List<T> readAll();

    @Nullable
    T read(@NonNull Predicate<T> query);

    int size();

    void update(T item);

    void save();

    void load();
}
