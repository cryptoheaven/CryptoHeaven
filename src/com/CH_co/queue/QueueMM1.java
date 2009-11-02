/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_co.queue;

import com.CH_co.trace.Trace;

/** 
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class QueueMM1 extends Object {

  private FifoWriterI fifo = null;
  private final Object processingMonitor = new Object();

  private PrivateQueueServer server;
  private ProcessingFunctionI processingFunction;
  private String name;

  private boolean finishing;
  private boolean killing;

  /** Creates new QueueMM1 */
  public QueueMM1(String name) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(QueueMM1.class, "(String name)");
    this.name = name;
    fifo = new Fifo();
    if (trace != null) trace.args(name);
    if (trace != null) trace.exit(QueueMM1.class, this);
  }

  public QueueMM1(String name, ProcessingFunctionI processingFunction) {
    this(name, new Fifo(), processingFunction);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(QueueMM1.class, "(String name, ProcessingFunctionI processingFunction)");
    if (trace != null) trace.args(name);
    if (trace != null) trace.args(processingFunction);
    if (trace != null) trace.exit(QueueMM1.class, this);
  }

  public QueueMM1(String name, FifoWriterI customFifo, ProcessingFunctionI processingFunction) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(QueueMM1.class, "(String name, FifoWriterI customFifo, ProcessingFunctionI processingFunction)");
    if (trace != null) trace.args(name);
    if (trace != null) trace.args(processingFunction);
    this.name = name;
    fifo = customFifo;
    setProcessingFunction(processingFunction);
    if (trace != null) trace.exit(QueueMM1.class, this);
  }


  /** Sets the class for processing objects by server thread and
      starts the server thread if it wan't started yet.
      If the Server thread is already running, switches the object processor.
      None of the objects in the queue are lost.  */
  public void setProcessingFunction(ProcessingFunctionI function) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(QueueMM1.class, "setProcessingFunction(ProcessingFunctionI)");
    synchronized (processingMonitor) {
      processingFunction = function;
      if (trace != null) trace.data(10, function);

      if (server == null && !finishing && !killing) {
        if (trace != null) trace.data(20, "About to create and start a queue server");
        String threadName = name;
        if (threadName == null)
          threadName = "PrivateQueueServer";
        server = new PrivateQueueServer(threadName);
        server.setDaemon(true);
        server.start();
        if (trace != null) trace.data(22, "Queue server started");
      }
    }
    if (trace != null) trace.exit(QueueMM1.class);
  }

  /** We only externalize the Fifo writer end on one can write to it.  Output is private
      and should not be externalized due to potential invalid state exception,
      as the run() assumes that once fifo size is > 0 the item(s) are there
      without further synchronization to make sure they are not removed by external
      actions before runner gets to act on them.
  */
  public FifoWriterI getFifoWriterI() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(QueueMM1.class, "getFifoWriterI()");
    if (trace != null) trace.exit(QueueMM1.class, fifo);
    return fifo;
  }

  /** Finish up processing of the queue and clear the queue.  
      All items outstanding in the queue will be processed
      by the processor.  No new items can be accepted.
      Invalidates Queue.  Finished Queue cannot be restarted. */
  public void finish() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(QueueMM1.class, "finish()");
    finishing = true;
    if (server != null) {
      server.interrupt();
      server = null;
    }
    // wake up all waiting
    wakeUpProcessingThread();
    if (trace != null) trace.exit(QueueMM1.class);
  }

  /** Kill processing of the queue and clear the queue.  
      All items outstanding in the queue will not be processed.
      Invalidates Queue.  Killed Queue cannot be restarted.  */
  public void kill() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(QueueMM1.class, "kill()");
    killing = true;
    if (server != null) {
      server.interrupt();
      server = null;
    }
    // wake up all waiting
    wakeUpProcessingThread();
    processingFunction = null;
    fifo.clear();
    fifo = null;
    if (trace != null) trace.exit(QueueMM1.class);
  }

  public int size() {
    return fifo.size();
  }

  private void wakeUpProcessingThread() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(QueueMM1.class, "wakeUpProcessingThread()");
    FifoWriterI f = fifo;
    Object o = processingMonitor;
    if (f != null) {
      synchronized (f) {
        f.notifyAll();
      }
    }
    if (o != null) {
      synchronized (o) {
        o.notifyAll();
      }
    }
    if (trace != null) trace.exit(QueueMM1.class);
  }

  /**
   * Private Thread that serves/runs the elements coming out of the queue.
   */
  private class PrivateQueueServer extends Thread {

    private PrivateQueueServer(String name) {
      super(name);
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PrivateQueueServer.class, "()");
      if (trace != null) trace.data(10, name);
      if (trace != null) trace.exit(PrivateQueueServer.class);
    }

    /** For private use only! 
        The running queue-server.
    */
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PrivateQueueServer.class, "run()");
      while (true) {
        // Curtesy to other threads -- since we are in an infinite loop here.
        yield();

        // Let go of the monitor and wait for queue additions.
        // Addition to the queue will wake up this process.
        if (!(finishing || killing)) {
          synchronized (fifo) {
            if (fifo.size() == 0) {
              if (trace != null) trace.data(10, "About to wait for new objects in fifo.");
              try {
                fifo.wait();
              } catch (InterruptedException e) {
              }
              if (trace != null) trace.data(20, "Woke up from waiting for new objects in fifo.");
            } // if size = 0
          } // end synchronized
        }

        if (killing) break;

        // *** BEGIN Feed Objects to the processor 
        synchronized (processingMonitor) {
          if (fifo.size()>0) {
            //Object nextObj = ((FifoReaderI) fifo).peek();
            Object nextObj = ((FifoReaderI) fifo).remove();

            // Process message in a try-catch block so that queue won't be destroyed when external execution throws up.
            try {
              if (processingFunction != null)
                processingFunction.processQueuedObject(nextObj);
            } catch (Throwable t) {
              if (trace != null) trace.exception(QueueMM1.class, 40, t);
            }

            //((FifoReaderI) fifo).remove();
          }
        }
        // *** END Feed Objects to the processor 

        if (finishing && fifo.size() == 0) {
          if (trace != null) trace.data(50, "Breaking out of the while(true) loop.");
          break;
        }
      } // end while 
      processingFunction = null;

      if (trace != null) trace.data(90, Thread.currentThread().getName() + " done.");
      if (trace != null) trace.exit(PrivateQueueServer.class);
      if (trace != null) trace.clear();
    }
  }

} // end class QueueMM1