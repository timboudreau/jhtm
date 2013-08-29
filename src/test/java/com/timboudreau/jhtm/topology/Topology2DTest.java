package com.timboudreau.jhtm.topology;

import org.junit.Test;
import static com.timboudreau.jhtm.topology.Direction2D.*;
import static org.junit.Assert.*;
import com.timboudreau.jhtm.util.Visitor;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
/**
 *
 * @author tim
 */
public class Topology2DTest {
    
    @Test
    public void testNewCoordinate() {
        Topology2D d = new Topology2D(16);
        Path2D p = new Path2D(UP, LEFT, DOWN, RIGHT);
        final List<Coordinate2D> coords = new LinkedList<>();
        final List<Coordinate2D> expect = new LinkedList<>(Arrays.asList(new Coordinate2D(8,7), new Coordinate2D(7, 7), new Coordinate2D(7, 8), new Coordinate2D(8,8)));
        d.walk(new Coordinate2D(8, 8), p, new Visitor<Coordinate2D, Topology<Coordinate2D>>(){
            @Override
            public Visitor.Result visit(Coordinate2D obj, Topology<Coordinate2D> arg) {
                System.out.println("VISIT " + obj);
                coords.add(obj);
                return Visitor.Result.NOT_DONE;
            }
        });
        System.out.println("COORDS: " + coords);
        System.out.println("EXPECT: " + expect);
        assertEquals(expect, coords);

        coords.clear();
        p = new Path2D(DOWN, DOWN, DOWN, DOWN, DOWN, DOWN, DOWN, DOWN, DOWN, DOWN, DOWN);
        d.walk(new Coordinate2D(8, 8), p, new Visitor<Coordinate2D, Topology<Coordinate2D>>(){
            @Override
            public Visitor.Result visit(Coordinate2D obj, Topology<Coordinate2D> arg) {
                System.out.println("VISIT " + obj);
                coords.add(obj);
                return Visitor.Result.NOT_DONE;
            }
        });
    }
    
}
