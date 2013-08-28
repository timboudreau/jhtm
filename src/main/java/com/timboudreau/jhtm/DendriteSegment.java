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
