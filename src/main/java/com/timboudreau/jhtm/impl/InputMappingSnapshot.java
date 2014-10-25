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
