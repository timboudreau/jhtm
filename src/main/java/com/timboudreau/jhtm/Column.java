package com.timboudreau.jhtm;

import com.timboudreau.jhtm.util.Visitor;

/**
 *
 * @author Tim Boudreau
 */
public interface Column<Coordinate> extends Iterable<Cell<Coordinate>> {

    Region getRegion();

    boolean isActivated();

    <R> Visitor.Result visitActivatedCells(Visitor<Cell, R> visitor, R arg);

    <R> Visitor.Result visitAllCells(Visitor<Cell, R> visitor, R arg);

    Cell getCell(int index);

    ProximalDendriteSegment getProximalSegment();

    int index();
    
    Coordinate coordinate();
}
