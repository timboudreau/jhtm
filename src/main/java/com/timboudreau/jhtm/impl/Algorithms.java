package com.timboudreau.jhtm.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Pluggable logic
 *
 * @author Tim Boudreau
 */
public class Algorithms {

    private final List<Entry<?>> entries = new LinkedList<>();

    public <T> Algorithms put(Class<T> type, T instance) {
        for (Iterator<Entry<?>> it = entries.iterator(); it.hasNext();) {
            if (type == it.next().type) {
                it.remove();
            }
        }
        entries.add(new Entry(type, instance));
        return this;
    }

    public <T> T get(Class<T> type) {
        for (Entry<?> e : entries) {
            if (e.type == type) {
                return type.cast(e.impl);
            }
        }
        throw new IllegalArgumentException("No entry for " + type);
    }

    private static class Entry<T> {

        private final Class<T> type;
        private final T impl;

        Entry(Class<T> type, T impl) {
            this.type = type;
            this.impl = impl;
        }
    }
}
