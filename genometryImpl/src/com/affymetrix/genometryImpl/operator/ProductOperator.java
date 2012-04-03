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
public class ProductOperator extends AbstractGraphOperator implements Operator{

	@Override
	protected String getSymbol() {
		return null;
	}

	@Override
	protected float operate(List<Float> operands) {
		float total = 1;
		for (Float f : operands) {
			total *= f.floatValue();
		}
		return total;
	}
	
	@Override
	public String getName() {
		return "product";
	}

	@Override
	public String getDisplay() {
		return GenometryConstants.BUNDLE.getString("operator_" + getName());
	}
}
