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
package com.timboudreau.jhtm;

/**
 *
 * @author Tim Boudreau
 */
public abstract class PotentialSynapse<Target> {

    public abstract Permanence setPermanence(Permanence p);

    public abstract Permanence getPermanence();

    public abstract Target getTarget();

    public abstract Permanence adjustPermanence(double amount, boolean temporary);

    public abstract DendriteSegment getDendriteSegment();
    
    public abstract OutputState getTargetState();

    public Permanence retainTemporaryValues() {
        Permanence old = getPermanence();
        Permanence nue = old.retainTemporaryValues();
        if (old != nue) {
            setPermanence(nue);
        }
        return nue;
    }

    public Permanence cullTemporaryValues() {
        Permanence old = getPermanence();
        Permanence nue = old.cullTemporaryValues();
        if (old != nue) {
            setPermanence(nue);
        }
        return nue;
    }

    public String toString() {
        return "Synapse for " + getTarget() + " on " + getDendriteSegment();
    }
}
