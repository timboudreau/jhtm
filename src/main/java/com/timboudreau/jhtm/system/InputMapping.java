package com.timboudreau.jhtm.system;

import com.timboudreau.jhtm.Column;
import com.timboudreau.jhtm.InputBit;
import com.timboudreau.jhtm.ProximalDendriteSegment;
import com.timboudreau.jhtm.impl.LayerImpl;
import com.timboudreau.jhtm.util.Visitor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Holds mapping info which maps proximal dendrites between columns and input
 * bits
 *
 * @author Tim Boudreau
 */
public abstract class InputMapping<T, C extends Column> {

    protected final Input<T> input;
    private volatile boolean initialized;
    protected final SynapseFactory<T, C> connections;
    private final Layer layer;

    public InputMapping(Input<T> input, SynapseFactory<T, C> connections, Layer layer) {
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
        ProximalDendriteBuilder<T, C> conn = connector();
        connections.connect(layer, conn, input);
        if (layer instanceof LayerImpl) {
            InputMapping m = this; //XXX hack
            ((LayerImpl) layer).setInputMapping(m);
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

    public ProximalDendriteSegment segmentFor(C column) {
        checkInit();
        return getSegmentFor(column);
    }

    protected abstract ProximalDendriteSegment getSegmentFor(C column);

    protected abstract ProximalDendriteBuilder<T, C> connector();

    protected abstract <R> Visitor.Result doVisitProximalDendriteSegments(Visitor<ProximalDendriteSegment, R> v, R arg);

    public static abstract class SynapseFactory<T, C extends Column> {

        public abstract void connect(Layer layer, ProximalDendriteBuilder<T, C> connector, Input<T> input);
    }

    public interface ProximalDendriteBuilder<T, C extends Column> {

        public ProximalDendriteSegment save();

        public ProximalDendriteSegment saveAndNew(C col);

        public void newDendrite(C column);

        public void add(InputBit<T> bit);
    }
}
