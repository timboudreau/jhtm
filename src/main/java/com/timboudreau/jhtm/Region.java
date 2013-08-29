package com.timboudreau.jhtm;

import com.timboudreau.jhtm.util.Visitor;

/**
 *
 * @author Tim Boudreau
 */
public abstract class Region<Coordinate> {

    public abstract int size();

    public abstract Column<Coordinate> get(int ix);
    
    public abstract Column<Coordinate> get(Coordinate coord);

    public <R> Visitor.Result visitAllCells(final Visitor<Cell, R> v, R arg) {
        return visitActivatedColumns(new Visitor<Column, R>() {

            @Override
            public Visitor.Result visit(Column obj, R arg) {
                return obj.visitAllCells(v, arg);
            }

        }, arg);
    }

    public <R> Visitor.Result visitActivatedColumns(Visitor<Column, R> v, R arg) {
        int sz = size();
        Visitor.Result result = Visitor.Result.NO_VISITS;
        for (int i = 0; i < sz; i++) {
            Column c = get(i);
            if (c.isActivated()) {
                result = v.visit(c, arg);
                if (result == Visitor.Result.DONE) {
                    break;
                }
            }
        }
        return result;
    }
}
