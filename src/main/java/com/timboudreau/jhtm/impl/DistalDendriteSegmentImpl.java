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
