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
