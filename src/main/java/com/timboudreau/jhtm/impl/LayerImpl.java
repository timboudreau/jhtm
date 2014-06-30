package com.timboudreau.jhtm.impl;

import com.timboudreau.jhtm.Column;
import com.timboudreau.jhtm.Region;
import com.timboudreau.jhtm.system.InputMapping;
import com.timboudreau.jhtm.system.Layer;
import com.timboudreau.jhtm.topology.Direction;
import com.timboudreau.jhtm.topology.Path;
import com.timboudreau.jhtm.topology.Topology;
import com.timboudreau.jhtm.util.Bits;
import com.timboudreau.jhtm.util.Snapshottable;
import java.util.Iterator;
import java.util.Random;

/**
 * Implements region functionality and actually stores the data of one layer.
 * Uses BitSet and friends to try to ensure that data is laid out contiguously
 * in memory, and flyweight implementations of column and cell which use that
 * data.
 *
 * @author Tim Boudreau
 */
public class LayerImpl<Coordinate> implements Layer, Snapshottable<LayerSnapshot<Coordinate>> {

    final int cellsPerColumn;
    final RegionImpl region = new RegionImpl();
    Path<Coordinate, ? extends Direction<Coordinate>>[][] paths;
    Topology<Coordinate> topology;
    LayerSnapshot snapshot;
    InputMapping<?, Coordinate> mapping;

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
        return new ColumnIterator();
    }

    public void setInputMapping(InputMapping<?, Coordinate> mapping) {
        this.mapping = mapping;
    }

    Bits activatedCells() {
        return snapshot.activatedCells;
    }

    Bits predictiveCells() {
        return snapshot.predictiveCells;
    }

    @Override
    public Column<Coordinate> getColumn(int index) {
        if (index < topology.columnCount()) {
            return new ColumnImpl<>(index, this);
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

    CellImpl getCell(int pos) {
        return new CellImpl(pos, this);
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

    private class ColumnIterator implements Iterator<Column<Coordinate>> {

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
    }
}
