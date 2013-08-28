package com.timboudreau.jhtm;

/**
 *
 * @author Tim Boudreau
 */
public abstract class ProximalDendriteSegment<T extends Column> extends DendriteSegment<T, T> {

    public abstract BoostFactor getBoostFactor();

    public abstract void setBoostFactor(BoostFactor factor);

    public final void increaseBoostFactor(double by) {
        setBoostFactor(getBoostFactor().increase(by));
    }

    public final void decreaseBoostFactor(double by) {
        setBoostFactor(getBoostFactor().decrease(by));
    }
}
