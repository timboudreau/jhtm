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

import java.io.Serializable;

/**
 * Am immutable sequence of instructions for navigating a topology.
 *
 * @author Tim Boudreau
 */
public interface Path<Coordinate, Dir extends Direction<Coordinate>> extends Iterable<Dir>, Serializable {
    /**
     * Add a path, returning a new instance of Path with the passed
     * direction added.  Does not alter the instance this method is called on.
     * @param dir
     * @return 
     */
    Path add(Dir dir);
    /**
     * Iterate this path as a set of coordinates atrting at the passed
     * start coordinate, applying the edge rule in the context of the
     * extents.
     * 
     * @param start The starting location
     * @param extents The maximum location all valid locations are less than
     * @param edgeRule The rule for what to do if the path specifies an invalid
     * location
     * @return An iterable 
     */
    Iterable<Coordinate> coordinates(Coordinate start, Coordinate extents, EdgeRule<Coordinate> edgeRule);
    int length();
}
