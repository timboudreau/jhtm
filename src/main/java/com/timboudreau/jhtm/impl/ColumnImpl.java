package com.timboudreau.jhtm.impl;

import com.timboudreau.jhtm.Cell;
import com.timboudreau.jhtm.Column;
import com.timboudreau.jhtm.ProximalDendriteSegment;
import com.timboudreau.jhtm.Region;
import com.timboudreau.jhtm.util.Visitor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Tim Boudreau
 */
class ColumnImpl<Coordinate> implements Column<Coordinate> {

    final int index;
    final LayerImpl<Coordinate> outer;

    ColumnImpl(int index, final LayerImpl<Coordinate> outer) {
        this.outer = outer;
        this.index = index;
    }

    @Override
    public int index() {
        return index;
    }

    List<Cell<Coordinate>> getCells() {
        List<Cell<Coordinate>> l = new ArrayList<>(outer.cellsPerColumn);
        int offset = index * outer.cellsPerColumn;
        for (int i = 0; i < outer.cellsPerColumn; i++) {
            l.add(outer.getCell(offset + i));
        }
        return l;
    }

    @Override
    public Coordinate coordinate() {
        return outer.topology.coordinateForIndex(index);
    }

    @Override
    public Region getRegion() {
        return outer.region;
    }

    @Override
    public int hashCode() {
        return index * 65536;
    }

    private LayerImpl layer() {
        return outer;
    }

    @Override
    public boolean equals(Object o) {
        boolean result = o != null && o.getClass() == ColumnImpl.class;
        if (result) {
            result = ((ColumnImpl) o).index == index;
            if (result) {
                result = ((ColumnImpl) o).layer() == layer();
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return index + " in " + layer();
    }

    public boolean isPredictivelyActivated() {
        int offset = index * outer.cellsPerColumn;
        for (int i = 0; i < outer.cellsPerColumn; i++) {
            if (outer.activatedCells().get(offset + i)) {
                if (outer.predictiveCells().get(offset + i)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isActivated() {
        int offset = index * outer.cellsPerColumn;
        for (int i = 0; i < outer.cellsPerColumn; i++) {
            if (outer.activatedCells().get(offset + i)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <R> Visitor.Result visitAllCells(Visitor<Cell, R> visitor, R arg) {
        Visitor.Result result = Visitor.Result.NO_VISITS;
        int offset = index * outer.cellsPerColumn;
        for (int i = 0; i < outer.cellsPerColumn; i++) {
            int pos = offset + i;
            Cell cell = outer.getCell(pos);
            result = visitor.visit(cell, arg);
            if (result.isDone()) {
                break;
            }
        }
        return result;
    }

    @Override
    public <R> Visitor.Result visitActivatedCells(Visitor<Cell, R> visitor, R arg) {
        Visitor.Result result = Visitor.Result.NO_VISITS;
        int offset = index * outer.cellsPerColumn;
        for (int i = 0; i < outer.cellsPerColumn; i++) {
            int pos = offset + i;
            if (outer.activatedCells().get(pos)) {
                Cell cell = outer.getCell(pos);
                result = visitor.visit(cell, arg);
                if (result.isDone()) {
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public Cell getCell(int index) {
        int pos = (this.index * outer.cellsPerColumn) + index;
        return outer.getCell(pos);
    }

    public ProximalDendriteSegment getProximalSegment() {
        return outer.mapping.segmentFor(this);
    }

    @Override
    public Iterator<Cell<Coordinate>> iterator() {
        return new Iter();
    }

    class Iter implements Iterator<Cell<Coordinate>> {
        int pos = index * outer.cellsPerColumn;
        final int max = pos + outer.cellsPerColumn;

        @Override
        public boolean hasNext() {
            return pos < max;
        }

        @Override
        public Cell<Coordinate> next() {
            Cell<Coordinate> result = outer.getCell(pos);
            pos++;
            return result;
        }

    }
}
