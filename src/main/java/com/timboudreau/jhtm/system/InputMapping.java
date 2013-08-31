package com.timboudreau.jhtm.system;

import com.timboudreau.jhtm.Column;
import com.timboudreau.jhtm.InputBit;
import com.timboudreau.jhtm.ProximalDendriteSegment;
import com.timboudreau.jhtm.impl.LayerImpl;
import com.timboudreau.jhtm.util.Visitor;

/**
 * Holds mapping info which maps proximal dendrites between columns and input
 * bits
 *
 * @author Tim Boudreau
 */
public abstract class InputMapping<T, Coordinate> {

    protected final Input<T> input;
    private volatile boolean initialized;
    protected final SynapseFactory<T, Coordinate> connections;
    private final Layer layer;

    public InputMapping(Input<T> input, SynapseFactory<T, Coordinate> connections, Layer layer) {
        this.input = input;
        this.connections = connections;
        this.layer = layer;
    }

    public final Input<T> input() {
        return input;
    }

    public final Layer layer() {
        return layer;
    }

    protected void init() {
        ProximalDendriteBuilder<T, Coordinate> conn = connector();
        connections.connect(layer, conn, input);
        if (layer instanceof LayerImpl) {
            ((LayerImpl<Coordinate>) layer).setInputMapping(this);
        }
    }

    private void checkInit() {
        if (!initialized) {
            initialized = true;
            init();
        }
    }

    public final <R> Visitor.Result visitProximalDendriteSegments(Visitor<ProximalDendriteSegment, R> v, R arg) {
        checkInit();
        return doVisitProximalDendriteSegments(v, arg);
    }

    public ProximalDendriteSegment segmentFor(Column<Coordinate> column) {
        checkInit();
        return getSegmentFor(column);
    }

    protected abstract ProximalDendriteSegment getSegmentFor(Column<Coordinate> column);

    protected abstract ProximalDendriteBuilder<T, Coordinate> connector();

    protected abstract <R> Visitor.Result doVisitProximalDendriteSegments(Visitor<ProximalDendriteSegment, R> v, R arg);

    public static interface SynapseFactory<T, Coordinate> {

        public abstract void connect(Layer<Coordinate> layer, ProximalDendriteBuilder<T, Coordinate> connector, Input<T> input);
    }

    public interface ProximalDendriteBuilder<T, Coordinate> {

        public ProximalDendriteSegment save();

        public ProximalDendriteSegment saveAndNew(Column<Coordinate> col);

        public void newDendrite(Column<Coordinate> column);

        public void add(InputBit<T> bit);
    }
}
