/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_cl.service.ops;

import com.CH_co.queue.PriorityFifo;

/**
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class FileLobUpSynch extends Object {

  private static final Object monitor = new Object();
  private static int synchCount = 0;

  private static PriorityFifo priorityFifo = new PriorityFifo();

  /**
   * Lower priority value entries will get dispatched ahead of higher priority values.
   * @param maxCount
   * @param priority 
   */
  public static void entry(int maxCount, long priority) {
    // limit number of entries
    synchronized (monitor) {
      if (maxCount >= 1 + synchCount + priorityFifo.size()) {
        // me plus all runners plus all waiting can fit so skip quarantine
        synchCount ++;
      } else if (maxCount >= 1 + synchCount && priorityFifo.size() > 0 && priority <= priorityFifo.peekPriority()) {
        // last chance to skip quarantine if any spots available and I'm more important priority of all waiting
        synchCount ++;
      } else {
        // go to quarantine and wait for my priority to be released
        Object obj = new Object();
        priorityFifo.add(obj, priority);
        while (synchCount >= maxCount || priorityFifo.peekPriority() < priority) {
          try {
            monitor.wait();
          } catch (InterruptedException e) { }
        }
        synchCount ++;
        priorityFifo.remove();
      }
    }
  }

  public static void exit() {
    synchronized (monitor) {
      synchCount --;
      monitor.notifyAll();
    }
  }
}