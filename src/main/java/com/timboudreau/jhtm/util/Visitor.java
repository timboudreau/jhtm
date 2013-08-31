package com.timboudreau.jhtm.util;

/**
 * A visitor which will be passed some collection of objects which are
 * iterated over.  In much of this API visitors are used in preference to
 * collections, due to the much smaller memory footprint required.  Most objects
 * passed to vistors are flyweight objects created immediately before the call
 * over a simple data model.
 * <p/>
 * Essentially:  This project involves very large numbers of objects (can be 
 * in the billions) which 
 * frequently only represent a few bits or bytes of information.  If each one
 * were a proper Java object held in memory, there would be a number of
 * consequences:
 * <ul>
 * <li>Every object is, at a minimum, an 8 byte pointer *plus* associated
 * data structures the VM needs, multiplying the memory requirements vastly</li>
 * <Li>Abstractions or not, the best performance can be had by laying out the
 * data to be processed contiguously in memory - you get fewer cache misses
 * and behavior a which is predictable.  Using Java primitive arrays, 
 * BitSets and offsets for all state information is the best way to achieve that</li>
 * </ul>
 * By using a visitor approach, we get all the richness of having the data
 * structures implemented as Java objects - yet if we're iterating synapses,
 * there may only be one synapse instance actually instantiated (all of its
 * state lives elsewhere).
 * <p/>
 * This approach also makes it easy to produce serializable snapshots of the
 * state of the entire system which are much more robust to changes in the
 * Java classes representing those structures, since changing the implementation
 * details of those will not affect the serialization format.
 * 
 * @author Tim Boudreau
 */
public abstract class Visitor<T, R> {

    public Result visit(T obj, R arg) {
        return visit(obj);
    }

    public Result visit(T obj) {
        throw new UnsupportedOperationException("Override something");
    }

    public enum Result {

        DONE,
        NOT_DONE,
        NO_VISITS;
        
        public boolean isDone() {
            return this == DONE;
        }
    }
}
