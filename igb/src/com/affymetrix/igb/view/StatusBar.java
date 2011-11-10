package com.affymetrix.igb.view;

import java.awt.Dimension;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;

import com.jidesoft.status.MemoryStatusBarItem;
import com.affymetrix.common.CommonUtils;
import com.affymetrix.genoviz.swing.recordplayback.JRPButton;
import com.affymetrix.igb.util.ThreadHandler;

public final class StatusBar extends JPanel{
	private static final long serialVersionUID = 1l;
	
	private static final ImageIcon closeIcon = CommonUtils.getInstance().getIcon("images/stop.png");
	
	private final JLabel status_ta;
	private final MemoryStatusBarItem memory_item;
	private final JRPButton mainCancel;
	public final JProgressBar progressBar;
		
	public StatusBar() {
		String tt_status = "Shows Selected Item, or other Message";
	
		status_ta = new JLabel("");
		progressBar = new JProgressBar();
		memory_item = new MemoryStatusBarItem();
		memory_item.setShowMaxMemory(true);
		mainCancel = new JRPButton("StatusBar_mainCancel", closeIcon);
		mainCancel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 0));
		ThreadHandler.getThreadHandler().addPopupHandler(mainCancel);

		status_ta.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		progressBar.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
	
		status_ta.setToolTipText(tt_status);
		progressBar.setMaximumSize(new Dimension(150, 5));
		progressBar.setIndeterminate(true);
		progressBar.setVisible(false);
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);

		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
		layout.setHonorsVisibility(false);

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(mainCancel)
				.addComponent(status_ta)
				.addGap(1, 1, Short.MAX_VALUE)
				.addComponent(progressBar)
				.addComponent(memory_item, 1, 200, 200));

		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER)
				.addComponent(mainCancel)
				.addComponent(status_ta)
				.addGap(1, 1, Short.MAX_VALUE)
				.addComponent(progressBar)
				.addComponent(memory_item));
		
	}

	/** Sets the String in the status bar.
	 *  HTML can be used if prefixed with "<html>".
	 *  Can be safely called from any thread.
	 *  @param s  a String, null is ok; null will erase the status String.
	 */
	public final void setStatus(String s) {
		if (s == null) {
			s = "";
		}

		status_ta.setText(s);
	}

	public String getStatus() {
		return status_ta.getText();
	}
}
