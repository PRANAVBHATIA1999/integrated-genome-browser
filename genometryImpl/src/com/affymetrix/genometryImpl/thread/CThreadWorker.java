package com.affymetrix.genometryImpl.thread;

import javax.swing.SwingWorker;

/**
 *
 * @author hiralv
 */
public abstract class CThreadWorker<T,V> extends SwingWorker<T,V>{
	
	final private String message;
	
	public CThreadWorker(String msg){
		super();
		if(msg == null || msg.length() == 0){
			throw new IllegalArgumentException("Invalid Statusbar Message");
		}
		message = msg;
	}
	
	public String getMessage(){
		return message;
	}
	
	@Override
	public final void done() {
		finished();
		CThreadHolder.getInstance().notifyEndThread(this);
	}

	public void setProgressAsPercent(double percent) {
		if (percent > 1.0) {
			percent = 1.0;
		}
		if (percent < 0.0) {
			percent = 0.0;
		}
		setProgress((int)(percent * 100.0));
	}

	@Override
	protected final T doInBackground() throws Exception {
		CThreadHolder.getInstance().notifyStartThread(this);
		return runInBackground();
	}
	
	protected abstract T runInBackground();
	
	protected abstract void finished();

	protected boolean showCancelConfirmation(){
		return true;
	}
	
	public void cancelThread(boolean b){
		if(!showCancelConfirmation()){
			return;
		}
		this.cancel(b);
	}
}
