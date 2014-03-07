/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.trace;

import com.CH_co.util.MyUncaughtExceptionHandlerOps;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
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
      MyUncaughtExceptionHandlerOps.unhandledException(t);
    }
    if (trace != null) trace.data(300, Thread.currentThread().getName() + " done.");
    if (trace != null) trace.exit(getClass());
    if (trace != null) trace.clear();
  }

}