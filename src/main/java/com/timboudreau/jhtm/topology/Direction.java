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
 * An instruction for navigating away from one location to another in
 * a topology.  For example, left, right, up, down.
 *
 * @author Tim Boudreau
 */
public interface Direction<Coordinate> {
    /**
     * Return a coordinate that reflects adjusting the passed current
     * coordinate by this direction, and applying the passed edge rule.
     * 
     * @param extents The coordinate which valid coordinates must be less than in
     * all directions
     * @param curr The current coordinate
     * @param edgeRule A rule for what to do when a coordinate does not fit within
     * the extents that were passed
     * @return A coordinate
     */
    Coordinate navigate(Coordinate extents, Coordinate curr, EdgeRule<Coordinate> edgeRule);
}
