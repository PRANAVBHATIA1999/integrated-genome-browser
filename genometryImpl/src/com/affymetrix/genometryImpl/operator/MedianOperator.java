/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.genometryImpl.operator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.affymetrix.genometryImpl.GenometryConstants;

/**
 *
 * @author auser
 */
public class MedianOperator extends AbstractGraphOperator implements Operator{

	@Override
	protected String getSymbol() {
		return null;
	}

	@Override
	protected float operate(List<Float> operands) {
		List<Float> sortOperands = new ArrayList<Float>(operands);
		Collections.sort(sortOperands);
		float median = (operands.size() % 2 == 0) ?
			(float)((sortOperands.get((operands.size() / 2) - 1) + sortOperands.get(operands.size() / 2)) / 2.0) :
			sortOperands.get((operands.size() - 1) / 2);
		return median;
	}
	
	@Override
	public String getName() {
		return "median";
	}

	@Override
	public String getDisplay() {
		return GenometryConstants.BUNDLE.getString("operator_" + getName());
	}
}
