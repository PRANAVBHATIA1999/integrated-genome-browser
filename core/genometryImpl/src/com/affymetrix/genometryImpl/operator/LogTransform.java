package com.affymetrix.genometryImpl.operator;


public final class LogTransform extends AbstractLogTransform implements Operator{
	private static final String BASE_NAME = "log";
	double LN_BASE;
	float LOG_1;

	public LogTransform() {
		super();
	}
	
	public LogTransform(Double base) {
		super();
		LN_BASE = Math.log(base);
		LOG_1 = (float)(Math.log(1)/LN_BASE);
	}

	@Override
	protected String getBaseName() {
		return BASE_NAME;
	}
	
	public float transform(float x) {
		return (x <= 1) ? LOG_1 : (float)(Math.log(x)/LN_BASE);
	}

	@Override
	protected boolean setParameter(String s) {
		if (parameterized && super.setParameter(s)) {
			if (!("e".equals(s.trim().toLowerCase()))) {
				LN_BASE = Math.log(base);
				LOG_1 = (float) (Math.log(1) / LN_BASE);
			}
		}
		return true;
	}
}
