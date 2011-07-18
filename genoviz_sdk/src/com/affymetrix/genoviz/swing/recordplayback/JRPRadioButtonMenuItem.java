package com.affymetrix.genoviz.swing.recordplayback;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JRadioButtonMenuItem;

public class JRPRadioButtonMenuItem extends JRadioButtonMenuItem implements JRPWidget {
	private static final long serialVersionUID = 1L;
	private final String id;

	public JRPRadioButtonMenuItem(String id) {
		super();
		this.id = id;
		init();
	}
	public JRPRadioButtonMenuItem(String id, Action a) {
		super(a);
		this.id = id;
		init();
	}
	public JRPRadioButtonMenuItem(String id, Icon icon) {
		super(icon);
		this.id = id;
		init();
	}
	public JRPRadioButtonMenuItem(String id, Icon icon, boolean selected) {
		super(icon, selected);
		this.id = id;
		init();
	}
	public JRPRadioButtonMenuItem(String id, String text) {
		super(text);
		this.id = id;
		init();
	}
	public JRPRadioButtonMenuItem(String id, String text, boolean selected) {
		super(text, selected);
		this.id = id;
		init();
	}
	public JRPRadioButtonMenuItem(String id, String text, Icon icon) {
		super(text, icon);
		this.id = id;
		init();
	}
	public JRPRadioButtonMenuItem(String id, String text, Icon icon, boolean selected) {
		super(text, icon, selected);
		this.id = id;
		init();
	}

	private void init() {
		RecordPlaybackHolder.getInstance().addWidget(this);
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RecordPlaybackHolder.getInstance().recordOperation(new Operation(id, "doClick()"));
			}
		});
    }

	@Override
	public String getId() {
		return id;
	}
}
