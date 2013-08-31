package com.timboudreau.jhtm.topology;

/**
 * What to do with an invalid coordinate - wrap around to the opposite
 * edge, do nothing, bounce, whatever.
 *
 * @author Tim Boudreau
 */
public abstract class EdgeRule<Coordinate> {

    /**
     * A rule which does nothing and allows invalid coordinates
     * @param <T> The type
     * @return A rule
     */
    public static <T> EdgeRule<T> noop() {
        return new Noop<T>();
    }

    /**
     * Adjust the proposed coordinate to fit within the constraints of the
     * extents and this rule
     * @param curr The current location
     * @param proposed The proposed location
     * @param extents The maximum location + 1 in all directions
     * @return A coordinate
     */
    public abstract Coordinate adjust(Coordinate curr, Coordinate proposed, Coordinate extents);

    /**
     * A rule which wraps around to the other side
     * @return A rule
     */
    public static EdgeRule<Coordinate2D> wrap() {
        return new WrapRule();
    }

    /**
     * A rule which constrains coordinates to within the extents
     * @return A rule
     */
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
                return Coordinate2D.valueOf(x, y);
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
                return Coordinate2D.valueOf(x, y);
            }
            return proposed;
        }
    }
}
