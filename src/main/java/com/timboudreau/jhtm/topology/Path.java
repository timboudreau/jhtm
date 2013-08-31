package com.timboudreau.jhtm.topology;

import java.io.Serializable;

/**
 * Am immutable sequence of instructions for navigating a topology.
 *
 * @author Tim Boudreau
 */
public interface Path<Coordinate, Dir extends Direction<Coordinate>> extends Iterable<Dir>, Serializable {
    /**
     * Add a path, returning a new instance of Path with the passed
     * direction added.  Does not alter the instance this method is called on.
     * @param dir
     * @return 
     */
    Path add(Dir dir);
    /**
     * Iterate this path as a set of coordinates atrting at the passed
     * start coordinate, applying the edge rule in the context of the
     * extents.
     * 
     * @param start The starting location
     * @param extents The maximum location all valid locations are less than
     * @param edgeRule The rule for what to do if the path specifies an invalid
     * location
     * @return An iterable 
     */
    Iterable<Coordinate> coordinates(Coordinate start, Coordinate extents, EdgeRule<Coordinate> edgeRule);
    int length();
}
