package com.timboudreau.jhtm;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Tim Boudreau
 */
public abstract class Permanence extends Number {
    
    public static final Permanence ZERO = new Permanence() {
        @Override
        public double get() {
            return 0;
        }
    };

    Permanence() {
    }

    public abstract double get();

    public final double get(LimitFunction function) {
        return function.limit(get());
    }

    public final double getScalar() {
        return get(LimitFunction.ZERO_TO_ONE);
    }

    public static Permanence create(double value) {
        return new SimplePermanence(value);
    }

    public Permanence add(double value, boolean temporary) {
        return new CompoundPermanence(new Value(get(), false), new Value(value, temporary));
    }

    public Permanence cullTemporaryValues() {
        return this;
    }

    public Permanence retainTemporaryValues() {
        return this;
    }

    @Override
    public int intValue() {
        return (int) get();
    }

    @Override
    public long longValue() {
        return (long) get();
    }

    @Override
    public float floatValue() {
        return (float) get();
    }

    @Override
    public double doubleValue() {
        return get();
    }

    @Override
    public boolean equals(Object o) {
        return o == this;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    public static interface LimitFunction {

        public static final LimitFunction ZERO_TO_ONE = new LimitFunction() {

            public double limit(double value) {
                return Math.min(1.0D, Math.max(0D, value));
            }

        };

        double limit(double value);
    }

    private static class CompoundPermanence extends Permanence {

        private final List<Value> values = new LinkedList<Value>();

        CompoundPermanence(Value a, Value b) {
            values.add(a);
            values.add(b);
        }

        @Override
        public Permanence cullTemporaryValues() {
            double result = 0D;
            for (Value v : values) {
                if (!v.temporary) {
                    result += v.val;
                }
            }
            return new SimplePermanence(result);
        }

        @Override
        public Permanence retainTemporaryValues() {
            return new SimplePermanence(get());
        }

        @Override
        public double get() {
            double result = 0D;
            for (Value v : values) {
                result += v.val;
            }
            return result;
        }

    }

    private static class SimplePermanence extends Permanence {

        private final double value;

        SimplePermanence(double value) {
            this.value = value;
        }

        @Override
        public double get() {
            return value;
        }
    }

    private static class Value {

        final double val;
        final boolean temporary;

        public Value(double val, boolean temporary) {
            this.val = val;
            this.temporary = temporary;
        }
    }
}
