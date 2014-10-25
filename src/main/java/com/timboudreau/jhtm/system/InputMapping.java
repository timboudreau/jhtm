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
        System.out.println("InputMapping init " + this);
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
