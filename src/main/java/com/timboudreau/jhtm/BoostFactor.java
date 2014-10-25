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

import java.io.Serializable;
import static java.lang.Math.min;

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
        return new BoostFactor(min(1.0D, multiplier - by));
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
