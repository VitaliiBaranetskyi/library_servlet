package com.github.vitaliibaranetskyi.library.dao.impl.mysql.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Gets disjoint of two sets. One is old set, which needs to be deleted. Second is new set, which needs to be added.
 * They can have common elements, which should be kicked out of update
 *
 * @param <T> type of class in set
 */
public class Disjoint<T> {
    private final List<T> toDelete;
    private final List<T> toAdd;

    /**
     * @param old     list supposed to be deleted
     * @param newList list supposed to be added
     */
    public Disjoint(List<T> old, List<T> newList) {
        toDelete = new ArrayList<>(old);
        toAdd = new ArrayList<>(newList);

        toAdd.removeAll(toDelete);
        toDelete.removeAll(newList);
    }

    public List<T> getToDelete() {
        return toDelete;
    }

    public List<T> getToAdd() {
        return toAdd;
    }
}