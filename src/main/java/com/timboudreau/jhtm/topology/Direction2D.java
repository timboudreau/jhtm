package com.timboudreau.jhtm.topology;

import java.util.Random;

/**
 *
 * @author Tim Boudreau
 */
public enum Direction2D implements Direction<Coordinate2D> {

    UP, DOWN, LEFT, RIGHT, UP_RIGHT, DOWN_RIGHT, UP_LEFT, DOWN_LEFT;

    public static Direction2D random(Random r) {
        return Direction2D.values()[r.nextInt(Direction2D.values().length)];
    }

    private int adjustX(int x, int maxX) {
        switch (this) {
            case LEFT:
            case UP_LEFT:
            case DOWN_LEFT:
                return Math.min(maxX, Math.max(x - 1, 0));
            case RIGHT:
            case UP_RIGHT:
            case DOWN_RIGHT:
                return Math.min(maxX, Math.max(x + 1, 0));
            default:
                return x;
        }
    }

    private int adjustY(int y, int maxY) {
        assert maxY > 0;
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
        int x = adjustX(curr.x, extent.x() - 1);
        int y = adjustY(curr.y, extent.y() - 1);
        return edgeRule.adjust(curr, new Coordinate2D(x, y), extent);
    }
}
