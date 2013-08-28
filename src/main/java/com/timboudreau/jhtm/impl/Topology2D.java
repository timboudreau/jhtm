package com.timboudreau.jhtm.impl;

import com.timboudreau.jhtm.Column;
import com.timboudreau.jhtm.impl.DendritePath.Direction;
import com.timboudreau.jhtm.system.Layer;
import com.timboudreau.jhtm.system.Topology;
import com.timboudreau.jhtm.util.Visitor;

/**
 *
 * @author Tim Boudreau
 */
public class Topology2D implements Topology<Coordinate2D> {

    public final int width;
    public final int height;
    private final ColumnMapper mapper;

    public Topology2D(int widthAndHeight, ColumnMapper mapper) {
        this(widthAndHeight, widthAndHeight, mapper);
    }

    public Topology2D(int width, int height, ColumnMapper mapper) {
        this.width = width;
        this.height = height;
        this.mapper = mapper;
    }

    public Coordinate2D newCoordinate(int x, int y) {
        return new Coordinate2D(x, y);
    }

    Coordinate2D coordinateForColumnIndex(int ix) {
        int y = ix / width;
        int x = ix % width;
        return new Coordinate2D(x, y);
    }

    public boolean isValid(Coordinate2D coord) {
        return coord.x >= 0 && coord.y >= 0 && coord.x < width && coord.y < height;
    }

    public Visitor.Result walk(Coordinate2D startPoint, Iterable<Direction> directions, Visitor<Coordinate2D, Topology2D> visitor) {
        Visitor.Result result = Visitor.Result.NO_VISITS;
        for (Direction d : directions) {
            startPoint = d.next(this, startPoint);
            result = visitor.visit(startPoint, this);
            if (result.isDone()) {
                break;
            }
        }
        return result;
    }

    public <R> Visitor.Result visitNeighbors(Layer layer, int radius, Visitor<Column, R> v, Column column, R arg) {
        Visitor.Result result = Visitor.Result.NO_VISITS;
        Coordinate2D loc = mapper.coordinatesOf(column);
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

    public Column getColumn(Layer layer, Coordinate2D coord) {
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

    public interface ColumnMapper {

        Coordinate2D coordinatesOf(Column cell);
    }
}
