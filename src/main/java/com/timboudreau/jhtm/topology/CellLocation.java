package com.timboudreau.jhtm.topology;

/**
 * The location of a cell, incorporating its column's coordinate and
 * the index of it within the column.
 *
 * @author Tim Boudreau
 */
public final class CellLocation<Coordinate> {
    private final int cellIndex;
    private final Coordinate coordinate;

    public CellLocation(int cellIndex, Coordinate coordinate) {
        this.cellIndex = cellIndex;
        this.coordinate = coordinate;
    }
    
    public int index() {
        return cellIndex;
    }
    
    public Coordinate coordinate() {
        return coordinate;
    }
    
    @Override
    public boolean equals(Object o) {
        boolean result = o instanceof CellLocation;
        if (result) {
            CellLocation<?> c = (CellLocation<?>) o;
            result = c.index() == index();
            if (result) {
                result = coordinate().equals(c.coordinate());
            }
        }
        return result;
    }
    
    @Override
    public int hashCode() {
        return (index() + 1) * coordinate().hashCode() * 89821;
    }
    
    @Override
    public String toString() {
        return coordinate() + ":" + index();
    }
}
