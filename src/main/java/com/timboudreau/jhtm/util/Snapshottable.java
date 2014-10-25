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
package com.timboudreau.jhtm.util;

import java.io.Serializable;

/**
 * Something whose state can be snapshotted and later restored to
 * a snapshotted state.  Snapshottable objects should contain their
 * entire state in a snapshot.
 *
 * @author Tim Boudreau
 */
public interface Snapshottable<T extends Serializable> {
    /**
     * Take a snapshot.  The snapshot is frozen in time and will not
     * be modified if the source object is modified.
     * @return A snapshot
     */
    public T snapshot();
    /**
     * Restore a previously taken snapshot.  The passed-in snapshot
     * will not be modified by the source object; rather the source object
     * copies this snapshot into its internal state.
     * 
     * @param snapshot
     * @return 
     */
    public T restore(T snapshot);
}
