/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package comx.HTTP_Common;

import java.util.LinkedList;
import java.util.Iterator;

/** 
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p> 
 *
 * Class Description: 
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.3 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class OrderedFifo extends Object {

  private static final long ORDER_LOWEST = 0;
  private static final long ORDER_HIGHEST = Long.MAX_VALUE;

  private LinkedList list;

  /** Creates new OrderedFifo */
  public OrderedFifo() {
    this.list = new LinkedList();
  }

  /**
   * Lower orderId number, closer in the queue.
   * @return true if object was added, false if orderId number already existed
   */
  public synchronized boolean add(Object obj, long orderId) {

    if (orderId < ORDER_LOWEST || orderId > ORDER_HIGHEST)
      throw new IllegalArgumentException("OrderId is out of supported range.");

    boolean rc = false;

    int size = list.size();
    if (size == 0) {
      list.add(0, new ObjectPair(obj, orderId));
      rc = true;
    } else {
      boolean processed = false;
      // find last object with lower orderId and insert it after it
      int index = 0;
      Iterator iter = list.iterator();
      while (iter.hasNext()) {
        ObjectPair pair = (ObjectPair) iter.next();
        if (pair.orderId == orderId) {
          // no-op
          processed = true;
          break;
        } else if (pair.orderId > orderId) {
          break;
        }
        index ++;
      }
      if (!processed) {
        list.add(index, new ObjectPair(obj, orderId));
        rc = true;
      }
    }

    notifyAll();

    return rc;
  }

  /** 
   * @return the next object in the fifo having the next orderId number, null if no Object exists.
   */
  public synchronized Object remove() {
    Object obj = null;

    if (list.size() > 0) {
      ObjectPair pair = (ObjectPair) list.remove(0);
      obj = pair.obj;
      notifyAll();
    }

    return obj;
  }

  /** peek the next object without removing it. */
  public synchronized Object peek() {
    Object obj = null;
    if (list.size() > 0) {
      ObjectPair pair = (ObjectPair) list.getFirst();
      obj = pair.obj;
    }
    return obj;
  }

  public synchronized Object peekLast() {
    ObjectPair pair = (ObjectPair) list.getLast();
    Object obj = pair.obj;
    return obj;
  }

  /** Get number of objects in the fifo */
  public synchronized int size() {
    int size = list.size();
    return size;
  }

  /** Clears the contents of the FIFO.  All references in the FIFO are discarded. */
  public synchronized void clear() {
    list.clear();
    notifyAll();
  }

  public synchronized Iterator iterator() {
    return new OrderedFifoIterator(this);
  }


  private static class ObjectPair {
    Object obj;
    long orderId;

    ObjectPair(Object obj, long orderId) {
      this.obj = obj;
      this.orderId = orderId;
    }
  }

  private static class OrderedFifoIterator extends Object implements Iterator {
    Iterator iter;

    private OrderedFifoIterator(OrderedFifo oFifo) {
      iter = oFifo.list.iterator();
    }

    public boolean hasNext() {
      return iter.hasNext();
    }

    public Object next() {
      return ((ObjectPair) iter.next()).obj;
    }

    public void remove() {
      iter.remove();
    }
  }

}