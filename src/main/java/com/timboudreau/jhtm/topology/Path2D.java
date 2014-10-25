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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A path starting from some 2D coordinates, specified by a set of navigation
 * instructions.
 *
 * @author Tim Boudreau
 */
public class Path2D implements Path<Coordinate2D, Direction2D> {

    private final byte[] directions;

    public Path2D() {
        this.directions = new byte[0];
    }

    Path2D(List<Direction2D> directions) {
        this.directions = new byte[directions.size()];
        for (int i = 0; i < this.directions.length; i++) {
            this.directions[i] = directions.get(i).toByte();
        }
    }

    Path2D(Direction2D... directions) {
        this.directions = new byte[directions.length];
        for (int i = 0; i < directions.length; i++) {
            this.directions[i] = directions[i].toByte();
        }
    }

    Path2D(byte[] bytes) {
        this.directions = bytes.clone();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Direction2D d : directions()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(d);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Path2D && Arrays.equals(((Path2D) o).directions, directions);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(directions);
    }

    public String toString(Coordinate2D start) {
        StringBuilder sb = new StringBuilder(start + "");
        Iterator<Direction2D> it = iterator();
        for (Coordinate2D c : coordinates(start, Coordinate2D.valueOf(Integer.MAX_VALUE, Integer.MAX_VALUE), EdgeRule.<Coordinate2D>noop())) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(it.next()).append(" to ").append(c);
        }
        return sb.toString();
    }

    public List<Direction2D> directions() {
        List<Direction2D> result = new ArrayList<>(directions.length);
        for (int i = 0; i < directions.length; i++) {
            result.add(Direction2D.fromByte(directions[i]));
        }
        return result;
    }

    public int length() {
        return directions.length;
    }

    public Path2D add(Direction2D direction) {
        byte[] b = new byte[length() + 1];
        System.arraycopy(directions, 0, b, 0, directions.length);
        b[b.length - 1] = direction.toByte();
        return new Path2D(b);
    }

    @Override
    public ListIterator<Direction2D> iterator() {
        return directions().listIterator();
    }

    @Override
    public Iterable<Coordinate2D> coordinates(Coordinate2D start, Coordinate2D extents, EdgeRule<Coordinate2D> edgeRule) {
        return new Iter(start, extents, edgeRule);
    }

    private class Iter implements Iterator<Coordinate2D>, Iterable<Coordinate2D> {

        private final Iterator<Direction2D> iter = Path2D.this.iterator();
        private Coordinate2D last;
        private final Coordinate2D extents;
        private final EdgeRule<Coordinate2D> edgeRule;

        Iter(Coordinate2D last, Coordinate2D extents, EdgeRule<Coordinate2D> edgeRule) {
            this.last = last;
            this.extents = extents;
            this.edgeRule = edgeRule;
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public Coordinate2D next() {
            return last = iter.next().navigate(extents, last, edgeRule);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<Coordinate2D> iterator() {
            return this;
        }
    }
}
