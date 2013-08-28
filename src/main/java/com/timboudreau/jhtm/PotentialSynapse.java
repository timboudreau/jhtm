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
