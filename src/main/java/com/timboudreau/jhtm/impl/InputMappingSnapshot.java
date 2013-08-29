package com.timboudreau.jhtm.impl;

import com.timboudreau.jhtm.BoostFactor;
import com.timboudreau.jhtm.Permanence;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Snapshot of input mapping configuration.
 *
 * @author Tim Boudreau
 */
class InputMappingSnapshot implements Serializable {

    final Map<Integer, Map<Integer, Permanence>> permanencesForColumn = new HashMap<>();
    final Map<Integer, BoostFactor> boostFactorForColumn = new HashMap<>();

    public InputMappingSnapshot snapshot() {
        InputMappingSnapshot nue = new InputMappingSnapshot();
        nue.boostFactorForColumn.putAll(boostFactorForColumn);
        for (Map.Entry<Integer, Map<Integer, Permanence>> e : permanencesForColumn.entrySet()) {
            nue.permanencesForColumn.put(e.getKey(), new HashMap<>(e.getValue()));
        }
        return nue;
    }
}
