/* 
 * Copyright (C) 2014 Tim Boudreau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
        return new Noop<>();
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
