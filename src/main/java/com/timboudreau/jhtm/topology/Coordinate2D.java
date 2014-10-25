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
 * A coordinate in a 2d topology
 *
 * @author Tim Boudreau
 */
public class Coordinate2D {

    public final int x;
    public final int y;
    private static final int INTERN_TABLE_SIZE = 1024;
    private static final Coordinate2D[][] INTERN_TABLE = new Coordinate2D[INTERN_TABLE_SIZE][INTERN_TABLE_SIZE];

    static {
        for (int i = 0; i < INTERN_TABLE_SIZE; i++) {
            for (int j = 0; j < INTERN_TABLE_SIZE; j++) {
                INTERN_TABLE[i][j] = new Coordinate2D(i, j);
            }
        }
    }

    public static Coordinate2D valueOf(int x, int y) {
        if (x > 0 && x < INTERN_TABLE_SIZE && y > 0 && y < INTERN_TABLE_SIZE) {
            return INTERN_TABLE[x][y];
        }
        return new Coordinate2D(x, y);
    }

    Coordinate2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        return o == this ? true : o instanceof Coordinate2D && ((Coordinate2D) o).x == x && ((Coordinate2D) o).y == y;
    }

    @Override
    public int hashCode() {
        int base = (65536 * y) + x;
        return base;
    }

    @Override
    public String toString() {
        return x + "," + y;
    }

    public int toOffset(Topology2D top) {
        int maxX = top.width - 1;
        return (maxX * y) + x;
    }
}
