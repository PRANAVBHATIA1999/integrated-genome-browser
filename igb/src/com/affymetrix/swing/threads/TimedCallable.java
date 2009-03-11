/*
 * TimedCallable.java
 * 
 * Originally written by Joseph Bowbeer and released into the public domain.
 * This may be used for any purposes whatsoever without acknowledgment.
 */

package com.affymetrix.swing.threads;

/**
 * TimedCallable runs a Callable function for a given length of time.
 * The function is run in its own thread. If the function completes
 * in time, its result is returned; otherwise the thread is interrupted
 * and an InterruptedException is thrown.
 * <p>
 * Note: TimedCallable will always return within the given time limit
 * (modulo timer inaccuracies), but whether or not the worker thread
 * stops in a timely fashion depends on the interrupt handling in the
 * Callable function's implementation. 
 *
 * @author  Joseph Bowbeer
 * @version 1.0
 */
public final class TimedCallable extends Object implements Callable {

    private final Callable function;
    private final long millis;

    public TimedCallable(Callable function, long millis) {
        this.function = function;
        this.millis = millis;
    }

    public Object call() throws Exception {

        FutureResult result = new FutureResult();

        Thread thread = new Thread(result.setter(function));
        thread.start();

        try {
            return result.timedGet(millis);
        }
        catch (InterruptedException ex) {
            /* Stop thread if we were interrupted or timed-out
               while waiting for the result. */
            thread.interrupt();
            throw ex;
        }
    }
}
