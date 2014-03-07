/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.queue;

import com.CH_co.trace.ThreadTraced;
import com.CH_co.trace.Trace;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class Fifo extends Object implements FifoReaderI, FifoWriterI {

  /** Head node of the Fifo */
  private DoubleLinkedNode head;
  /** Tail node of the Fifo */
  private DoubleLinkedNode tail;
  /** Number of linked nodes in the Fifo */
  private int count;
  /** Closed flag */
  private boolean closed;

  private ProcessingFunctionI processingFunction = null;
  private String processingFunctionRunnerName = null;
  private boolean isProcessingFunctionRunning = false;

  /** Creates new FIFO LinkedList */
  public Fifo() {
    closed = false;
    count = 0;
    head = null;
    tail = null;
  }

  /** Adds an object at the end of the queue. 
      Notifies objects waiting on 'this' through notify().
  */
  public synchronized void add(Object obj) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Fifo.class, "add(Object)");
    if (trace != null) trace.data(10, obj);
    if (closed)
      throw new IllegalStateException("Fifo is closed, no new items can be added.");

    if (count == 0) {
      head = tail = new DoubleLinkedNode(obj);
    }
    else {
      tail.linkNext(new DoubleLinkedNode(obj));
      tail = tail.getNext();
    }
    count ++;
    if (trace != null) trace.data(20, "count="+count);
    notify();

    // If sink installed, run it to consume the item.
    if (processingFunction != null) {
      consume();
    }

    if (trace != null) trace.exit(Fifo.class);
  }

  private synchronized void consume() {
    if (!isProcessingFunctionRunning) {
      isProcessingFunctionRunning = true;
      try {
        Thread th = new ThreadTraced("Fifo Consumer - " + processingFunctionRunnerName) {
          public void runTraced() {
            while (true) {
              Object obj = null;
              synchronized (Fifo.this) {
                if (Fifo.this.size() == 0) {
                  isProcessingFunctionRunning = false;
                  break;
                } else {
                  obj = Fifo.this.remove();
                }
              }
              try { processingFunction.processQueuedObject(obj); } catch (Throwable t) { }
            }
          }
        };
        th.setDaemon(true);
        th.start();
      } catch (Throwable t) {
        // if there was a problem starting a thread, mark as completed to enable restart
        isProcessingFunctionRunning = false;
      }
    }
  }

  /** 
   * Removes an object from the beginning of the queue.
   * Increments the position in the Fifo and discards the removed node.
   * @return the next object from the beginning of the Fifo, or null if no object exists in the Fifo.
   */
  public synchronized Object remove() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Fifo.class, "remove()");
    if (count == 0) {
      if (trace != null) trace.exit(Fifo.class, null);
      return null;
    }

    Object obj = head.getObject();

    if (count ==  1) {
      head.clear();
      tail.clear();
      head = tail = null;
    }
    else {
      head = head.getNext();
      head.breakPrevious().clear();
    }

    count --;
    if (trace != null) trace.data(20, "count="+count);
    if (trace != null) trace.exit(Fifo.class, obj);
    return obj;
  }

  /** @return the next to remove object without removing it. */
  public synchronized Object peek() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Fifo.class, "peek()");
    if (count == 0)
      throw new IllegalStateException("Fifo is empty, no objects could be peaked at this time.");

    Object o = head.getObject();
    if (trace != null) trace.exit(Fifo.class, o);
    return o;
  }


  /** Get number of objects in the fifo */
  public synchronized int size() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Fifo.class, "size()");
    int c = count;
    if (trace != null) trace.exit(Fifo.class, c);
    return c;
  }

  /** Closes the Fifo, no new items will be accepted */
  public synchronized void close() {
    closed = true;
  }

  public synchronized void clear() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Fifo.class, "clear()");
    while (size()>0)
      remove();
    if (trace != null) trace.exit(Fifo.class);
  }

  public synchronized Fifo installSink(String runnerName, ProcessingFunctionI function) {
    if (processingFunction != null)
      throw new IllegalStateException("Sink already installed!");
    processingFunction = function;
    processingFunctionRunnerName = runnerName;
    return this;
  }

}