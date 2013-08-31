package com.timboudreau.jhtm.topology;

/**
 * An instruction for navigating away from one location to another in
 * a topology.  For example, left, right, up, down.
 *
 * @author Tim Boudreau
 */
public interface Direction<Coordinate> {
    /**
     * Return a coordinate that reflects adjusting the passed current
     * coordinate by this direction, and applying the passed edge rule.
     * 
     * @param extents The coordinate which valid coordinates must be less than in
     * all directions
     * @param curr The current coordinate
     * @param edgeRule A rule for what to do when a coordinate does not fit within
     * the extents that were passed
     * @return A coordinate
     */
    Coordinate navigate(Coordinate extents, Coordinate curr, EdgeRule<Coordinate> edgeRule);
}
