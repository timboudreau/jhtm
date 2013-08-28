package com.timboudreau.jhtm.system;

import com.timboudreau.jhtm.Column;

/**
 *
 * @author Tim Boudreau
 */
public interface Layer extends Iterable<Column> {
    public Column getColumn(int index);
    public int size();
}
