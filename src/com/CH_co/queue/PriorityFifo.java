/*
 * Copyright 2001-2012 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_co.queue;

import com.CH_co.trace.ThreadTraced;
import java.util.LinkedList;
import java.util.Iterator;

import com.CH_co.trace.Trace;

/** 
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.13 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class PriorityFifo extends Object implements PriorityFifoWriterI, PriorityFifoReaderI {

//  public static final int PRIORITY_HIGHEST = 0;
//  public static final int PRIORITY_LOWEST = 10000;
  public static final long PRIORITY_HIGHEST = 0;
  public static final long PRIORITY_LOWEST = Long.MAX_VALUE;

  private LinkedList list;
  /** Closed flag */
  private boolean closed;

  private ProcessingFunctionI processingFunction = null;
  private String processingFunctionRunnerName = null;
  private boolean isProcessingFunctionRunning = false;

  /** Creates new PriorityFifo */
  public PriorityFifo() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PriorityFifo.class, "PriorityFifo()");
    this.list = new LinkedList();
    this.closed = false;
    if (trace != null) trace.exit(PriorityFifo.class);
  }

  /**
   * Lower priority number, closer in the queue.
   */
  public synchronized void add(Object obj, long priority) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PriorityFifo.class, "add(Object obj, long priority)");
    if (trace != null) trace.args(obj);
    if (trace != null) trace.args(priority);

    if (closed)
      throw new IllegalStateException("Fifo is closed, no new items can be added.");

    if (priority < PRIORITY_HIGHEST || priority > PRIORITY_LOWEST)
      throw new IllegalArgumentException("Priority is out of supported range.");

    // find last object with lower or equal priority
    int index = 0;
    Iterator iter = list.iterator();
    while (iter.hasNext()) {
      ObjectPair pair = (ObjectPair) iter.next();
      if (pair.priority > priority)
        break;
      index ++;
    }

    if (trace != null) trace.data(10, "insertion index", index);
    list.add(index, new ObjectPair(obj, priority));

    notify();

    // If sink installed, run it to consume the item.
    if (processingFunction != null) {
      consume();
    }

    if (trace != null) trace.exit(PriorityFifo.class);
  }

  public synchronized void add(Object obj) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PriorityFifo.class, "add(Object obj)");
    add(obj, PRIORITY_HIGHEST);
    if (trace != null) trace.exit(PriorityFifo.class);
  }

  private synchronized void consume() {
    if (!isProcessingFunctionRunning) {
      isProcessingFunctionRunning = true;
      try {
        Thread th = new ThreadTraced("Priority Fifo Consumer - " + processingFunctionRunnerName) {
          public void runTraced() {
            while (true) {
              Object obj = null;
              synchronized (PriorityFifo.this) {
                if (PriorityFifo.this.size() == 0) {
                  isProcessingFunctionRunning = false;
                  break;
                } else {
                  obj = PriorityFifo.this.remove();
                }
              }
              try { processingFunction.processQueuedObject(obj); } catch (Throwable t) { }
            }
          }
        };
        th.setDaemon(true);
        th.start();
      } catch (Throwable t) {
        isProcessingFunctionRunning = false;
      }
    }
  }

  /** @return the next object in the fifo within specified priority bounds. */
  public synchronized Object remove(long higherInclusiveBound, long lowerInclusiveBound) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PriorityFifo.class, "remove(int higherInclusiveBound, int lowerInclusiveBound)");
    if (trace != null) trace.args(higherInclusiveBound);
    if (trace != null) trace.args(lowerInclusiveBound);

    long lowValue = higherInclusiveBound < lowerInclusiveBound ? higherInclusiveBound : lowerInclusiveBound;
    long highValue = higherInclusiveBound < lowerInclusiveBound ? lowerInclusiveBound : higherInclusiveBound;

    // look for the first object included in the specified priorities
    boolean found = false;
    Iterator iter = list.iterator();
    ObjectPair pair = null;
    while (iter.hasNext()) {
      pair = (ObjectPair) iter.next();
      if (pair.priority >= lowValue && pair.priority <= highValue) {
        // inclusion OK
        found = true;
        break;
      }
    }

    Object obj = null;
    if (found) {
      obj = pair.obj;
      iter.remove();
    }

    if (trace != null) trace.exit(PriorityFifo.class, obj);
    return obj;
  }

  /** @return the next object in the fifo. */
  public synchronized Object remove() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PriorityFifo.class, "remove()");
    Object obj = remove(PRIORITY_HIGHEST, PRIORITY_LOWEST);
    if (trace != null) trace.exit(PriorityFifo.class);
    return obj;
  }

  /** @return the next to remove object without removing it. */
  public synchronized Object peek() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PriorityFifo.class, "peek()");
    ObjectPair pair = (ObjectPair) list.getFirst();
    Object obj = pair.obj;
    if (trace != null) trace.exit(PriorityFifo.class, obj);
    return obj;
  }

  /** @return the next object's priority without removing it. */
  public synchronized long peekPriority() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PriorityFifo.class, "peekPriority()");
    ObjectPair pair = (ObjectPair) list.getFirst();
    long priority = pair.priority;
    if (trace != null) trace.exit(PriorityFifo.class, priority);
    return priority;
  }

  /** Get number of objects in the fifo */
  public synchronized int size() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PriorityFifo.class, "size()");
    int size = list.size();
    if (trace != null) trace.exit(PriorityFifo.class, size);
    return size;
  }

  /** Clears the contents of the FIFO.  All references in the FIFO are discarded. */
  public synchronized void clear() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(PriorityFifo.class, "clear()");
    list.clear();
    if (trace != null) trace.exit(PriorityFifo.class);
  }

  /** Closes the Fifo, no new items will be accepted */
  public synchronized void close() {
    closed = true;
  }

  public synchronized PriorityFifo installSink(String runnerName, ProcessingFunctionI function) {
    if (processingFunction != null)
      throw new IllegalStateException("Sink already installed!");
    processingFunction = function;
    processingFunctionRunnerName = runnerName;
    return this;
  }

  public synchronized Iterator iterator() {
    return new PriorityFifoIterator(this);
  }


  private static class ObjectPair {
    Object obj;
    long priority;

    ObjectPair(Object obj, long priority) {
      this.obj = obj;
      this.priority = priority;
    }
  }

  public static class PriorityFifoIterator extends Object implements Iterator {
    Iterator iter;
    ObjectPair lastObj;

    private PriorityFifoIterator(PriorityFifo pFifo) {
      iter = pFifo.list.iterator();
    }

    public boolean hasNext() {
      return iter.hasNext();
    }

    public Object next() {
      lastObj = (ObjectPair) iter.next();
      return lastObj.obj;
    }

    public long priority() {
      return lastObj.priority;
    }

    public void remove() {
      iter.remove();
    }
  }

}