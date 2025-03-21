package com.affymetrix.genometry.operator.comparator;

/**
 *
 * @author hiralv
 */
public class GreaterThanMathComparisonOperator implements MathComparisonOperator {

    @Override
    public boolean operate(int i1, int i2) {
        return i1 > i2;
    }

    @Override
    public boolean operate(long l1, long l2) {
        return l1 > l2;
    }

    @Override
    public boolean operate(float f1, float f2) {
        return f1 > f2;
    }

    @Override
    public boolean operate(double d1, double d2) {
        return d1 > d2;
    }

    @Override
    public String getName() {
        return "greater_than";
    }

    @Override
    public String getDisplay() {
        return ">";
    }
}
