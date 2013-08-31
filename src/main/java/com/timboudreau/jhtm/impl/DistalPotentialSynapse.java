package com.timboudreau.jhtm.impl;

import com.timboudreau.jhtm.Cell;
import com.timboudreau.jhtm.DendriteSegment;
import com.timboudreau.jhtm.OutputState;
import com.timboudreau.jhtm.Permanence;
import com.timboudreau.jhtm.PotentialSynapse;

/**
 *
 * @author Tim Boudreau
 */
public class DistalPotentialSynapse<Coordinate> extends PotentialSynapse<Cell<Coordinate>> {

    private final int pathIndex;
    private final DistalDendriteSegmentImpl<Coordinate> segment;
    private final CellImpl<Coordinate> cell;

    public DistalPotentialSynapse(int pathIndex, DistalDendriteSegmentImpl<Coordinate> outer, CellImpl<Coordinate> cell) {
        this.pathIndex = pathIndex;
        this.segment = outer;
        this.cell = cell;
    }

    @Override
    public String toString() {
        return "Synapse " + segment.cell + " in " + segment;
    }

    @Override
    public Permanence getPermanence() {
        return segment.cell.layer.snapshot.getPermanences(segment.path).getPermanence(pathIndex, cell.indexInColumn());
    }

    private DistalDendriteSegmentImpl seg() {
        return segment;
    }

    private CellImpl dest() {
        return segment.cell;
    }

    @Override
    public int hashCode() {
        return (seg().hashCode() + (41 * segment.cell.hashCode())) * (pathIndex + 1);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        boolean result = o != null && o.getClass() == DistalPotentialSynapse.class;
        if (result) {
            DistalPotentialSynapse syn = (DistalPotentialSynapse) o;
            result = syn.pathIndex == pathIndex;
            if (result) {
                result = syn.getTarget().equals(segment.cell);
                if (result) {
                    result = syn.seg().equals(seg());
                    if (result) {
                        result = segment.equals(syn.dest());
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Cell getTarget() {
        return segment.cell;
    }

    @Override
    public Permanence adjustPermanence(double amount, boolean temporary) {
        return segment.cell.layer.snapshot.getPermanences(segment.path).updatePermanence(pathIndex, cell.indexInColumn(), amount, temporary);
    }

    @Override
    public Permanence setPermanence(Permanence pp) {
        return segment.cell.layer.snapshot.getPermanences(segment.path).setPermanence(cell.index(), cell.columnIndex(), pp);
    }

    @Override
    public DendriteSegment getDendriteSegment() {
        return segment;
    }

    @Override
    public OutputState getTargetState() {
        return cell.state();
    }
}
