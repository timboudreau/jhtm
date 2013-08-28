package com.timboudreau.jhtm;

/**
 *
 * @author Tim Boudreau
 */
public enum OutputState {

    INACTIVE,
    ACTIVE,
    PREDICTED;

    public static OutputState forState(boolean active, boolean predictive) {
        if (active) {
            return predictive ? PREDICTED : ACTIVE;
        }
        return INACTIVE;
    }

    public boolean isActive() {
        return this != INACTIVE;
    }
}
