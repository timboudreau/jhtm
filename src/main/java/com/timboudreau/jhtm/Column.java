package com.timboudreau.jhtm;

import com.timboudreau.jhtm.util.Visitor;

/**
 *
 * @author Tim Boudreau
 */
public interface Column {
    public Region getRegion();
    public boolean isActivated();
    public <R> Visitor.Result visitActivatedCells(Visitor<Cell, R> visitor, R arg);
    public <R> Visitor.Result visitAllCells(Visitor<Cell, R> visitor, R arg);
    public Cell getCell(int index);
    public ProximalDendriteSegment getProximalSegment();
}
