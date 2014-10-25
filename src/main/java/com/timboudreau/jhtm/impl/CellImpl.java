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
package com.timboudreau.jhtm.impl;

import com.timboudreau.jhtm.Cell;
import com.timboudreau.jhtm.Column;
import com.timboudreau.jhtm.DistalDendriteSegment;
import com.timboudreau.jhtm.OutputState;
import com.timboudreau.jhtm.ProximalDendriteSegment;
import com.timboudreau.jhtm.topology.CellLocation;
import com.timboudreau.jhtm.topology.Coordinate2D;
import com.timboudreau.jhtm.topology.Direction;
import com.timboudreau.jhtm.topology.Path;
import com.timboudreau.jhtm.util.Visitor;

/**
 *
 * @author Tim Boudreau
 */
class CellImpl<Coordinate> implements Cell<Coordinate> {

    private final int pos;
    final LayerImpl<Coordinate> layer;

    CellImpl(int pos, final LayerImpl<Coordinate> layer) {
        this.layer = layer;
        this.pos = pos;
    }

    int columnIndex() {
        return pos / layer.cellsPerColumn;
    }

    public CellLocation<Coordinate> coordinate() {
        return new CellLocation<>(indexInColumn(), layer.topology.coordinateForIndex(columnIndex()));
    }

    public int index() {
        return pos;
    }

    int indexInColumn() {
        return pos % layer.cellsPerColumn;
    }

    Path<Coordinate2D, ? extends Direction<Coordinate2D>>[] getPaths() {
        return (Path<Coordinate2D, ? extends Direction<Coordinate2D>>[]) layer.paths[pos];
    }

    @Override
    public OutputState state() {
        boolean active = layer.activatedCells().get(pos);
        boolean predictive = active ? layer.predictiveCells().get(pos) : false;
        return OutputState.forState(active, predictive);
    }

    private LayerImpl layer() {
        return layer;
    }

    @Override
    public int hashCode() {
        return pos * 41;
    }

    @Override
    public boolean equals(Object o) {
        boolean result = o != null && CellImpl.class == o.getClass();
        if (result) {
            CellImpl c = (CellImpl) o;
            result = c.pos == pos && c.layer() == layer();
        }
        return result;
    }

    @Override
    public String toString() {
        return "Cell " + coordinate() + ":" + indexInColumn() + " " + state();
    }

    @Override
    public <R> Visitor.Result visitDistalConnections(final Visitor<DistalDendriteSegment, R> v, final R outerArg) {
        Path<Coordinate2D, ? extends Direction<Coordinate2D>>[] pths = this.getPaths();
        Visitor.Result result = Visitor.Result.NO_VISITS;
        for (int i = 0; i < pths.length; i++) {
            result = v.visit(new DistalDendriteSegmentImpl(this, i, pths[i]), outerArg);
            if (result.isDone()) {
                break;
            }
        }
        return result;
    }

    @Override
    public ProximalDendriteSegment getProximalSegment() {
        Column<Coordinate> c = layer.getColumn(columnIndex());
        return c.getProximalSegment();
    }
}
