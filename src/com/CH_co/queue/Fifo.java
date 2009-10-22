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
public class Fifo extends Object implements FifoReaderI, FifoWriterI {

  /** Head node of the Fifo */
  private DoubleLinkedNode head;
  /** Tail node of the Fifo */
  private DoubleLinkedNode tail;
  /** Number of linked nodes in the Fifo */
  private int count;
  /** Closed flag */
  private boolean closed;

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
    if (trace != null) trace.exit(Fifo.class);
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

}