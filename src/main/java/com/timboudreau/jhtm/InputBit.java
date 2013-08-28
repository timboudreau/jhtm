package com.timboudreau.jhtm;

/**
 *
 * @author Tim Boudreau
 */
public abstract class InputBit<T> {
    public abstract boolean isActive();
    public abstract T getID();
    public abstract int index();
    @Override
    public final boolean equals(Object o) {
        if (o instanceof InputBit) {
            return getID().equals(((InputBit) o).getID());
        }
        return false;
    }
    
    @Override
    public final int hashCode() {
        return getID().hashCode();
    }
    
    @Override
    public String toString() {
        return (isActive() ? "active" : "inactive") + ":" + getID();
    }
}
