package com.timboudreau.jhtm;

import java.io.Serializable;

/**
 *
 * @author Tim Boudreau
 */
public final class BoostFactor implements Serializable {

    private final double multiplier;
    public static final BoostFactor DEFAULT = new BoostFactor(1.0D);

    private BoostFactor(double multiplier) {
        this.multiplier = multiplier;
    }

    public static BoostFactor create(double multiplier) {
        return new BoostFactor(multiplier);
    }

    public BoostFactor increase(double by) {
        return new BoostFactor(multiplier + by);
    }

    public BoostFactor decrease(double by) {
        return new BoostFactor(Math.min(1.0D, multiplier - by));
    }

    public double boost(double input) {
        if (multiplier > 1.0D) {
            return input * multiplier;
        } else {
            return input;
        }
    }

    private Double doubleValue() {
        return multiplier;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BoostFactor && ((BoostFactor) o).doubleValue().equals(doubleValue());
    }

    @Override
    public int hashCode() {
        return doubleValue().hashCode();
    }
}
