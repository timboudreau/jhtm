package com.timboudreau.jhtm.topology;

/**
 *
 * @author Tim Boudreau
 */
public class Coordinate2D {

    public final int x;
    public final int y;

    public Coordinate2D(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public int x() {
        return x;
    }
    
    public int y() {
        return y;
    }
    
    @Override
    public boolean equals(Object o) {
        return o instanceof Coordinate2D && ((Coordinate2D) o).x == x && ((Coordinate2D) o).y == y;
    }

    @Override
    public int hashCode() {
        int base = (65536 * y) + x;
        return base;
    }

    @Override
    public String toString() {
        return x + "," + y;
    }

    public int toOffset(Topology2D top) {
        int maxX = top.width - 1;
        return (maxX * y) + x;
    }
}
