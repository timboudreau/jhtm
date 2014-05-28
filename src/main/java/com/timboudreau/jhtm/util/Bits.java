package com.timboudreau.jhtm.util;

import java.io.Serializable;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Potential replacement for BitSet which flips between array-of-long-bits
 * internal representations and list-of-ints internal representations,
 * depending on which will use less memory.  In a situation where the
 * majority of cells are not activated, storing a list of 4-byte ints
 * is smaller than storing a bitmap where most bits are inactive.  At a 
 * certain point, though, it crosses a line where a bitmap representation
 * will be more efficient.
 *
 * @author Tim Boudreau
 */
public abstract class Bits implements Serializable {

    public abstract Bits flip(int i);

    public abstract Bits clear();

    public abstract Bits set(int i);

    public abstract Bits clear(int i);

    public abstract boolean get(int i);

    public abstract boolean isEmpty();

    public abstract int size();

    public abstract int cardinality();

    abstract Bits toIntSetBits();

    abstract Bits toBitSetBits();

    abstract Bits copy();

    abstract int sizeInBytes();

    public final Bits clone() {
        return copy();
    }
    
    boolean isIntSetBits() {
        return this instanceof IntSetBits;
    }

    public Set<Integer> setBits() {
        int sz = size();
        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < sz; i++) {
            if (get(i)) {
                set.add(i);
            }
        }
        return set;
    }

    public boolean equals(Object o) {
        return o instanceof Bits && ((Bits) o).setBits().equals(setBits());
    }

    public int hashCode() {
        return setBits().hashCode();
    }
    
    public static Bits create(int size) {
        return new MetaBits(new IntSetBits(size));
    }

    static class MetaBits extends WrapperBits {

        private static final int INTERVAL = 10;
        private static final int THRESHOLD = 10;
        private volatile int count = 0;

        public MetaBits(Bits wrapped) {
            super(wrapped);
        }

        void check(Bits inner) {
            assert Thread.holdsLock(this);
            if (++count % INTERVAL == 0) {
                int cardinality = inner.cardinality();
                int sz = size();
                int bytesToStoreCurrentCardinalityAsInts = Integer.SIZE * cardinality;
                int bytesToStoreCurrentCardinalityAsBits = sz / 8;
                if (inner.isIntSetBits() && bytesToStoreCurrentCardinalityAsInts > (bytesToStoreCurrentCardinalityAsBits + THRESHOLD)) {
                    setInner(inner.toBitSetBits());
                }
            }
        }

        protected synchronized Bits inner() {
            Bits inner = super.inner();
            check(inner);
            return inner;
        }

        @Override
        boolean isIntSetBits() {
            return super.inner().isIntSetBits();
        }

        @Override
        public Bits flip(int i) {
            inner().flip(i);
            return this;
        }

        @Override
        public Bits clear() {
            inner().clear();
            return this;
        }

        @Override
        public Bits set(int i) {
            inner().set(i);
            return this;
        }

        @Override
        public Bits clear(int i) {
            inner().clear(i);
            return this;
        }

        @Override
        public boolean get(int i) {
            return inner().get(i);
        }

        @Override
        public boolean isEmpty() {
            return inner().isEmpty();
        }

        @Override
        public int size() {
            return inner().size();
        }

        @Override
        public int cardinality() {
            return inner().cardinality();
        }

        @Override
        Bits toIntSetBits() {
            return inner().toIntSetBits();
        }

        @Override
        Bits toBitSetBits() {
            return inner().toBitSetBits();
        }

    }

    static abstract class WrapperBits extends Bits {

        private Bits inner;

        public WrapperBits(Bits wrapped) {
            this.inner = wrapped;
        }

        protected synchronized Bits inner() {
            return inner;
        }
        
        synchronized void setInner(Bits inner) {
            this.inner = inner;
        }

        public boolean equals(Object o) {
            return inner().equals(o);
        }

        public int hashCode() {
            return inner().hashCode();
        }

        Bits copy() {
            return inner().copy();
        }

        int sizeInBytes() {
            return inner().sizeInBytes();
        }
    }

    private static class SynchronizedBits extends WrapperBits {

        public SynchronizedBits(Bits inner) {
            super(inner);
        }

        @Override
        public synchronized Bits flip(int i) {
            return inner().flip(i);
        }

        @Override
        public synchronized Bits clear() {
            return inner().clear();
        }

        @Override
        public synchronized Bits set(int i) {
            return inner().set(i);
        }

        @Override
        public synchronized Bits clear(int i) {
            return inner().clear(i);
        }

        @Override
        public synchronized boolean get(int i) {
            return inner().get(i);
        }

        @Override
        public synchronized boolean isEmpty() {
            return inner().isEmpty();
        }

        @Override
        public synchronized int size() {
            return inner().size();
        }

        @Override
        synchronized Bits toIntSetBits() {
            if (inner() instanceof IntSetBits) {
                return this;
            } else {
                return new SynchronizedBits(inner().toIntSetBits());
            }
        }

        @Override
        synchronized Bits toBitSetBits() {
            if (inner() instanceof BitSetBits) {
                return this;
            } else {
                return new SynchronizedBits(inner().toBitSetBits());
            }
        }

        @Override
        synchronized Bits copy() {
            return new SynchronizedBits(inner().copy());
        }

        @Override
        public synchronized int cardinality() {
            return inner().cardinality();
        }

        public synchronized boolean equals(Object o) {
            return super.equals(o);
        }

        public synchronized int hashCode() {
            return super.hashCode();
        }

        public synchronized Set<Integer> setBits() {
            return inner().setBits();
        }
    }

    private static class IntSetBits extends Bits {

        private final Set<Integer> ints = new HashSet<>();
        private int size;

        IntSetBits(int size, Set<Integer> ints) {
            this(size);
            this.ints.addAll(ints);
        }

        IntSetBits(int size) {
            this.size = size;
        }

        @Override
        public Bits flip(int i) {
            if (!ints.contains(i)) {
                ints.add(i);
            } else {
                ints.remove(i);
            }
            return this;
        }

        @Override
        public Bits clear() {
            ints.clear();
            return this;
        }

        @Override
        public Bits set(int i) {
            ints.add(i);
            return this;
        }

        @Override
        public Bits clear(int i) {
            ints.remove(i);
            return this;
        }

        @Override
        public boolean get(int i) {
            return ints.contains(i);
        }

        @Override
        public boolean isEmpty() {
            return ints.isEmpty();
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        BitSetBits toBitSetBits() {
            BitSet bs = new BitSet();
            for (int i : ints) {
                bs.set(i);
            }
            return new BitSetBits(bs);
        }

        @Override
        IntSetBits toIntSetBits() {
            return this;
        }

        @Override
        Bits copy() {
            return new IntSetBits(size, ints);
        }

        @Override
        public int cardinality() {
            return ints.size();
        }


        @Override
        int sizeInBytes() {
            return Integer.SIZE * ints.size();
        }
    }

    private static class BitSetBits extends Bits {

        private final BitSet bits;

        public BitSetBits(BitSet bits) {
            this.bits = bits;
        }

        public BitSetBits(int size) {
            this.bits = new BitSet(size);
        }

        public BitSetBits flip(int i) {
            bits.flip(i);
            return this;
        }

        public BitSetBits set(int i) {
            bits.set(i);
            return this;
        }

        public BitSetBits clear(int i) {
            bits.clear(i);
            return this;
        }

        public BitSetBits clear() {
            bits.clear();
            return this;
        }

        public boolean get(int i) {
            return bits.get(i);
        }

        public boolean isEmpty() {
            return bits.isEmpty();
        }

        public int size() {
            return bits.length();
        }

        public boolean equals(Object o) {
            if (o instanceof BitSetBits) {
                return ((BitSetBits) o).bits.equals(bits) && ((Bits) o).size() == size();
            } else {
                return super.equals(o);
            }
        }

        IntSetBits toIntSetBits() {
            int size = this.bits.size();
            IntSetBits bits = new IntSetBits(size);
            for (int i = 0; i < size; i++) {
                if (this.bits.get(i)) {
                    bits.set(i);
                }
            }
            return bits;
        }

        @Override
        Bits toBitSetBits() {
            return this;
        }

        @Override
        Bits copy() {
            return new BitSetBits((BitSet) bits.clone());
        }

        @Override
        public int cardinality() {
            return bits.cardinality();
        }

        public int sizeInBytes() {
            return bits.length() / Integer.SIZE;
        }
    }
}
