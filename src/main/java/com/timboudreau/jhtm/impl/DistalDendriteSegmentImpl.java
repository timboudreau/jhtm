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
import com.timboudreau.jhtm.PotentialSynapse;
import com.timboudreau.jhtm.topology.Direction;
import com.timboudreau.jhtm.topology.Path;
import com.timboudreau.jhtm.topology.Topology;
import com.timboudreau.jhtm.util.Visitor;

/**
 *
 * @author Tim Boudreau
 */
class DistalDendriteSegmentImpl<Coordinate> extends DistalDendriteSegment<Coordinate> {

    final CellImpl<Coordinate> cell;
    private final int ix;
    final Path<Coordinate, ? extends Direction<Coordinate>> path;

    DistalDendriteSegmentImpl(final CellImpl<Coordinate> cell, int ix, Path<Coordinate, ? extends Direction<Coordinate>> path) {
        this.cell = cell;
        this.ix = ix;
        this.path = path;
    }

    @Override
    public Cell<Coordinate> getSource() {
        return cell;
    }

    @Override
    public String toString() {
        return "Segment for " + path;
    }

    private Path<Coordinate, ? extends Direction<Coordinate>> path() {
        return path;
    }

    @Override
    public int hashCode() {
        return cell.hashCode() * (ix + 1) * 73;
    }

    @Override
    public boolean equals(Object o) {
        boolean result = o instanceof DistalDendriteSegmentImpl;
        if (result) {
            if (o == this) {
                return true;
            }
            result = ((DistalDendriteSegmentImpl<?>) o).getSource().equals(getSource());
            if (result) {
                result = ((DistalDendriteSegmentImpl<?>) o).path().equals(path);
            }
        }
        return result;
    }

    @Override
    public <J> Visitor.Result visitSynapses(final Visitor<PotentialSynapse<? extends Cell<Coordinate>>, J> visitor, final J midArg) {
        Coordinate coord = cell.layer.topology.coordinateForIndex(cell.columnIndex());
        return cell.layer.topology.walk(coord, path, new SynapseOuterVisitor<J, Coordinate>(visitor, midArg, this));
    }

    private static class SynapseOuterVisitor<J, Coordinate> extends Visitor<Coordinate, Topology<Coordinate>> {

        private final Visitor<PotentialSynapse<? extends Cell<Coordinate>>, J> visitor;
        private final J midArg;
        private final DistalDendriteSegmentImpl impl;

        public SynapseOuterVisitor(Visitor<PotentialSynapse<? extends Cell<Coordinate>>, J> visitor, J midArg, DistalDendriteSegmentImpl<Coordinate> impl) {
            this.visitor = visitor;
            this.midArg = midArg;
            this.impl = impl;
        }
        private int pathIndex;

        @Override
        public Visitor.Result visit(Coordinate coordinate, Topology<Coordinate> topology) {
            int offset = topology.toIndex(coordinate);
            final Column<Coordinate> column = impl.cell.layer.getColumn(offset);
//            System.out.println("DistalDendriteSegmentImpl visit " + coordinate + " in " + column);
            Visitor.Result result = Visitor.Result.NO_VISITS;
            for (final Cell<Coordinate> cell : column) {
//                System.out.println("Visit one cell " + cell.coordinate());
                result = visitor.visit(new DistalPotentialSynapse(pathIndex, impl, (CellImpl<Coordinate>) cell), midArg);
                if (result.isDone()) {
                    break;
                }
            }
            pathIndex++;
            return result;
        }
    }
}
