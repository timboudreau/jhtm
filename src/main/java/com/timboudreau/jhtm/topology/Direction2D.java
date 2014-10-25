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

import java.util.Random;

/**
 * Directions that can be navigated in 2d
 *
 * @author Tim Boudreau
 */
public enum Direction2D implements Direction<Coordinate2D> {

    UP, DOWN, LEFT, RIGHT, UP_RIGHT, DOWN_RIGHT, UP_LEFT, DOWN_LEFT;

    public static Direction2D random(Random r) {
        return Direction2D.values()[r.nextInt(Direction2D.values().length)];
    }
    
    public byte toByte() {
        return (byte) ordinal();
    }
    
    public static Direction2D fromByte(byte b) {
        return values()[b];
    }

    private int adjustX(int x) {
        switch (this) {
            case LEFT:
            case UP_LEFT:
            case DOWN_LEFT:
                return x - 1;
            case RIGHT:
            case UP_RIGHT:
            case DOWN_RIGHT:
                return x + 1;
            default:
                return x;
        }
    }

    private int adjustY(int y) {
        switch (this) {
            case UP:
            case UP_RIGHT:
            case UP_LEFT:
                return y - 1;
            case DOWN:
            case DOWN_RIGHT:
            case DOWN_LEFT:
                return y + 1;
            default:
                return y;
        }
    }

    @Override
    public Coordinate2D navigate(Coordinate2D extent, Coordinate2D curr, EdgeRule<Coordinate2D> edgeRule) {
        int x = adjustX(curr.x);
        int y = adjustY(curr.y);
        return edgeRule.adjust(curr, Coordinate2D.valueOf(x, y), extent);
    }
}
