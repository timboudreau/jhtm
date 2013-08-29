package com.timboudreau.jhtm.topology;

import java.io.Serializable;

/**
 * A sequence of instructions for navigating a topology.
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
    Iterable<Coordinate> coordinates(Coordinate start, Coordinate extents, EdgeRule<Coordinate> edgeRule);
}
