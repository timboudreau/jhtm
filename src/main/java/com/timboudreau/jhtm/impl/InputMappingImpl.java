/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.timboudreau.jhtm.impl;

import com.timboudreau.jhtm.BoostFactor;
import com.timboudreau.jhtm.DendriteSegment;
import com.timboudreau.jhtm.InputBit;
import com.timboudreau.jhtm.Permanence;
import com.timboudreau.jhtm.PotentialSynapse;
import com.timboudreau.jhtm.ProximalDendriteSegment;
import com.timboudreau.jhtm.impl.LayerImpl.ColumnImpl;
import com.timboudreau.jhtm.system.Input;
import com.timboudreau.jhtm.system.InputMapping;
import com.timboudreau.jhtm.system.Thresholds;
import com.timboudreau.jhtm.util.Visitor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Tim Boudreau
 */
public class InputMappingImpl<T> extends InputMapping<T, ColumnImpl> {

    private final Map<Integer, ProximalDendriteImpl<T>> dendrites = new HashMap<>();
    private final Thresholds thresholds;

    public InputMappingImpl(Input<T> input, SynapseFactory<T, ColumnImpl> connections, LayerImpl layer, Thresholds thresholds) {
        super(input, connections, layer);
        this.thresholds = thresholds;
    }

    @Override
    protected ProximalDendriteBuilder<T, ColumnImpl> connector() {
        return new ProximalDendriteBuilder<T, ColumnImpl>() {
            private final List<InputBit<T>> bits = new LinkedList<>();
            private ColumnImpl column;

            public synchronized ProximalDendriteImpl save() {
                assert column != null;
                ProximalDendriteImpl result = new ProximalDendriteImpl<>(column, bits, thresholds);
                dendrites.put(column.index(), result);
                bits.clear();
                column = null;
                return result;
            }

            public synchronized ProximalDendriteImpl saveAndNew(ColumnImpl col) {
                ProximalDendriteImpl result = null;
                if (this.column != null) {
                    result = save();
                }
                this.column = col;
                return result;
            }

            @Override
            public synchronized void newDendrite(ColumnImpl col) {
                this.column = column;
                save();
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
        for (Map.Entry<Integer, ProximalDendriteImpl<T>> e : dendrites.entrySet()) {
            result = v.visit(e.getValue(), arg);
            if (result.isDone()) {
                break;
            }
        }
        return result;
    }

    @Override
    protected ProximalDendriteSegment getSegmentFor(ColumnImpl column) {
        if (!dendrites.containsKey(column.index())) {
            throw new IllegalArgumentException("No dendrite for " + column.index() + " in " + dendrites.keySet());
        }
        return dendrites.get(column.index());
    }

    private static class ProximalDendriteImpl<T> extends ProximalDendriteSegment<ColumnImpl> {

        private final ColumnImpl column;
        private final Map<InputBit<T>, Permanence> permanenceForBit = new HashMap<>();
        private BoostFactor boost = BoostFactor.DEFAULT;

        public ProximalDendriteImpl(ColumnImpl column, List<InputBit<T>> l, Thresholds tolerances) {
            this.column = column;
            for (InputBit bit : l) {
                permanenceForBit.put(bit, Permanence.create(tolerances.defaultPermanence()));
            }
        }
        
        public synchronized BoostFactor getBoostFactor() {
            return boost;
        }
        
        public synchronized void setBoostFactor(BoostFactor boost) {
            assert boost != null;
            this.boost = boost;
        }

        boolean matches(ColumnImpl col) {
            return col.index() == column.index();
        }

        @SuppressWarnings("element-type-mismatch")
        public boolean contains(InputBit<?> bit) {
            return permanenceForBit.containsKey(bit);
        }

        public int size() {
            return permanenceForBit.size();
        }

        @Override
        public ColumnImpl getSource() {
            return column;
        }

        @Override
        public <R> Visitor.Result visitSynapses(Visitor<PotentialSynapse<? extends ColumnImpl>, R> visitor, R arg) {
            Visitor.Result result = Visitor.Result.NO_VISITS;
            for (final Map.Entry<InputBit<T>, Permanence> e : permanenceForBit.entrySet()) {
                class PC extends PotentialSynapse<ColumnImpl> {

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
                    public ColumnImpl getTarget() {
                        return column;
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
                        return e.getKey();
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
