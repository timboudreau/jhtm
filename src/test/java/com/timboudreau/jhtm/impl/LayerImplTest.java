package com.timboudreau.jhtm.impl;

import com.timboudreau.jhtm.topology.Coordinate2D;
import com.timboudreau.jhtm.topology.Topology2D;
import com.timboudreau.jhtm.Cell;
import com.timboudreau.jhtm.DistalDendriteSegment;
import com.timboudreau.jhtm.PotentialSynapse;
import com.timboudreau.jhtm.impl.LayerImpl.CellImpl;
import com.timboudreau.jhtm.topology.Direction;
import com.timboudreau.jhtm.topology.Path;
import com.timboudreau.jhtm.util.Visitor;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tim
 */
public class LayerImplTest {

    @Test
    public void test() {
        System.out.println("Okay");
        long start = System.currentTimeMillis();
        Topology2D topo = new Topology2D(18);
        LayerImpl layer = new LayerImpl(4, 24, topo, new LayerImpl.RandomDistalLayoutFactory<>(24));
        System.out.println("Ready");
        final Set<CellImpl> cells = new HashSet<>();

        final int[] totalSynapses = new int[1];
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                System.out.println("Start watcher thread");
                for (;;) {
                    try {
                        Thread.sleep(5000);
                        System.out.println(totalSynapses[0] + " synapses visited");
                    } catch (Exception ex) {
                        Logger.getLogger(LayerImplTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }, "Watcher thread");
        System.out.println("Created " + layer.size() + " cells in " + ((System.currentTimeMillis() - start) / 1000) + " seconds");
        start = System.currentTimeMillis();
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();

        for (int i = 0; i < layer.cellCount(); i++) {
            CellImpl cell = layer.getCell(i);
            assertNotNull("" + i, cell);
            assertFalse("Saw " + i + " twice", cells.contains(cell));
            cells.add(cell);
//            System.out.println("CELL " + cell + " at " + cell.coordinate() + " column " + cell.columnIndex() + " ix " + cell.indexInColumn() + " abs " + cell.index());
            Path<Coordinate2D, ? extends Direction<Coordinate2D>>[] pths = ((CellImpl) cell).getPaths();
            assertNotNull(pths);
            assertNotSame(0, pths.length);

            final int[] segCount = new int[1];
            final int[] synCount = new int[1];
            cell.visitDistalConnections(new Visitor<DistalDendriteSegment, Void>() {

                @Override
                public Visitor.Result visit(DistalDendriteSegment obj) {
//                    System.out.println("Visit " + segCount[0] + " " + obj);
                    segCount[0]++;
                    final int[] syncount = new int[1];
                    obj.visitSynapses(new Visitor<PotentialSynapse<? extends Cell>, Void>() {

                        Set<PotentialSynapse> seen = new HashSet<>();

                        @Override
                        public Visitor.Result visit(PotentialSynapse obj) {
                            syncount[0]++;
                            totalSynapses[0]++;
//                            System.out.println("Visit syn " + synCount[0] + " " + obj);
                            assertFalse("Visited twice", seen.contains(obj));
                            synCount[0]++;
                            assertEquals(0D, obj.getPermanence().get(), 0.01D);
                            obj.adjustPermanence(0.25D, true);
                            assertEquals(0.25D, obj.getPermanence().get(), 0.01D);
                            return Visitor.Result.NOT_DONE;
                        }
                    }, null);
                    assertNotSame("No synapses found on " + obj, 0, syncount[0]);
                    return Visitor.Result.NOT_DONE;
                }
            }, null);
        }
        long dur = (System.currentTimeMillis() - start) / 1000;
        System.out.println("Visited " + totalSynapses[0] + " synapses in " + dur + " seconds. Memory: " + Runtime.getRuntime().totalMemory());
    }
}
