/* 
 * Copyright (C) 2014 Tim Boudreau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
