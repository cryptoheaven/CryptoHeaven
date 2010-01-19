/*
 * Copyright 2001-2010 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_co.trace;

/**
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class ThreadTraced extends Thread {

  public ThreadTraced(String threadName) {
    super(threadName);
  }

  public ThreadTraced(Runnable target, String threadName) {
    super(target, threadName);
  }

  public void runTraced() {
  }

  public void run() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "ThreadTraced.run()");
    try {
      super.run();
      runTraced();
    } catch (Throwable t) {
      if (trace != null) trace.exception(getClass(), 200, t);
    }
    if (trace != null) trace.data(300, Thread.currentThread().getName() + " done.");
    if (trace != null) trace.exit(getClass());
    if (trace != null) trace.clear();
  }

}