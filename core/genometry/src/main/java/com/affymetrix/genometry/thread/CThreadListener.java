package com.affymetrix.genometry.thread;

import java.util.EventListener;

/**
 *
 * @author hiralv
 */
public interface CThreadListener extends EventListener {

    public void heardThreadEvent(CThreadEvent cte);
}
