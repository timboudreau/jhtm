package com.timboudreau.jhtm;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.System.identityHashCode;
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
        return identityHashCode(this);
    }

    public static interface LimitFunction {

        public static final LimitFunction ZERO_TO_ONE = new LimitFunction() {

            public double limit(double value) {
                return min(1.0D, max(0D, value));
            }

        };

        double limit(double value);
    }

    private static class CompoundPermanence extends Permanence {

        private final Value a;
        private final Value b;
        CompoundPermanence(Value a, Value b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public Permanence cullTemporaryValues() {
            double result = 0D;
            if (!a.temporary) {
                result+=a.val;
            }
            if (!b.temporary) {
                result+=b.val;
            }
            return new SimplePermanence(result);
        }

        @Override
        public Permanence retainTemporaryValues() {
            return new SimplePermanence(get());
        }

        @Override
        public double get() {
            return a.val + b.val;
        }
        
        public String toString() {
            return Double.toString(get()) + " (" + a + ", " + b + ")";
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

        public String toString() {
            return Double.toString(value);
        }
    }

    private static class Value {

        final double val;
        final boolean temporary;

        Value(double val, boolean temporary) {
            this.val = val;
            this.temporary = temporary;
        }
    }
}
