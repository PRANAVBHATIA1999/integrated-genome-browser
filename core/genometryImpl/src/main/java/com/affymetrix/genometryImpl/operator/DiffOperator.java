/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.genometryImpl.operator;

import java.util.List;

import com.affymetrix.genometryImpl.GenometryConstants;

/**
 *
 * @author auser
 */
public class DiffOperator extends AbstractGraphOperator implements Operator, Operator.Order{

	@Override
	public String getName() {
		return "diff" ;
	}

	@Override
	public String getDisplay() {
		return GenometryConstants.BUNDLE.getString("operator_" + getName());
	}

	@Override
	protected float operate(List<Float> operands) {
		return operands.get(0) - operands.get(1);
	}

	@Override
	protected String getSymbol() {
		return "-";
	}

	@Override
	public int getOrder() {
		return 2;
	}
	
}
