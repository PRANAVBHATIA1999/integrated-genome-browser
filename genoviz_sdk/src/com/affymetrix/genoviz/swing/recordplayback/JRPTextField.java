package com.affymetrix.genoviz.swing.recordplayback;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;
import javax.swing.text.Document;

public class JRPTextField extends JTextField implements JRPWidget {
	private static final long serialVersionUID = 1L;
	private final String id;

	public JRPTextField(String id) {
		super();
		this.id = id;
		init();
	}
	public JRPTextField(String id, Document doc, String text, int columns) {
		super(doc, text, columns);
		this.id = id;
		init();
	}
	public JRPTextField(String id, int columns) {
		super(columns);
		this.id = id;
		init();
	}
	public JRPTextField(String id, String text) {
		super(text);
		this.id = id;
		init();
	}
	public JRPTextField(String id, String text, int columns) {
		super(text, columns);
		this.id = id;
		init();
	}
	private void init() {
		RecordPlaybackHolder.getInstance().addWidget(this);
		addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}
			@Override
			public void keyReleased(KeyEvent e) {
				RecordPlaybackHolder.getInstance().recordOperation(new Operation(JRPTextField.this));
			}
			@Override
			public void keyPressed(KeyEvent e) {}
		});
    }
	@Override
	public String getID() {
		return id;
	}

	@Override
	public void execute(String... params) {
		setText(params[0]);
	}

	@Override
	public String[] getParms() {
		return new String[]{id, "" + getText()};
	}
}
