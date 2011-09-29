package com.affymetrix.genoviz.swing.recordplayback;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JToggleButton;

public class JRPToggleButton extends JToggleButton implements JRPWidget {
	private static final long serialVersionUID = 1L;
	private final String id;

	public JRPToggleButton(String id) {
		super();
		this.id = id;
		init();
	}
	public JRPToggleButton(String id, Icon icon) {
		super(icon);
		this.id = id;
		init();
    }
	public JRPToggleButton(String id, Icon icon, boolean selected) {
		super(icon, selected);
		this.id = id;
		init();
    }
	public JRPToggleButton(String id, String text) {
		super(text);
		this.id = id;
		init();
    }
	public JRPToggleButton(String id, String text, boolean selected) {
		super(text, selected);
		this.id = id;
		init();
    }
	public JRPToggleButton(String id, String text, Icon icon) {
		super(text, icon);
		this.id = id;
		init();
    }
	public JRPToggleButton(String id, String text, Icon icon, boolean selected) {
		super(text, icon, selected);
		this.id = id;
		init();
    }
	public JRPToggleButton(String id, Action a) {
		super(a);
		this.id = id;
		init();
	}
    private void init() {
		RecordPlaybackHolder.getInstance().addWidget(this);
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RecordPlaybackHolder.getInstance().recordOperation(new Operation(JRPToggleButton.this, "doClick()"));
			}
		});
    }
    @Override
	public String getId() {
		return id;
	}

    @Override
	public boolean consecutiveOK() {
		return true;
	}
}
