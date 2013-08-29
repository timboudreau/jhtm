package com.timboudreau.jhtm.impl;

import com.timboudreau.jhtm.Permanence;
import com.timboudreau.jhtm.topology.Direction;
import com.timboudreau.jhtm.topology.Path;
import java.io.Serializable;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Holds state for the layer, so history can be rolled back and forward
 */
class LayerSnapshot<Coordinate> implements Serializable {
    public final BitSet activatedCells;
    public final BitSet predictiveCells;
    public final Map<Path<Coordinate, ? extends Direction<Coordinate>>, Map<Integer, Map<Integer, Permanence>>> permanenceForDendritePath = new IdentityHashMap<>();

    public LayerSnapshot(int totalCells) {
        activatedCells = new BitSet(totalCells);
        predictiveCells = new BitSet(totalCells);
    }

    private LayerSnapshot(LayerSnapshot<Coordinate> other) {
        this.activatedCells = (BitSet) other.activatedCells.clone();
        this.predictiveCells = (BitSet) other.predictiveCells.clone();
        // Do a deep copy, culling empty sets to save space
        for (Map.Entry<Path<Coordinate, ? extends Direction<Coordinate>>, Map<Integer, Map<Integer, Permanence>>> e : other.permanenceForDendritePath.entrySet()) {
            Map<Integer, Map<Integer, Permanence>> p4 = e.getValue();
            if (!p4.isEmpty()) {
                Map<Integer, Map<Integer, Permanence>> n4 = new HashMap<>();
                for (Map.Entry<Integer, Map<Integer, Permanence>> e1 : p4.entrySet()) {
                    Map<Integer, Permanence> m = e1.getValue();
                    if (!m.isEmpty()) {
                        Map<Integer, Permanence> n = new HashMap<>(m);
                        for (Map.Entry<Integer, Permanence> me : m.entrySet()) {
                            if (me.getValue().get(Permanence.LimitFunction.ZERO_TO_ONE) > 0D) {
                                n.put(me.getKey(), me.getValue());
                            }
                        }
                        n4.put(e1.getKey(), n);
                    }
                }
                if (!n4.isEmpty()) {
                    permanenceForDendritePath.put(e.getKey(), n4);
                }
            }
        }
    }

    public PermanenceInfo getPermanences(Path<Coordinate, ? extends Direction<Coordinate>> pth) {
        return new PermanenceInfo(pth);
    }

    public LayerSnapshot snapshot() {
        return new LayerSnapshot(this);
    }
    
    class PermanenceInfo {

        private final Path<Coordinate, ? extends Direction<Coordinate>> dendritePath;

        public PermanenceInfo(Path<Coordinate, ? extends Direction<Coordinate>> dendritePath) {
            this.dendritePath = dendritePath;
        }

        Map<Integer, Map<Integer, Permanence>> permanences(boolean create) {
            Map<Integer, Map<Integer, Permanence>> result = permanenceForDendritePath.get(dendritePath);
            if (result == null) {
                if (create) {
                    result = new HashMap<>();
                    permanenceForDendritePath.put(dendritePath, result);
                } else {
                    result = Collections.emptyMap();
                }
            }
            return result;
        }

        public boolean hasPermanence(int column) {
            Map<Integer, Permanence> m = permanences(false).get(column);
            if (m == null || m.isEmpty()) {
                return false;
            }
            for (Map.Entry<Integer, Permanence> e : m.entrySet()) {
                if (e.getValue().get() > 0D) {
                    return true;
                }
            }
            return false;
        }

        public Permanence getPermanence(int position, int cell) {
            Map<Integer, Permanence> m = permanences(false).get(position);
            if (m == null || m.isEmpty()) {
                return Permanence.ZERO;
            }
            Permanence result = m.get(cell);
            return result == null ? Permanence.ZERO : result;
        }

        Permanence updatePermanence(int position, int cell, double amount, boolean temporary) {
            Map<Integer, Permanence> m = permanences(true).get(position);
            if (m == null) {
                m = new HashMap<>();
                permanences(true).put(position, m);
            }
            Permanence permanence = m.get(cell);
            if (permanence == null) {
                permanence = Permanence.create(0);
            }
            Permanence nue = permanence.add(amount, temporary);
            if (nue != permanence) {
                m.put(cell, nue);
            }
            return nue;
        }

        Permanence setPermanence(int cell, int offset, Permanence permanence) {
            Map<Integer, Permanence> m = permanences(true).get(cell);
            Permanence old = null;
            if (m == null) {
                m = new HashMap<>();
                permanences(true).put(cell, m);
            } else {
                old = m.get(offset);
            }
            m.put(offset, permanence);
            return old;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.activatedCells);
        hash = 71 * hash + Objects.hashCode(this.predictiveCells);
        hash = 71 * hash + Objects.hashCode(this.permanenceForDendritePath);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LayerSnapshot other = (LayerSnapshot) obj;
        if (!Objects.equals(this.activatedCells, other.activatedCells)) {
            return false;
        }
        if (!Objects.equals(this.predictiveCells, other.predictiveCells)) {
            return false;
        }
        if (!Objects.equals(this.permanenceForDendritePath, other.permanenceForDendritePath)) {
            return false;
        }
        return true;
    }

}
