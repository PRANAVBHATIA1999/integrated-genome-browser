/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.genometry.operator;

import com.affymetrix.genometry.GenometryConstants;
import java.util.List;

/**
 *
 * @author auser
 */
public class RatioOperator extends AbstractGraphOperator implements Operator, Operator.Order {

    @Override
    protected String getSymbol() {
        return "/";
    }

    @Override
    protected float operate(List<Float> operands) {
        if (operands.get(1) == 0.0) {
            return 0.0f;
        }
        return operands.get(0) / operands.get(1);
    }

    @Override
    public String getName() {
        return "ratio";
    }

    @Override
    public String getDisplay() {
        return GenometryConstants.BUNDLE.getString("operator_" + getName());
    }

    @Override
    public int getOrder() {
        return 4;
    }
}
