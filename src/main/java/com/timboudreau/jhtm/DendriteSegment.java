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

import com.timboudreau.jhtm.util.Visitor;

/**
 *
 * @author Tim Boudreau
 */
public abstract class DendriteSegment<T, Q> {

    public abstract T getSource();

    public abstract <R> Visitor.Result visitSynapses(Visitor<PotentialSynapse<? extends Q>, R> visitor, R arg);

    public int countSynapsesAboveThreshold(final double threshold) {
        int[] result = new int[1];
        visitSynapses(new Visitor<PotentialSynapse<? extends Q>, int[]>() {

            @Override
            public Visitor.Result visit(PotentialSynapse synapse, int[] arg) {
                if (synapse.getPermanence().get(Permanence.LimitFunction.ZERO_TO_ONE) >= threshold) {
                    arg[0]++;
                }
                return Visitor.Result.NOT_DONE;
            }

        }, result);
        return result[0];
    }
}
