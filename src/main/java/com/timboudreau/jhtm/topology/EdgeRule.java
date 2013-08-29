package com.timboudreau.jhtm.topology;

/**
 *
 * @author Tim Boudreau
 */
public abstract class EdgeRule<Coordinate> {

    public static <T> EdgeRule<T> noop() {
        return new Noop<T>();
    }

    public abstract Coordinate adjust(Coordinate curr, Coordinate proposed, Coordinate extents);

    public static EdgeRule<Coordinate2D> wrap() {
        return new WrapRule();
    }

    public static EdgeRule<Coordinate2D> constrain() {
        return new ConstrainRule();
    }

    private static class WrapRule extends EdgeRule<Coordinate2D> {

        @Override
        public Coordinate2D adjust(Coordinate2D curr, Coordinate2D proposed, Coordinate2D extents) {
            int x = proposed.x;
            int y = proposed.y;
            if (x >= extents.x) {
                x = 0;
            }
            if (y >= extents.y) {
                y = 0;
            }
            if (x < 0) {
                x = extents.x - 1;
            }
            if (y < 0) {
                y = extents.y - 1;
            }
            if (x != proposed.x || y != proposed.y) {
                return new Coordinate2D(x, y);
            }
            return proposed;
        }

    }

    private static class Noop<T> extends EdgeRule<T> {
        @Override
        public T adjust(T curr, T proposed, T extents) {
            return proposed;
        }
    }

    private static class ConstrainRule extends EdgeRule<Coordinate2D> {
        @Override
        public Coordinate2D adjust(Coordinate2D curr, Coordinate2D proposed, Coordinate2D extents) {
            int x = proposed.x;
            int y = proposed.y;
            if (x >= extents.x) {
                x = extents.x - 1;
            }
            if (y >= extents.y) {
                y = extents.y - 1;
            }
            if (x < 0) {
                x = 0;
            }
            if (y < 0) {
                y = 0;
            }
            if (x != proposed.x || y != proposed.y) {
                return new Coordinate2D(x, y);
            }
            return proposed;
        }
    }
}
