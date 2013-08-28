package com.timboudreau.jhtm.impl;

import com.timboudreau.jhtm.impl.DendritePath.Direction;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;

/**
 * A path starting from some 2D coordinates, specified by a set of navigation
 * instructions.
 *
 * @author Tim Boudreau
 */
class DendritePath implements Iterable<Direction>, Serializable {

    private final List<Direction> directions = new ArrayList<>();

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Direction d : directions) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(d);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DendritePath && ((DendritePath) o).directions.equals(directions);
    }

    @Override
    public int hashCode() {
        return directions.hashCode();
    }

    public String toString(Coordinate2D start) {
        StringBuilder sb = new StringBuilder(start + "");
        for (Direction d : directions) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            start = start.adjustedBy(d);
            sb.append(d).append(" to ").append(start);
        }
        return sb.toString();
    }

    static DendritePath createRandom(Random r, Coordinate2D start, Topology2D topology, int length) {
        DendritePath result = new DendritePath();
        Set<Coordinate2D> seen = new HashSet<>(Arrays.asList(start));
        outer:
        for (int i = 0; i < length; i++) {
            boolean valid;
            Direction d;
            Coordinate2D nue = null;
            Set<Direction> tried = EnumSet.noneOf(Direction.class);
            do {
                d = Direction.random(r);
                tried.add(d);
                nue = start.adjustedBy(d);
                valid = topology.isValid(nue) && !seen.contains(nue);
                if (!valid && tried.size() == Direction.values().length) {
//                    System.out.println("Give up at " + result.directions.size() + " seen " + seen + " tried " + tried + " last " + nue);
                    // nowhere to go
                    // PENDING:  Could try to decrement i
                    break outer;
                }
            } while (!valid);
            seen.add(nue);
            result.add(d);
        }
        return result;
    }

    void add(Direction direction) {
        directions.add(direction);
    }

    @Override
    public ListIterator<Direction> iterator() {
        return directions.listIterator();
    }

    public enum Direction {

        UP,
        DOWN,
        LEFT,
        RIGHT,
        UP_RIGHT,
        DOWN_RIGHT,
        UP_LEFT,
        DOWN_LEFT;

        public static Direction random(Random r) {
            return values()[r.nextInt(values().length)];
        }

        public Coordinate2D next(Topology2D topology, Coordinate2D curr) {
            int x = adjustX(curr.x, topology.width - 1);
            int y = adjustY(curr.y, topology.height - 1);
            return new Coordinate2D(x, y);
        }

        int adjustX(int x, int maxX) {
            switch (this) {
                case LEFT:
                case UP_LEFT:
                case DOWN_LEFT:
                    return Math.min(maxX, Math.max(x + 1, 0));
                case RIGHT:
                case UP_RIGHT:
                case DOWN_RIGHT:
                    return Math.min(maxX, Math.max(x + 1, 0));
                default:
                    return x;
            }
        }

        int adjustY(int y, int maxY) {
            switch (this) {
                case UP:
                case UP_RIGHT:
                case UP_LEFT:
                    return Math.min(maxY, Math.max(y - 1, 0));
                case DOWN:
                case DOWN_RIGHT:
                case DOWN_LEFT:
                    return Math.min(maxY, Math.max(y + 1, 0));
                default:
                    return y;
            }
        }
    }
}
