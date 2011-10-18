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

package com.CH_cl.service.ops;

/**
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class FileLobUpSynch extends Object {

  private static Object monitor = new Object();
  private static int synchCount = 0;

  public static void entry(int maxCount) {
    // limit number of entries
    synchronized (monitor) {
      while (synchCount >= maxCount) {
        try {
          monitor.wait();
        } catch (InterruptedException e) { }
      }
      synchCount ++;
    }
  }

  public static void exit() {
    synchronized (monitor) {
      synchCount --;
      monitor.notifyAll();
    }
  }
}