package com.timboudreau.jhtm;

import com.timboudreau.jhtm.topology.CellLocation;
import com.timboudreau.jhtm.util.Visitor;

/**
 *
 * @author Tim Boudreau
 */
public interface Cell<Coordinate> {
    OutputState state();
    <R> Visitor.Result visitDistalConnections(Visitor<DistalDendriteSegment, R> v, R arg);
    ProximalDendriteSegment getProximalSegment();
    CellLocation<Coordinate> coordinate();
}
