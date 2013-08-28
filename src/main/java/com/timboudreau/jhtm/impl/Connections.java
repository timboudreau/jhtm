package com.timboudreau.jhtm.impl;

import com.timboudreau.jhtm.Cell;
import com.timboudreau.jhtm.DistalDendriteSegment;
import com.timboudreau.jhtm.OutputState;
import com.timboudreau.jhtm.Permanence;
import com.timboudreau.jhtm.PotentialSynapse;
import com.timboudreau.jhtm.impl.LayerImpl.CellImpl;
import com.timboudreau.jhtm.util.Visitor;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Manages a set of synapses and their connections.
 *
 * @author Tim Boudreau
 */
class Connections {

    private final LayerImpl layer;
    private final int connectionsPerCell;
//    private final Map<Integer, Collection<Synapse>> synapses = new HashMap<>();
    private final InitialConnectionsFactory factory;

    public Connections(LayerImpl layer, int connectionsPerCell, InitialConnectionsFactory factory) {
        this.layer = layer;
        this.connectionsPerCell = connectionsPerCell;
        this.factory = factory;
    }

    interface InitialConnectionsFactory {

        public int[] createInitialConnections(int cell);
    }
    
    
    
    /*

     public Collection<Synapse> getSynapses(CellImpl cell) {
     int ix = cell.index();
     Collection<Synapse> s = synapses.get(ix);
     if (s == null) {
     s = new LinkedList<>();
     for (int i = 0; i < connectionsPerCell; i++) {
     int[] vals = factory.createInitialConnections(ix);
     s.add(new Synapse(vals));
     }
     }
     return s;
     }


    
     <R> Visitor.Result visitConnectionsForCell(CellImpl cell, Visitor<DistalDendriteSegment, R> v, R arg) {
     int ix = cell.index();
     Visitor.Result result = Visitor.Result.NO_VISITS;
     for (Synapse s : getSynapses(cell)) {
     DistalDendriteSegment seg = s.segment(ix);
     result = v.visit(seg, arg);
     if (result.isDone()) {
     break;
     }
     }
     return result;
     }
     private class Synapse extends PotentialSynapse {

     private Permanence permanence = Permanence.create(0);
     private final int[] connects;

     public Synapse(int[] connects) {
     this.connects = connects;
     Arrays.sort(connects);
     }

     public synchronized Permanence add(double value, boolean temporary) {
     return permanence = permanence.add(value, temporary);
     }

     public synchronized Permanence cullTemporaryValues() {
     return permanence = permanence.cullTemporaryValues();
     }

     public synchronized Permanence retainTemporaryValues() {
     return permanence = permanence.retainTemporaryValues();
     }

     @Override
     public synchronized Permanence getPermanence() {
     return permanence;
     }

     private LayerImpl layer() {
     return layer;
     }

     @Override
     public boolean equals(Object o) {
     return o instanceof Synapse && ((Synapse) o).layer() == layer() && Arrays.equals(connects, ((Synapse) o).connects);
     }

     @Override
     public int hashCode() {
     return Arrays.hashCode(connects);
     }

     @Override
     public <R> Visitor.Result visitConnectionsInState(Visitor<DistalDendriteSegment, R> v, R r, OutputState... states) {
     Visitor.Result result = Visitor.Result.NO_VISITS;
     if (states.length == 0) {
     return result;
     }
     EnumSet<OutputState> set = EnumSet.copyOf(Arrays.asList(states));
     for (int i = 0; i < connects.length; i++) {
     Seg seg = new Seg(connects[i]);
     if (set.contains(seg.getSource().state())) {
     result = v.visit(seg, r);
     if (result.isDone()) {
     break;
     }
     }
     }
     return result;
     }

     @Override
     public <R> Visitor.Result visitConnections(Visitor<DistalDendriteSegment, R> v, R r) {
     Visitor.Result result = Visitor.Result.NO_VISITS;
     for (int i = 0; i < connects.length; i++) {
     Seg seg = new Seg(connects[i]);
     result = v.visit(seg, r);
     if (result.isDone()) {
     break;
     }
     }
     return result;
     }
        
     private Seg segment(int cell) {
     return new Seg(cell);
     }

     private class Seg extends DistalDendriteSegment {

     private final int cell;

     public Seg(int cell) {
     this.cell = cell;
     }

     @Override
     public Cell getSource() {
     return layer.getCell(cell);
     }

     @Override
     public PotentialSynapse getTarget() {
     return Synapse.this;
     }

     @Override
     public boolean equals(Object o) {
     return o instanceof Seg && ((Seg) o).cell == cell && ((Seg) o).getTarget() == getTarget();
     }

     @Override
     public int hashCode() {
     return cell * (1 + getTarget().hashCode());
     }
     }
     }
     */
}
