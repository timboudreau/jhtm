package com.timboudreau.jhtm.topology;

/**
 *
 * @author Tim Boudreau
 */
public interface Direction<Coordinate> {
    Coordinate navigate(Coordinate extents, Coordinate curr, EdgeRule<Coordinate> edgeRule);

}
