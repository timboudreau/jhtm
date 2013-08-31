package com.timboudreau.jhtm.topology;

import com.timboudreau.jhtm.Column;
import com.timboudreau.jhtm.system.Layer;
import com.timboudreau.jhtm.util.Visitor;
import java.lang.reflect.Array;
import java.util.Random;

/**
 * Maps a linear list of columns and friends into a topology with some sort of
 * coordinates, such as 2D and x,y coordinates, or whatever is useful.
 * <p/>
 * Abstracts out assumptions about topology, so that internal code does not wind
 * up with, say, algorithms for deciding what cells are neighbors hard-coded
 * into it.
 *
 * @author Tim Boudreau
 */
public abstract class Topology<Coordinate> {

    private Class<? extends Path<Coordinate, ? extends Direction<Coordinate>>> pathType;

    /**
     * Create a new topology.
     *
     * @param pathType The path type, for navigating the topology via lists of
     * directions (i.e. left, up, down).
     */
    protected Topology(Class<? extends Path<Coordinate, ? extends Direction<Coordinate>>> pathType) {
        this.pathType = pathType;
    }

    /**
     * Visit all the n
     *
     * @param <R>
     * @param layer
     * @param radius
     * @param v
     * @param column
     * @param arg
     * @return
     */
    public abstract <R> Visitor.Result visitNeighbors(Layer<Coordinate> layer, int radius, Visitor<Column, R> v, Column column, R arg);

    /**
     * Get a column located at a given coordinate in a given layer
     *
     * @param layer
     * @param coord
     * @return
     */
    public Column<Coordinate> getColumn(Layer<Coordinate> layer, Coordinate coord) {
        return layer.getColumn(toIndex(coord));
    }

    /*
     * Get the number of columns in this topology
     */
    public abstract int columnCount();

    /**
     * Get the coordinate for a given offset into the array of columns
     *
     * @param ix An index
     * @return A coordinate
     */
    public abstract Coordinate coordinateForIndex(int ix);

    /**
     * Get the maximum coordinate component values, exclusive, as a coordinate.
     * This is a coordinate which is one unit past the largest possible
     * coordinate in this topology - all valid coordinates are less than this.
     *
     * @return The extents
     */
    public abstract Coordinate getExtents();

    /**
     * Determine if the coordinate is valid in this topology - greater than zero
     * for its components and less than getExtents().
     *
     * @param coord a coordinate
     * @return true if it is a usable coordinate
     */
    public abstract boolean isValid(Coordinate coord);

    /**
     * Walk the coordinates starting with the passed path object, using the
     * passed directions to navigate and applying this topology's edge rule, if
     * any.
     *
     * @param startPoint The initial point - will not be included in the output
     * @param directions An iterable of directions, such as a Path, which
     * describes offsets from the current location
     * @param visitor A visitor which will be called for each location
     * @return The result of visiting
     */
    public abstract Visitor.Result walk(Coordinate startPoint, Iterable<? extends Direction<Coordinate>> directions, Visitor<Coordinate, Topology<Coordinate>> visitor);

    /**
     * Convert a coordinate to a linear column index
     *
     * @param coordinate A coordinate
     * @return An index
     */
    public abstract int toIndex(Coordinate coordinate);

    /**
     * Create a random path starting from the passed coordinate, of length
     * length
     *
     * @param r A random
     * @param start The start location
     * @param length The number of coordinates in the resulting path
     * @return A path
     */
    public abstract Path<Coordinate, ? extends Direction<Coordinate>> createRandom(Random r, Coordinate start, int length);

    /**
     * Get the concrete type of Path which is returned by things like
     * createRandom(). Needed for constructing arrays of this component type.
     *
     * @return A path
     */
    public final Class<? extends Path<Coordinate, ? extends Direction<Coordinate>>> pathType() {
        return pathType;
    }

    /**
     * Create an array of Path objects of the right type and the passed
     * dimensions
     *
     * @param size
     * @return
     */
    public Object /*Path<Coordinate2D, ? extends Direction<Coordinate2D>>*/ pathArray(int... size) {
        assert size.length > 0;
        Object result = Array.newInstance(pathType(), size);
//        return (Path<Coordinate2D, ? extends Direction<Coordinate2D>>[]) result;
        return result;
    }
}
