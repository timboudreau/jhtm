/* 
 * Copyright (C) 2014 Tim Boudreau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.timboudreau.jhtm.topology;

/**
 * The location of a cell, incorporating its column's coordinate and
 * the index of it within the column.
 *
 * @author Tim Boudreau
 */
public final class CellLocation<Coordinate> {
    private final int cellIndex;
    private final Coordinate coordinate;

    public CellLocation(int cellIndex, Coordinate coordinate) {
        this.cellIndex = cellIndex;
        this.coordinate = coordinate;
    }
    
    public int index() {
        return cellIndex;
    }
    
    public Coordinate coordinate() {
        return coordinate;
    }
    
    @Override
    public boolean equals(Object o) {
        boolean result = o instanceof CellLocation;
        if (result) {
            CellLocation<?> c = (CellLocation<?>) o;
            result = c.index() == index();
            if (result) {
                result = coordinate().equals(c.coordinate());
            }
        }
        return result;
    }
    
    @Override
    public int hashCode() {
        return (index() + 1) * coordinate().hashCode() * 89821;
    }
    
    @Override
    public String toString() {
        return coordinate() + ":" + index();
    }
}
