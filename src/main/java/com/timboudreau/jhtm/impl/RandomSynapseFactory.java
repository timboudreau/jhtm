package com.timboudreau.jhtm.impl;

import com.timboudreau.jhtm.Column;
import com.timboudreau.jhtm.InputBit;
import com.timboudreau.jhtm.system.Input;
import com.timboudreau.jhtm.system.InputMapping;
import com.timboudreau.jhtm.system.InputMapping.SynapseFactory;
import com.timboudreau.jhtm.system.Layer;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Tim Boudreau
 */
public class RandomSynapseFactory<T, Coordinate> implements SynapseFactory<T, Coordinate> {

    private final Random random;
    private final int synapsesPerDendrite;

    public RandomSynapseFactory(Random random, int synapsesPerDendrite) {
        this.random = random;
        this.synapsesPerDendrite = synapsesPerDendrite;
    }

    public RandomSynapseFactory(int synapsesPerDendrite) {
        this(new Random(System.currentTimeMillis()), synapsesPerDendrite);
    }

    private List<Integer> range(int start, int end) {
        List<Integer> result = new LinkedList<>();
        for (int i = start; i < end; i++) {
            result.add(i);
        }
        return result;
    }

    @Override
    public void connect(Layer<Coordinate> layer, InputMapping.ProximalDendriteBuilder<T, Coordinate> connector, Input<T> input) {
        List<Integer> usedBits = range(0, input.size());
        for (Column<Coordinate> column : layer) {
            connector.newDendrite(column);
            for (int i = 0; i < synapsesPerDendrite; i++) {
                if (usedBits.isEmpty()) {
                    usedBits = range(0, input.size());
                }
                int ix = random.nextInt(usedBits.size());
                InputBit<T> bit = input.get(ix);
                connector.add(bit);
                usedBits.remove(Integer.valueOf(ix)); // Integer.valueOf to remove by value, not by index
            }
            connector.save();
        }
    }
}
