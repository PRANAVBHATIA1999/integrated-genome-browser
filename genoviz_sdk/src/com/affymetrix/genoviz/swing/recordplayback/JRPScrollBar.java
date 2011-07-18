package com.affymetrix.genoviz.swing.recordplayback;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JScrollBar;

public class JRPScrollBar extends JScrollBar implements JRPWidget {
	private static final long serialVersionUID = 1L;
	private final String id;

	public JRPScrollBar(String id) {
		super();
		this.id = id;
		init();
	}
	public JRPScrollBar(String id, int orientation) {
		super(orientation);
		this.id = id;
		init();
	}
	public JRPScrollBar(String id, int orientation, int value, int extent, int min, int max) {
		super(orientation, value, extent, min, max);
		this.id = id;
		init();
	}
    private void init() {
		RecordPlaybackHolder.getInstance().addWidget(this);
		addAdjustmentListener(new AdjustmentListener() {
			
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				RecordPlaybackHolder.getInstance().recordOperation(new Operation(id, "setValue(" + getValue() + ")"));
			}
		});
    }
    @Override
	public String getId() {
		return id;
	}
}
