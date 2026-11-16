package domain.catalog;

import java.util.ArrayList;
import java.util.List;

// In-memory list of domain objects (to be modified when sqlite is implemented)
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
