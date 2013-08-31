package com.timboudreau.jhtm.system;

import com.timboudreau.jhtm.Column;

/**
 *
 * @author Tim Boudreau
 */
public interface Layer<Coordinate> extends Iterable<Column<Coordinate>> {
    public Column<Coordinate> getColumn(int index);
    public int size();
}
