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

package comx.HTTP_Common;

import java.util.LinkedList;
import java.util.Iterator;

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
 * <b>$Revision: 1.4 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class SequenceFifo extends Object {

  private static final long SEQUENCE_LOWEST = 0;
  private static final long SEQUENCE_HIGHEST = Long.MAX_VALUE;

  private LinkedList list;
  private long lastRemovedSequence = -1;

  /** Creates new SequenceFifo */
  public SequenceFifo() {
    this.list = new LinkedList();
  }

  /**
   * Lower sequence number, closer in the queue.
   * @return true if object was added, false if sequence number too low or already existed
   */
  public synchronized boolean add(Object obj, long sequence) {
    if (sequence < SEQUENCE_LOWEST || sequence > SEQUENCE_HIGHEST)
      throw new IllegalArgumentException("Sequence is out of supported range.");

    boolean rc = false;

    if (lastRemovedSequence >= sequence) {
      // no-op
    } else {
      int size = list.size();
      if (size == 0) {
        list.add(0, new ObjectPair(obj, sequence));
        rc = true;
      } else {
        boolean processed = false;
        // find last object with lower sequence and insert it after it
        int index = 0;
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
          ObjectPair pair = (ObjectPair) iter.next();
          if (pair.sequence == sequence) {
            // no-op
            processed = true;
            break;
          } else if (pair.sequence > sequence) {
            break;
          }
          index ++;
        }
        if (!processed) {
          list.add(index, new ObjectPair(obj, sequence));
          rc = true;
        }
      }
    }

    notifyAll();

    return rc;
  }

  /**
   * @return the next object in the fifo having the next "remove sequence" number, null if no Object in sequence exists.
   */
  public synchronized Object remove() {
    Object obj = null;

    if (list.size() > 0) {
      ObjectPair pair = (ObjectPair) list.getFirst();
      if (pair.sequence == lastRemovedSequence + 1) {
        list.remove(0);
        obj = pair.obj;
        lastRemovedSequence ++;
      }
      notifyAll();
    }

    return obj;
  }

  /**
   * @return true if next call to remove will return available sequenced object.
   */
  public synchronized boolean isAvailable(int numAvailable) {
    boolean available = false;
    if (list.size() > 0) {
      boolean anyFailed = false;
      for (int i=0; i<numAvailable; i++) {
        ObjectPair pair = null;
        if (list.size() > i)
          pair = (ObjectPair) list.get(i);
        if (pair != null && pair.sequence == lastRemovedSequence + 1 + i) {
        } else {
          anyFailed = true;
          break;
        }
      }
      available = !anyFailed;
    }
    return available;
  }

  public synchronized boolean hasBacklog() {
    return size() > 0 && !isAvailable(1);
  }

  public synchronized long getLastRemovedSequence() {
    return lastRemovedSequence;
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

  /** Get number of objects in the fifo */
  public synchronized int size() {
    int size = list.size();
    return size;
  }

  /** Clears the contents of the FIFO.  All references in the FIFO are discarded. */
  public synchronized void clear() {
    list.clear();
    notifyAll();
    lastRemovedSequence = SEQUENCE_HIGHEST;
  }

  public synchronized Iterator iterator() {
    return new SequenceFifoIterator(this);
  }


  private static class ObjectPair {
    Object obj;
    long sequence;

    ObjectPair(Object obj, long sequence) {
      this.obj = obj;
      this.sequence = sequence;
    }
  }

  private static class SequenceFifoIterator extends Object implements Iterator {
    Iterator iter;

    private SequenceFifoIterator(SequenceFifo sFifo) {
      iter = sFifo.list.iterator();
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