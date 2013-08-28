package com.timboudreau.jhtm.util;

/**
 *
 * @author Tim Boudreau
 */
public abstract class Visitor<T, R> {

    public Result visit(T obj, R arg) {
        return visit(obj);
    }

    public Result visit(T obj) {
        throw new UnsupportedOperationException("Override something");
    }

    public enum Result {

        DONE,
        NOT_DONE,
        NO_VISITS;
        
        public boolean isDone() {
            return this == DONE;
        }
    }
}
