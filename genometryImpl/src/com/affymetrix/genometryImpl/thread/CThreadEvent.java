
package com.affymetrix.genometryImpl.thread;

import java.util.EventObject;

/**
 *
 * @author hiralv
 */
public class CThreadEvent extends EventObject{
	public static int STARTED = 0;
	public static int ENDED = 1;
	
	private final int state;
	
	public CThreadEvent(CThreadWorker worker, int state){
		super(worker);
		if(state != STARTED && state != ENDED){
			throw new IllegalArgumentException("Invalid Statusbar Message");
		}
		this.state = state;
	}
		
	public int getState(){
		return state;
	}
}
