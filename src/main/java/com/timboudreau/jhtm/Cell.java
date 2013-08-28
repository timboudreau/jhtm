package com.timboudreau.jhtm;

import com.timboudreau.jhtm.util.Visitor;

/**
 *
 * @author Tim Boudreau
 */
public interface Cell {
    public OutputState state();
    public <R> Visitor.Result visitDistalConnections(Visitor<DistalDendriteSegment, R> v, R arg);
    public ProximalDendriteSegment getProximalSegment();
}
