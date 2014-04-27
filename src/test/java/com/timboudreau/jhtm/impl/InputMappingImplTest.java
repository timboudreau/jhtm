package com.timboudreau.jhtm.impl;

import com.timboudreau.jhtm.topology.Coordinate2D;
import com.timboudreau.jhtm.topology.Topology2D;
import com.timboudreau.jhtm.Column;
import com.timboudreau.jhtm.InputBit;
import com.timboudreau.jhtm.PotentialSynapse;
import com.timboudreau.jhtm.ProximalDendriteSegment;
import com.timboudreau.jhtm.system.Input;
import com.timboudreau.jhtm.system.Thresholds;
import com.timboudreau.jhtm.util.Visitor;
import java.util.Iterator;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tim
 */
public class InputMappingImplTest {

    @Test
    public void testConnector() {
        Topology2D topo = new Topology2D(16, 16);
        Random r = new Random(23);
        LayerImpl<Coordinate2D> layer = new LayerImpl(4, 24, topo);
        In in = new In(81);
        InputMappingImpl<Coordinate2D, Coordinate2D> mapping = new InputMappingImpl<>(in, new RandomSynapseFactory(23), layer, new Thresholds());
        layer.setInputMapping(mapping);
        Iterator<Column<Coordinate2D>> it = layer.iterator();
        while (it.hasNext()) {
            final Column<Coordinate2D> c = it.next();
            assertNotNull(c);
            ProximalDendriteSegment seg = c.getProximalSegment();
            assertNotNull("Null segment for " + c, seg);
            seg.increaseBoostFactor(1.2);
            System.out.println("Visit synapses in " + ((ColumnImpl)c).index);
            seg.visitSynapses(new Visitor<PotentialSynapse<?>, Void>() {

                @Override
                public Visitor.Result visit(PotentialSynapse<?> obj) {
                    System.out.println("VISIT " + obj + " for " + c);
                    return Visitor.Result.NOT_DONE;
                }

            }, null);
        }
    }

    private static class In implements Input<Coordinate2D> {

        private final int size;
        private final boolean[] vals;
        private final int rowSize;

        public In(int size) {
            this.size = size;
            rowSize = (int) Math.sqrt(size);

            assert rowSize * rowSize == size;
            vals = new boolean[size];
            for (int i = 0; i < size; i++) {
                vals[i] = i % 3 == 0;
            }
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public InputBit<Coordinate2D> get(final int i) {
            return new InputBit<Coordinate2D>() {

                @Override
                public boolean isActive() {
                    return vals[i];
                }

                @Override
                public Coordinate2D getID() {
                    int y = i / rowSize;
                    int x = i % rowSize;
                    return Coordinate2D.valueOf(x, y);
                }

                @Override
                public int index() {
                    return i;
                }
            };
        }

        @Override
        public InputBit<Coordinate2D> get(Coordinate2D id) {
            int offset = (id.y * rowSize) + (id.x);
            return get(offset);
        }

        @Override
        public Iterator<InputBit<Coordinate2D>> iterator() {
            return new Iterator<InputBit<Coordinate2D>>() {
                int ix = -1;

                @Override
                public boolean hasNext() {
                    return ix + 1 < size;
                }

                @Override
                public InputBit<Coordinate2D> next() {
                    return get(++ix);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Not supported");
                }
            };
        }
    }
}
