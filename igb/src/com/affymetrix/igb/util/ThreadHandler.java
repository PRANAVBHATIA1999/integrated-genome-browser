
package com.affymetrix.igb.util;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JFrame;

import com.affymetrix.igb.Application;
import com.affymetrix.common.CommonUtils;
import com.affymetrix.genometryImpl.thread.CThreadEvent;
import com.affymetrix.genometryImpl.thread.CThreadListener;
import com.affymetrix.genometryImpl.thread.CThreadWorker;
import com.affymetrix.genometryImpl.util.ThreadUtils;
import com.affymetrix.genoviz.swing.recordplayback.JRPButton;


/**
 *
 * @author hiralv
 */
public class ThreadHandler implements ActionListener, CThreadListener{
	private static final ImageIcon closeIcon = CommonUtils.getInstance().getIcon("images/stop.png");
	
	private static ThreadHandler singleton;
	private final Set<CThreadListener> listeners;
	private final Set<CThreadWorker> workers;
	private final JPopupMenu runningTasks;
	private final Set<AbstractButton> popupHandler;
	
	public static ThreadHandler getThreadHandler(){
		if(singleton == null){
			singleton = new ThreadHandler();
		}
		return singleton;
	}
	
	private ThreadHandler(){
		runningTasks = new JPopupMenu();
		runningTasks.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		listeners= new CopyOnWriteArraySet<CThreadListener>();
		workers = new LinkedHashSet<CThreadWorker>();
		popupHandler = new LinkedHashSet<AbstractButton>(1);
	}
	
	public void execute(Object obj, CThreadWorker worker){
		if(obj == null || worker == null){
			throw new IllegalArgumentException("None of parameters can be null");
		}
		worker.addThreadListener(this);
		ThreadUtils.getPrimaryExecutor(obj).execute(worker);
	}
	
	public void actionPerformed(ActionEvent ae) {
		int size = setCancelPopup();
		if(size == 0 )
			return;
		
		JFrame frame = Application.getSingleton().getFrame();
		final int x = (int) frame.getAlignmentX();
		final int y = (int) frame.getAlignmentY() + frame.getHeight()  - ((size+1) * 29);
		runningTasks.show(frame,x,y);
	}
	
	public int setCancelPopup() {
		int size = workers.size();
		if(size == 0){
			return 0;
		}
	
		final JPanel outerBox = new JPanel();
		outerBox.setLayout(new GridLayout(size,1));
		outerBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		for (final CThreadWorker worker : workers) {
			final Box box = Box.createHorizontalBox();		
			//String string = worker.getMessage().substring(0, Math.min(40, worker.getMessage().length()));
			final JLabel taskName = new JLabel(worker.getMessage());

			final JRPButton cancelTask = new JRPButton("ThreadHandler_cancelTask", closeIcon);
			cancelTask.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 0));
			cancelTask.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent ae) {
					box.setVisible(false);
					if(worker != null && !worker.isCancelled() && !worker.isDone()){
						worker.cancel(true);
					}
				}
			});

			box.setBorder(BorderFactory.createEtchedBorder());
			box.add(cancelTask);
			box.add(Box.createRigidArea(new Dimension(5,25)));
			box.add(taskName);
			box.add(Box.createRigidArea(new Dimension(5,25)));
			outerBox.add(box);

		}
		runningTasks.removeAll();
		runningTasks.add(outerBox);
		return size;
	}

	public void heardThreadEvent(CThreadEvent cte) {
		boolean enabled = workers.size() > 0;
		CThreadWorker w = (CThreadWorker) cte.getSource();
		if (cte.getState() == CThreadEvent.STARTED) {
			workers.add(w);
			Application.getSingleton().addNotLockedUpMsg(w.getMessage());
		} else {
			workers.remove(w);
			Application.getSingleton().removeNotLockedUpMsg(w.getMessage());
		}
		
		boolean nowEnabled = workers.size() > 0;
		if(enabled != nowEnabled){
			for(AbstractButton button : popupHandler){
				button.setEnabled(nowEnabled);
			}
		}
	}

	public void addPopupHandler(AbstractButton button) {
		button.addActionListener(this);
		button.setEnabled(false);
		popupHandler.add(button);
	}
}
