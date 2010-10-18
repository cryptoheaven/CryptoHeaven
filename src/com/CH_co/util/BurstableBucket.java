/*
 * Copyright 2001-2010 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_co.util;

import java.util.Random;

/**
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class BurstableBucket {

  private long bucketLevel = 0;
  private long bucketSize;
  private double fillRatePerSecond;
  private long fillStamp;
  private long minSeperationTimeMS;

  public BurstableBucket(long bucketSize, double fillRatePerSecond, boolean startFull, long minSeperationTimeMS) {
    this.bucketSize = bucketSize;
    if (startFull)
      bucketLevel = bucketSize;
    this.fillRatePerSecond = fillRatePerSecond;
    fillStamp = System.currentTimeMillis();
    this.minSeperationTimeMS = minSeperationTimeMS;
  }
  
  public synchronized int passThrough() {
    return passThrough(this);
  }

  public int passThrough(Object monitor) {
    synchronized (monitor) {
      return drain(monitor);
    }
  }

  /**
   * @return 0 if there was no delay
   * @return -1 if delay was interrupted
   * @return +ve equal to delay milliseconds
   * @param waitMonitor
   */
  private int drain(Object waitMonitor) {
    int delayedMillis = 0;
    if (minSeperationTimeMS > 0) {
      delayedMillis = doWait(waitMonitor, minSeperationTimeMS, delayedMillis);
    }
    if (delayedMillis != -1) {
      catchupWithFilling();
      if (bucketLevel > 0) {
        bucketLevel --;
      } else {
        while (true) {
          long now = System.currentTimeMillis();
          long filledAgoMillis = Math.max(0, now - fillStamp); // if clock changed, filling must be always in the past!
          long fillEveryMillis = (long) (1000.0 / fillRatePerSecond);
          long sleepMoreMillis = fillEveryMillis - filledAgoMillis;
          if (sleepMoreMillis > 0) {
            if ((delayedMillis = doWait(waitMonitor, sleepMoreMillis, delayedMillis)) == -1)
              break;
          }
          // fill the bucket when we wake up
          catchupWithFilling();
          // see if we can drain and exit
          if (bucketLevel > 0) {
            bucketLevel --;
            break;
          }
        }
      }
    }
    return delayedMillis;
  }

  /**
   * Wait on the monitor and return updated delayed time in millis
   */
  private int doWait(Object waitMonitor, long millisToWait, int delayedMillis) {
    try {
      // waitMonitor was already synchronized outside this function but
      // for strict correctness we must hold a lock to wait on it
      synchronized (waitMonitor) {
        long start = System.currentTimeMillis();
        waitMonitor.wait(millisToWait);
        delayedMillis += System.currentTimeMillis() - start;
      }
    } catch (InterruptedException ex) {
      delayedMillis = -1;
    }
    return delayedMillis;
  }

  private void catchupWithFilling() {
    long now = System.currentTimeMillis();
    long timeSinceFillingMS = now - fillStamp;
    double timeSinceFillingSec = ((double) timeSinceFillingMS) / 1000.0;
    long shouldFillCount = (long) (timeSinceFillingSec * fillRatePerSecond);
    if (shouldFillCount > 0) {
      bucketLevel = Math.min(bucketSize, bucketLevel + shouldFillCount);
      fillStamp = now;
    } else if (now < fillStamp) { // if clock changed, reset fillStamp
      fillStamp = now;
    }
  }

  public static void main(String[] args) {
    BurstableBucket bb = new BurstableBucket(2, 0.333, true, 0);
    //BurstableBucket bb2 = new BurstableBucket(40, 3, true, 0);
    int count = 0;
    while (true) {
      count ++;
      bb.passThrough();
      //bb2.passThrough();
      System.out.print(""+((count%10==0) ? "-":""+(count%10)));
      if (count % 80 == 0) {
        long sleepMS = new Random().nextInt(20000)+1;
        System.out.println("~zzz~ " + sleepMS + " ms.");
        try { Thread.sleep(sleepMS); } catch (InterruptedException e) { }
      }
    }
  }
}