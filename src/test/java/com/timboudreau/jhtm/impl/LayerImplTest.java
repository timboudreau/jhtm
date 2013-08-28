package com.timboudreau.jhtm.impl;

import com.timboudreau.jhtm.Cell;
import com.timboudreau.jhtm.Column;
import com.timboudreau.jhtm.DistalDendriteSegment;
import com.timboudreau.jhtm.PotentialSynapse;
import com.timboudreau.jhtm.impl.LayerImpl.CellImpl;
import com.timboudreau.jhtm.impl.Topology2D.ColumnMapper;
import com.timboudreau.jhtm.util.Visitor;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tim
 */
public class LayerImplTest {

    @Test
    public void test() {
        Topology2D topo = new Topology2D(16, 16, new ColumnMapper() {
            @Override
            public Coordinate2D coordinatesOf(Column cell) {
                return ((CellImpl) cell).coordinate();
            }
        });
        Random r = new Random(23);
        LayerImpl layer = new LayerImpl(topo.columnCount(), 4, 24, r, topo, 7);

        final Set<CellImpl> cells = new HashSet<>();
        
        for (int i = 0; i < layer.cellCount(); i++) {
            CellImpl cell = layer.getCell(i);
            assertFalse("Saw " + cell + " twice", cells.contains(cell));
            cells.add(cell);
            assertNotNull("" + i, cell);
            System.out.println("CELL " + cell + " at " + cell.coordinate() + " column " + cell.columnIndex() + " ix " + cell.indexInColumn() + " abs " + cell.index() );
            DendritePath[] pths = cell.getPaths();
            
            final int[] segCount = new int[1];
            final int[] synCount = new int[1];
            cell.visitDistalConnections(new Visitor<DistalDendriteSegment, Void> () {

                @Override
                public Visitor.Result visit(DistalDendriteSegment obj) {
                    System.out.println("Visit " + segCount[0] + " " + obj);
                    segCount[0]++;
                    obj.visitSynapses(new Visitor<PotentialSynapse<? extends Cell>, Void>() {
                        
                        Set<PotentialSynapse> seen = new HashSet<>();

                        @Override
                        public Visitor.Result visit(PotentialSynapse obj) {
                            System.out.println("Visit syn " + synCount[0] + " " + obj);
                            assertFalse("Visited " + obj + " twice", seen.contains(obj));
                            synCount[0] ++;
                            assertEquals(0D, obj.getPermanence().get(), 0.01D);
                            obj.adjustPermanence(0.25D, true);
                            assertEquals(0.25D, obj.getPermanence().get(), 0.01D);
                            return Visitor.Result.NOT_DONE;
                        }
                    }, null);
                    return Visitor.Result.NOT_DONE;
                }
                
            }, null);
            
            System.out.println("Visited " + segCount[0] + " segments and " + synCount[0] + " synapses");
        }
    }
}
