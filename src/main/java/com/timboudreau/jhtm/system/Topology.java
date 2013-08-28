package com.timboudreau.jhtm.system;

import com.timboudreau.jhtm.Cell;
import com.timboudreau.jhtm.Column;
import com.timboudreau.jhtm.util.Visitor;

/**
 *
 * @author Tim Boudreau
 */
public interface Topology<Coordinate> {
    public <R> Visitor.Result visitNeighbors(Layer layer, int radius, Visitor<Column, R> v, Column column, R arg);
    public Column getColumn(Layer layer, Coordinate coord);
    public int columnCount();
}
