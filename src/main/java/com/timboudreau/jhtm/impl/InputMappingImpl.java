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

import com.timboudreau.jhtm.BoostFactor;
import com.timboudreau.jhtm.Column;
import com.timboudreau.jhtm.DendriteSegment;
import com.timboudreau.jhtm.InputBit;
import com.timboudreau.jhtm.OutputState;
import com.timboudreau.jhtm.Permanence;
import com.timboudreau.jhtm.PotentialSynapse;
import com.timboudreau.jhtm.ProximalDendriteSegment;
import com.timboudreau.jhtm.impl.ColumnImpl;
import com.timboudreau.jhtm.system.Input;
import com.timboudreau.jhtm.system.InputMapping;
import com.timboudreau.jhtm.system.InputMapping.ProximalDendriteBuilder;
import com.timboudreau.jhtm.system.InputMapping.SynapseFactory;
import com.timboudreau.jhtm.system.Thresholds;
import com.timboudreau.jhtm.util.Snapshottable;
import com.timboudreau.jhtm.util.Visitor;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Tim Boudreau
 */
public class InputMappingImpl<T, Coordinate> extends InputMapping<T, Coordinate> implements Snapshottable<InputMappingSnapshot> {

    private final Thresholds thresholds;
    private InputMappingSnapshot snapshot = new InputMappingSnapshot();

    public InputMappingImpl(Input<T> input, SynapseFactory<T, Coordinate> connections, LayerImpl layer, Thresholds thresholds) {
        super(input, connections, layer);
        this.thresholds = thresholds;
        init();
    }

    @Override
    protected ProximalDendriteBuilder<T, Coordinate> connector() {
        return new ProximalDendriteBuilder<T, Coordinate>() {
            private final List<InputBit<T>> bits = new LinkedList<>();
            private Column<Coordinate> column;

            public synchronized ProximalDendriteImpl save() {
                ProximalDendriteImpl result = null;
                if (column != null) {
                    result = new ProximalDendriteImpl(column, bits, thresholds);
                }
                bits.clear();
                column = null;
                return result;
            }

            public synchronized ProximalDendriteImpl saveAndNew(Column<Coordinate> col) {
                ProximalDendriteImpl result = null;
                if (this.column != null) {
                    result = save();
                }
                this.column = col;
                return result;
            }

            @Override
            public synchronized void newDendrite(Column<Coordinate> col) {
                this.column = col;
                save();
                this.column = col;
            }

            @Override
            public void add(InputBit<T> bit) {
                bits.add(bit);
            }
        };
    }

    @Override
    protected <R> Visitor.Result doVisitProximalDendriteSegments(Visitor<ProximalDendriteSegment, R> v, R arg) {
        Visitor.Result result = Visitor.Result.NO_VISITS;
        for (Map.Entry<Integer, Map<Integer, Permanence>> e : currentSnapshot().permanencesForColumn.entrySet()) {
            ProximalDendriteImpl impl = new ProximalDendriteImpl(e.getKey(), thresholds);
            result = v.visit(impl, arg);
            if (result.isDone()) {
                break;
            }
        }
        return result;
    }

    @Override
    protected ProximalDendriteSegment getSegmentFor(Column<Coordinate> column) {
        return new ProximalDendriteImpl(column.index(), thresholds);
    }

    @Override
    public InputMappingSnapshot snapshot() {
        return currentSnapshot().snapshot();
    }

    @Override
    public synchronized InputMappingSnapshot restore(InputMappingSnapshot snapshot) {
        InputMappingSnapshot old = currentSnapshot();
        this.snapshot = snapshot.snapshot();
        return old;
    }

    private synchronized InputMappingSnapshot currentSnapshot() {
        return snapshot;
    }

    private class ProximalDendriteImpl<Coordinate> extends ProximalDendriteSegment<Coordinate, T> {

        private final int column;

        ProximalDendriteImpl(int column, Thresholds tolerances) {
            this.column = column;
        }

        ProximalDendriteImpl(Column<Coordinate> column, List<InputBit<T>> l, Thresholds tolerances) {
            this.column = column.index();
            InputMappingSnapshot snap = currentSnapshot();
            Map<Integer, Permanence> permanenceForBit = new HashMap<>();
            snap.permanencesForColumn.put(this.column, permanenceForBit);
            for (InputBit bit : l) {
                permanenceForBit.put(bit.index(), Permanence.create(tolerances.defaultPermanence()));
            }
        }

        public synchronized BoostFactor getBoostFactor() {
            BoostFactor factor = snapshot.boostFactorForColumn.get(column);
            return factor == null ? BoostFactor.DEFAULT : factor;
        }

        public synchronized void setBoostFactor(BoostFactor boost) {
            currentSnapshot().boostFactorForColumn.put(column, boost);
        }

        boolean matches(ColumnImpl col) {
            return column == col.index();
        }

        private Map<Integer, Permanence> permanenceForBit() {
            Map<Integer, Permanence> map = snapshot.permanencesForColumn.get(column);
            return map == null ? Collections.<Integer, Permanence>emptyMap() : map;
        }

        @SuppressWarnings("element-type-mismatch")
        public boolean contains(InputBit<?> bit) {
            Map<Integer, Permanence> permanenceForBit = permanenceForBit();
            return permanenceForBit == null ? false : permanenceForBit.containsKey(bit.index());
        }

        public int size() {
            Map<Integer, Permanence> permanenceForBit = permanenceForBit();
            return permanenceForBit == null ? 0 : permanenceForBit.size();
        }

        @Override
        public Column<Coordinate> getSource() {
            return layer().getColumn(column);
        }

        @Override
        public <R> Visitor.Result visitSynapses(Visitor<PotentialSynapse<? extends InputBit<T>>, R> visitor, R arg) {
            Visitor.Result result = Visitor.Result.NO_VISITS;
            Map<Integer, Permanence> permanenceForBit = permanenceForBit();
            for (final Map.Entry<Integer, Permanence> e : permanenceForBit.entrySet()) {
                class PC extends PotentialSynapse<InputBit<T>> {

                    @Override
                    public Permanence setPermanence(Permanence p) {
                        e.setValue(p);
                        return p;
                    }

                    @Override
                    public Permanence getPermanence() {
                        return e.getValue();
                    }

                    @Override
                    public Permanence adjustPermanence(double amount, boolean temporary) {
                        Permanence p = getPermanence().add(amount, temporary);
                        e.setValue(p);
                        return p;
                    }

                    @Override
                    public DendriteSegment getDendriteSegment() {
                        return ProximalDendriteImpl.this;
                    }

                    InputBit<T> bit() {
                        return input.get(e.getKey());
                    }

                    @Override
                    public boolean equals(Object o) {
                        return o != null && o.getClass() == getClass() && ((PC) o).bit().equals(bit());
                    }

                    @Override
                    public int hashCode() {
                        return bit().index() * 7639;
                    }

                    public String toString() {
                        return "Synapse " + column + " bit " + bit();
                    }

                    @Override
                    public InputBit<T> getTarget() {
                        return bit();
                    }

                    @Override
                    public OutputState getTargetState() {
                        return bit().isActive() ? OutputState.ACTIVE : OutputState.INACTIVE;
                    }

                }
                result = visitor.visit(new PC(), arg);
                if (result.isDone()) {
                    break;
                }
            }
            return result;
        }
    }
}
