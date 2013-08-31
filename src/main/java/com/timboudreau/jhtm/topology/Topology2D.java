package com.timboudreau.jhtm.topology;

import com.timboudreau.jhtm.Column;
import com.timboudreau.jhtm.system.Layer;
import com.timboudreau.jhtm.util.Visitor;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author Tim Boudreau
 */
public class Topology2D extends Topology<Coordinate2D> {

    public final int width;
    public final int height;
    private final EdgeRule<Coordinate2D> edgeRule;

    public Topology2D(int widthAndHeight) {
        this(widthAndHeight, EdgeRule.wrap());
    }

    public Topology2D(int widthAndHeight, EdgeRule<Coordinate2D> edgeRule) {
        this(widthAndHeight, widthAndHeight, edgeRule);
    }

    public Topology2D(int width, int height) {
        this(width, height, EdgeRule.wrap());
    }

    public Topology2D(int width, int height, EdgeRule<Coordinate2D> edgeRule) {
        super(Path2D.class);
        this.width = width;
        this.height = height;
        this.edgeRule = edgeRule;
    }

    public Coordinate2D newCoordinate(int x, int y) {
        return Coordinate2D.valueOf(x, y);
    }

    @Override
    public Coordinate2D coordinateForIndex(int ix) {
        int y = ix / width;
        int x = ix % width;
        return Coordinate2D.valueOf(x, y);
    }

    public int toIndex(Coordinate2D coord) {
        return (coord.y * width) + coord.x;
    }

    public boolean isValid(Coordinate2D coord) {
        return coord.x >= 0 && coord.y >= 0 && coord.x < width && coord.y < height;
    }

    public Coordinate2D getExtents() {
        return Coordinate2D.valueOf(width, height);
    }

    public <R> Visitor.Result visitNeighbors(Layer<Coordinate2D> layer, int radius, Visitor<Column, R> v, Column column, R arg) {
        Visitor.Result result = Visitor.Result.NO_VISITS;
        Coordinate2D loc = coordinateForIndex(column.index());
        for (int x = loc.x - radius; x < loc.x + radius; x++) {
            if (x < 0 || x >= width) {
                continue;
            }
            for (int y = loc.y - radius; y < loc.y + radius; y++) {
                if (y < 0 || y >= height) {
                    continue;
                }
                Column c = getColumn(layer, loc);
                result = v.visit(c, arg);
                if (result.isDone()) {
                    break;
                }
            }
        }
        return result;
    }

    public Column<Coordinate2D> getColumn(Layer layer, Coordinate2D coord) {
        int offset = coord.toOffset(this);
        if (offset > layer.size() || offset < 0) {
            return null;
        }
        return layer.getColumn(offset);
    }

    @Override
    public int columnCount() {
        return this.width * this.height;
    }

    @Override
    public Visitor.Result walk(Coordinate2D startPoint, Iterable<? extends Direction<Coordinate2D>> directions, Visitor<Coordinate2D, Topology<Coordinate2D>> visitor) {
        Visitor.Result result = Visitor.Result.NO_VISITS;
        Coordinate2D extents = this.getExtents();
        for (Direction<Coordinate2D> d : directions) {
            startPoint = d.navigate(extents, startPoint, edgeRule);
            result = visitor.visit(startPoint, this);
            if (result.isDone()) {
                break;
            }
        }
        return result;
    }

    @Override
    public Path<Coordinate2D, ? extends Direction<Coordinate2D>> createRandom(Random r, Coordinate2D start, int length) {
        Set<Coordinate2D> seen = new HashSet<>(Arrays.asList(start));
        List<Direction2D> directions = new LinkedList<>();
        outer:
        for (int i = 0; i < length; i++) {
            boolean valid;
            Direction2D d;
            Coordinate2D nue = null;
            Set<Direction2D> tried = EnumSet.noneOf(Direction2D.class);
            do {
                d = Direction2D.random(r);
                tried.add(d);
//                nue = start.adjustedBy(d);
                nue = d.navigate(getExtents(), start, edgeRule);
                valid = isValid(nue) && !seen.contains(nue);
                if (!valid && tried.size() == Direction2D.values().length) {
//                    System.out.println("Give up at " + result.directions.size() + " seen " + seen + " tried " + tried + " last " + nue);
                    // nowhere to go
                    // PENDING:  Could try to decrement i
                    break outer;
                }
            } while (!valid);
            seen.add(nue);
            start = nue;
//            result.add(d);
            directions.add(d);
        }
        return new Path2D(directions);
    }
}
