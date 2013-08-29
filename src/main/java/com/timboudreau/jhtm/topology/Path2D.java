package com.timboudreau.jhtm.topology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * A path starting from some 2D coordinates, specified by a set of navigation
 * instructions.
 *
 * @author Tim Boudreau
 */
public class Path2D implements Path<Coordinate2D, Direction2D> {

    private final List<Direction2D> directions = new ArrayList<>();

    public Path2D() {

    }

    Path2D(Collection<Direction2D> directions) {
        this.directions.addAll(directions);
    }

    Path2D(Direction2D... directions) {
        this(Arrays.asList(directions));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Direction2D d : directions) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(d);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Path2D && ((Path2D) o).directions.equals(directions);
    }

    @Override
    public int hashCode() {
        return directions.hashCode();
    }

    public String toString(Coordinate2D start) {
        StringBuilder sb = new StringBuilder(start + "");
        Iterator<Direction2D> it = iterator();
        for (Coordinate2D c : coordinates(start, new Coordinate2D(Integer.MAX_VALUE, Integer.MAX_VALUE), EdgeRule.<Coordinate2D> noop())) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(it.next()).append(" to ").append(c);
        }
        return sb.toString();
    }

    public Path2D add(Direction2D direction) {
        List<Direction2D> directions = new LinkedList<>(this.directions);
        directions.add(direction);
        return new Path2D(directions);
    }

    @Override
    public ListIterator<Direction2D> iterator() {
        return Collections.unmodifiableList(directions).listIterator();
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

        public Iter(Coordinate2D last, Coordinate2D extents, EdgeRule<Coordinate2D> edgeRule) {
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
