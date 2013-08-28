package com.timboudreau.jhtm.impl;

import com.timboudreau.jhtm.Cell;
import com.timboudreau.jhtm.Column;
import com.timboudreau.jhtm.DendriteSegment;
import com.timboudreau.jhtm.DistalDendriteSegment;
import com.timboudreau.jhtm.OutputState;
import com.timboudreau.jhtm.Permanence;
import com.timboudreau.jhtm.PotentialSynapse;
import com.timboudreau.jhtm.ProximalDendriteSegment;
import com.timboudreau.jhtm.Region;
import com.timboudreau.jhtm.system.InputMapping;
import com.timboudreau.jhtm.system.Layer;
import com.timboudreau.jhtm.util.Visitor;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Implements region functionality and actually stores the data of one layer.
 * Uses BitSet and friends to try to ensure that data is laid out contiguously
 * in memory, and flyweight implementations of column and cell which use that
 * data.
 *
 * @author Tim Boudreau
 */
public class LayerImpl implements Layer {

    public final int columnCount;
    public final int cellsPerColumn;
    private final RegionImpl region = new RegionImpl();
    private final DendritePath[][] paths;
    private Topology2D topology;
    private LayerSnapshot snapshot;
    private InputMapping<?, ColumnImpl> mapping;

    public LayerImpl(int columnCount, int cellsPerColumn, int connectionsPerCell, Random random, Topology2D topology, int dendriteLength) {
        this.columnCount = columnCount;
        this.cellsPerColumn = cellsPerColumn;
        this.topology = topology;
        snapshot = new LayerSnapshot(columnCount * cellsPerColumn);

        int totalCells = columnCount * cellsPerColumn;
        paths = new DendritePath[totalCells][cellsPerColumn];
        for (int i = 0; i < totalCells; i++) {
            Coordinate2D coord = topology.coordinateForColumnIndex(i);
            for (int j = 0; j < cellsPerColumn; j++) {
                paths[i][j] = DendritePath.createRandom(random, coord, topology, dendriteLength);
            }
        }
    }

    public Iterator<Column> iterator() {
        return new Iterator<Column>() {
            int ix = -1;

            @Override
            public boolean hasNext() {
                return ix + 1 < columnCount;
            }

            @Override
            public Column next() {
                return getColumn(++ix);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public void setInputMapping(InputMapping<?, ColumnImpl> mapping) {
        this.mapping = mapping;
    }

    private BitSet activatedCells() {
        return snapshot.activatedCells;
    }

    private BitSet predictiveCells() {
        return snapshot.predictiveCells;
    }

    @Override
    public ColumnImpl getColumn(int index) {
        if (index < columnCount) {
            return new ColumnImpl(index);
        }
        return null;
    }

    @Override
    public int size() {
        return columnCount;
    }

    public Region toRegion() {
        return region;
    }

    public int cellCount() {
        return columnCount * cellsPerColumn;
    }

    @Override
    public String toString() {
        return "Layer " + activatedCells().cardinality() + " active " + predictiveCells().cardinality() + " predictive";
    }

    class ColumnImpl implements Column {

        final int index;

        public ColumnImpl(int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }

        List<CellImpl> getCells() {
            List<CellImpl> l = new ArrayList<>(cellsPerColumn);
            int offset = index * cellsPerColumn;
            for (int i = 0; i < cellsPerColumn; i++) {
                l.add(LayerImpl.this.getCell(offset + i));
            }
            return l;
        }

        @Override
        public Region getRegion() {
            return region;
        }

        @Override
        public int hashCode() {
            return index * 65536;
        }

        private LayerImpl layer() {
            return LayerImpl.this;
        }

        public Coordinate2D getCoordinate() {
            return topology.coordinateForColumnIndex(index);
        }

        @Override
        public boolean equals(Object o) {
            boolean result = o instanceof ColumnImpl;
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
            int offset = index * cellsPerColumn;
            for (int i = 0; i < cellsPerColumn; i++) {
                if (activatedCells().get(offset + i)) {
                    if (predictiveCells().get(offset + i)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean isActivated() {
            int offset = index * cellsPerColumn;
            for (int i = 0; i < cellsPerColumn; i++) {
                if (activatedCells().get(offset + i)) {
                    return true;
                }
            }
            return false;
        }

        public <R> Visitor.Result visitAllCells(Visitor<Cell, R> visitor, R arg) {
            Visitor.Result result = Visitor.Result.NO_VISITS;
            int offset = index * cellsPerColumn;
            for (int i = 0; i < cellsPerColumn; i++) {
                int pos = offset + i;
                Cell cell = LayerImpl.this.getCell(pos);
                result = visitor.visit(cell, arg);
                if (result.isDone()) {
                    break;
                }
            }
            return result;
        }

        public <R> Visitor.Result visitActivatedCells(Visitor<Cell, R> visitor, R arg) {
            Visitor.Result result = Visitor.Result.NO_VISITS;
            int offset = index * cellsPerColumn;
            for (int i = 0; i < cellsPerColumn; i++) {
                int pos = offset + i;
                if (activatedCells().get(pos)) {
                    Cell cell = LayerImpl.this.getCell(pos);
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
            int pos = (this.index * cellsPerColumn) + index;
            return LayerImpl.this.getCell(pos);
        }

        public ProximalDendriteSegment getProximalSegment() {
            return mapping.segmentFor(this);
        }
    }

    class CellImpl implements Cell {

        private final int pos;

        public CellImpl(int pos) {
            this.pos = pos;
        }

        int columnIndex() {
            return pos / cellsPerColumn;
        }

        Coordinate2D coordinate() {
            return topology.coordinateForColumnIndex(columnIndex());
        }

        int index() {
            return pos;
        }

        int indexInColumn() {
            return pos % cellsPerColumn;
        }

        DendritePath[] getPaths() {
            return paths[pos];
        }

        @Override
        public OutputState state() {
            boolean active = activatedCells().get(pos);
            boolean predictive = active ? predictiveCells().get(pos) : false;
            return OutputState.forState(active, predictive);
        }

        private LayerImpl layer() {
            return LayerImpl.this;
        }

        @Override
        public int hashCode() {
            return pos * 41;
        }

        @Override
        public boolean equals(Object o) {
            boolean result = o instanceof CellImpl;
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
            DendritePath[] pths = this.getPaths();
            Visitor.Result result = Visitor.Result.NO_VISITS;
            for (int i = 0; i < pths.length; i++) {
                final DendritePath p = pths[i];
                final int ix = i;
                class Seg extends DistalDendriteSegment {

                    @Override
                    public Cell getSource() {
                        return CellImpl.this;
                    }

                    public String toString() {
                        return "Segment for " + p;
                    }

                    private DendritePath path() {
                        return p;
                    }

                    public int hashCode() {
                        return CellImpl.this.hashCode() * (ix + 1) * 73;
                    }

                    public boolean equals(Object o) {
                        boolean result = o instanceof Seg;
                        if (result) {
                            if (o == this) {
                                return true;
                            }
                            result = ((Seg) o).getSource().equals(getSource());
                            if (result) {
                                result = ((Seg) o).path().equals(p);
                            }
                        }
                        return result;
                    }

                    @Override
                    public <J> Visitor.Result visitSynapses(final Visitor<PotentialSynapse<? extends Cell>, J> visitor, final J midArg) {
                        return topology.walk(coordinate(), p, new Visitor<Coordinate2D, Topology2D>() {

                            int pathIndex;

                            @Override
                            public Visitor.Result visit(Coordinate2D coordinate, Topology2D topology) {
                                final ColumnImpl column = LayerImpl.this.getColumn(coordinate.toOffset(topology));
                                Visitor.Result result = Visitor.Result.NO_VISITS;
                                for (final CellImpl cell : column.getCells()) {
                                    class Syn extends PotentialSynapse<Cell> {

                                        public String toString() {
                                            return "Synapse " + cell + " in " + Seg.this;
                                        }

                                        @Override
                                        public Permanence getPermanence() {
                                            System.out.println("Get permanence for " + pathIndex + ":" + cell.indexInColumn());
                                            return snapshot.getPermanences(p).getPermanence(pathIndex, cell.indexInColumn());
                                        }

                                        private Seg seg() {
                                            return Seg.this;
                                        }

                                        private CellImpl dest() {
                                            return CellImpl.this;
                                        }

                                        @Override
                                        public int hashCode() {
                                            return seg().hashCode() + (41 * cell.hashCode());
                                        }

                                        @Override
                                        public boolean equals(Object o) {
                                            if (o == this) {
                                                return true;
                                            }
                                            boolean result = o instanceof Syn;
                                            if (result) {
                                                Syn syn = (Syn) o;
                                                result = syn.getTarget().equals(cell);
                                                if (result) {
                                                    result = syn.seg().equals(seg());
                                                    if (result) {
                                                        result = CellImpl.this.equals(syn.dest());
                                                    }
                                                }
                                            }
                                            return result;
                                        }

                                        @Override
                                        public Cell getTarget() {
                                            return cell;
                                        }

                                        @Override
                                        public Permanence adjustPermanence(double amount, boolean temporary) {
                                            return snapshot.getPermanences(p).updatePermanence(pathIndex, cell.indexInColumn(), amount, temporary);
                                        }

                                        @Override
                                        public Permanence setPermanence(Permanence pp) {
                                            return snapshot.getPermanences(p).setPermanence(cell.index(), cell.columnIndex(), pp);
                                        }

                                        @Override
                                        public DendriteSegment getDendriteSegment() {
                                            return Seg.this;
                                        }
                                    }
                                    result = visitor.visit(new Syn(), midArg);
                                    if (result.isDone()) {
                                        break;
                                    }
                                }
                                pathIndex++;
                                return result;
                            }

                        });
                    }
                }
                result = v.visit(new Seg(), outerArg);
                if (result.isDone()) {
                    break;
                }
            }
            return result;
        }

        @Override
        public ProximalDendriteSegment getProximalSegment() {
            ColumnImpl c = getColumn(columnIndex());
            return c.getProximalSegment();
        }
    }

    CellImpl getCell(int pos) {
        return new CellImpl(pos);
    }

    private class RegionImpl extends Region {

        @Override
        public int size() {
            return LayerImpl.this.size();
        }

        @Override
        public Column get(int ix) {
            return LayerImpl.this.getColumn(ix);
        }
    }
}