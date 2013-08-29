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
