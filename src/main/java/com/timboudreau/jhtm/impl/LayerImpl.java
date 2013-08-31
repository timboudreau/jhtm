package com.timboudreau.jhtm.impl;

import com.timboudreau.jhtm.topology.Coordinate2D;
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
import com.timboudreau.jhtm.topology.CellLocation;
import com.timboudreau.jhtm.topology.Direction;
import com.timboudreau.jhtm.topology.Path;
import com.timboudreau.jhtm.topology.Topology;
import com.timboudreau.jhtm.util.Snapshottable;
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
public class LayerImpl<Coordinate> implements Layer, Snapshottable<LayerSnapshot> {

    private final int cellsPerColumn;
    private final RegionImpl region = new RegionImpl();
    private final Path<Coordinate, ? extends Direction<Coordinate>>[][] paths;
    private Topology<Coordinate> topology;
    private LayerSnapshot snapshot;
    private InputMapping<?, Coordinate> mapping;

    @SuppressWarnings("LeakingThisInConstructor")
    public LayerImpl(int cellsPerColumn, int distalDendritesPerCell, Topology<Coordinate> topology) {
        this(cellsPerColumn, distalDendritesPerCell, topology, new RandomDistalLayoutFactory<Coordinate>());
    }

    public LayerImpl(int cellsPerColumn, int distalDendritesPerCell, Topology<Coordinate> topology, DistalLayoutFactory<Coordinate> layout) {
        int columnCount = topology.columnCount();
        this.cellsPerColumn = cellsPerColumn;
        this.topology = topology;
        snapshot = new LayerSnapshot(topology.columnCount() * cellsPerColumn);

        int totalCells = columnCount * cellsPerColumn;
        paths = (Path<Coordinate, ? extends Direction<Coordinate>>[][]) topology.pathArray(totalCells, distalDendritesPerCell);
        layout.createLayout(topology, this, columnCount, cellsPerColumn, distalDendritesPerCell, new DistalDendrites<Coordinate>() {

            @Override
            public void add(Path<Coordinate, ? extends Direction<Coordinate>> path, int cellIndex, int dendriteIndex) {
                paths[cellIndex][dendriteIndex] = path;
            }
        });
    }

    public LayerImpl(Topology<Coordinate> topology, LayerSnapshot snapshot, InputMapping<?, Coordinate> mapping, int cellsPerColumn, Path<Coordinate, ? extends Direction<Coordinate>>[][] paths) {
        this.snapshot = snapshot;
        this.mapping = mapping;
        this.topology = topology;
        this.cellsPerColumn = cellsPerColumn;
        this.paths = paths;
    }

    public interface DistalDendrites<Coordinate> {

        public void add(Path<Coordinate, ? extends Direction<Coordinate>> path, int cellIndex, int dendriteIndex);
    }

    public interface DistalLayoutFactory<Coordinate> {

        public void createLayout(Topology<Coordinate> topology, Layer<Coordinate> layer, int columnCount, int cellsPerColumn, int distalDendritesPerCell, DistalDendrites addTo);
    }

    public static final class RandomDistalLayoutFactory<Coordinate> implements DistalLayoutFactory<Coordinate> {

        private final Random random;
        private final int dendriteLength;
        public static final int DEFAULT_DENDRITE_LENGTH = 10;

        public RandomDistalLayoutFactory() {
            this(DEFAULT_DENDRITE_LENGTH);
        }

        public RandomDistalLayoutFactory(int dendriteLength) {
            this(new Random(23), dendriteLength);
        }

        public RandomDistalLayoutFactory(Random random, int dendriteLength) {
            this.random = random;
            this.dendriteLength = dendriteLength;
        }

        @Override
        public void createLayout(Topology<Coordinate> topology, Layer<Coordinate> layer, int columnCount, int cellsPerColumn, int distalDendritesPerCell, DistalDendrites addTo) {
            System.out.println("Creating random layout");
            int totalCells = columnCount * cellsPerColumn;
            for (int i = 0; i < totalCells; i++) {
                if (i % 10000 == 0) {
                    System.out.println("CELL " + i + " of " + totalCells);
                }
                Coordinate coord = topology.coordinateForIndex(i);
                for (int j = 0; j < distalDendritesPerCell; j++) {
                    Path<Coordinate, ? extends Direction<Coordinate>> path = topology.createRandom(random, coord, dendriteLength);
                    addTo.add(path, i, j);
                }
            }
            System.out.println("Done creating random layout");
        }
    }

    public synchronized LayerSnapshot snapshot() {
        return snapshot.snapshot();
    }

    public synchronized LayerSnapshot restore(LayerSnapshot snapshot) {
        LayerSnapshot old = this.snapshot;
        this.snapshot = snapshot.snapshot();
        return old;
    }

    public Iterator<Column<Coordinate>> iterator() {
        return new Iterator<Column<Coordinate>>() {
            int ix = -1;

            @Override
            public boolean hasNext() {
                return ix + 1 < topology.columnCount();
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

    public void setInputMapping(InputMapping<?, Coordinate> mapping) {
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
        if (index < topology.columnCount()) {
            return new ColumnImpl(index);
        }
        return null;
    }

    @Override
    public int size() {
        return topology.columnCount();
    }

    public Region toRegion() {
        return region;
    }

    public int cellCount() {
        return topology.columnCount() * cellsPerColumn;
    }

    @Override
    public String toString() {
        return "Layer " + activatedCells().cardinality() + " active " + predictiveCells().cardinality() + " predictive";
    }

    class ColumnImpl implements Column<Coordinate> {

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

        public Coordinate coordinate() {
            return topology.coordinateForIndex(index);
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

    class CellImpl implements Cell<Coordinate> {

        private final int pos;

        public CellImpl(int pos) {
            this.pos = pos;
        }

        int columnIndex() {
            return pos / cellsPerColumn;
        }

        public CellLocation<Coordinate> coordinate() {
            return new CellLocation<>(indexInColumn(), topology.coordinateForIndex(columnIndex()));
        }

        int index() {
            return pos;
        }

        int indexInColumn() {
            return pos % cellsPerColumn;
        }

        Path<Coordinate2D, ? extends Direction<Coordinate2D>>[] getPaths() {
            return (Path<Coordinate2D, ? extends Direction<Coordinate2D>>[]) paths[pos];
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
                final Path<Coordinate2D, ? extends Direction<Coordinate2D>> p = pths[i];
                final int ix = i;
                class Seg extends DistalDendriteSegment {

                    @Override
                    public Cell getSource() {
                        return CellImpl.this;
                    }

                    public String toString() {
                        return "Segment for " + p;
                    }

                    private Path<Coordinate2D, ? extends Direction<Coordinate2D>> path() {
                        return p;
                    }

                    public int hashCode() {
                        return CellImpl.this.hashCode() * (ix + 1) * 73;
                    }

                    public boolean equals(Object o) {
                        boolean result = o != null && o.getClass() == Seg.class;
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
                        Coordinate coord = topology.coordinateForIndex(columnIndex());
                        Iterable<? extends Direction<Coordinate>> iter = (Iterable<? extends Direction<Coordinate>>) p; //XXX
                        return topology.walk(coord, iter, new Visitor<Coordinate, Topology<Coordinate>>() {

                            int pathIndex;

                            @Override
                            public Visitor.Result visit(Coordinate coordinate, Topology<Coordinate> topology) {
                                int offset = topology.toIndex(coordinate);
                                final ColumnImpl column = LayerImpl.this.getColumn(offset);
                                Visitor.Result result = Visitor.Result.NO_VISITS;
                                for (final CellImpl cell : column.getCells()) {
                                    class Syn extends PotentialSynapse<Cell> {

                                        public String toString() {
                                            return "Synapse " + cell + " in " + Seg.this;
                                        }

                                        @Override
                                        public Permanence getPermanence() {
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
                                            boolean result = o != null && o.getClass() == Syn.class;
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

                                        @Override
                                        public OutputState getTargetState() {
                                            return cell.state();
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

    private class RegionImpl extends Region<Coordinate> {

        @Override
        public int size() {
            return LayerImpl.this.size();
        }

        @Override
        public Column get(int ix) {
            return LayerImpl.this.getColumn(ix);
        }

        @Override
        public Column<Coordinate> get(Coordinate coord) {
            return LayerImpl.this.getColumn(topology.toIndex(coord));
        }
    }
}
