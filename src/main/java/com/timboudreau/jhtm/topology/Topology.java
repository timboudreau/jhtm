package com.timboudreau.jhtm.topology;

import com.timboudreau.jhtm.Column;
import com.timboudreau.jhtm.system.Layer;
import com.timboudreau.jhtm.util.Visitor;
import java.lang.reflect.Array;
import java.util.Random;

/**
 *
 * @author Tim Boudreau
 */
public abstract class Topology<Coordinate> {

    private Class<? extends Path<Coordinate, ? extends Direction<Coordinate>>> pathType;

    protected Topology(Class<? extends Path<Coordinate, ? extends Direction<Coordinate>>> pathType) {
        this.pathType = pathType;
    }

    public abstract <R> Visitor.Result visitNeighbors(Layer layer, int radius, Visitor<Column, R> v, Column column, R arg);

    public abstract Column<Coordinate> getColumn(Layer layer, Coordinate coord);

    public abstract int columnCount();

    public abstract Coordinate coordinateForIndex(int ix);

    public abstract Coordinate getExtents();

    public abstract boolean isValid(Coordinate coord);

    public abstract Visitor.Result walk(Coordinate startPoint, Iterable<? extends Direction<Coordinate>> directions, Visitor<Coordinate, Topology<Coordinate>> visitor);

    public abstract int toIndex(Coordinate coordinate);

    public abstract Path<Coordinate, ? extends Direction<Coordinate>> createRandom(Random r, Coordinate start, int length);

    public final Class<? extends Path<Coordinate, ? extends Direction<Coordinate>>> pathType() {
        return pathType;
    }

    public Object /*Path<Coordinate2D, ? extends Direction<Coordinate2D>>*/ pathArray(int... size) {
        Object result = Array.newInstance(pathType(), size);
//        return (Path<Coordinate2D, ? extends Direction<Coordinate2D>>[]) result;
        return result;
    }
}
