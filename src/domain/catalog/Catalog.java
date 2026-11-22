package domain.catalog;

import java.util.ArrayList;
import java.util.List;

// In-memory list of domain objects. App loads them from the db and Console saves the changes
public abstract class Catalog<T> {

    protected abstract List<T> items();

    public void add(T item) {
        if (!items().contains(item)) {
            items().add(item);
        }
    }

    public List<T> getAll() {
        return new ArrayList<>(items());
    }
}
