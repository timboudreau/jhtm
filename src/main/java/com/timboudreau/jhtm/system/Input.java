package com.timboudreau.jhtm.system;

import com.timboudreau.jhtm.InputBit;

/**
 *
 * @author Tim Boudreau
 */
public interface Input<T> extends Iterable<InputBit<T>> {
    public int size();
    public InputBit<T> get(int i);
    public InputBit<T> get(T id);
}
