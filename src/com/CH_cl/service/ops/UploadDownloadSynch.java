/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.ops;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * Allows a specified number of threads enter a critical section.
 * @author  Marcin Kurzawa
 */
public class UploadDownloadSynch extends Object {

  private static final Object monitor = new Object();
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