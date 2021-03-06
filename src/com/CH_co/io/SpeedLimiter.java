/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.io;

import java.util.*;

import com.CH_co.monitor.Stats;
import com.CH_co.trace.*;
import com.CH_co.util.*;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.14 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class SpeedLimiter extends Object {

  private static final boolean PROPERTY_SAVE_ENABLED = false;

  private static final int DEFAULT_THROUGHPUT = 0; // kbps, 0 for unlimited with rate tracking, -1 for unlimited without rate tracking
  public static final long KEEP_HISTORY_MILLIS = 3000; // 5 seconds of history
  public static final long MIN_GRANUALITY_MILLIS = 150; // make new entries into history no more often than GRANUALITY specified

  public static long globalInRate = 0;
  public static long globalOutRate = 0;
  public static long globalCombinedRate = DEFAULT_THROUGHPUT;
  public static long connInRate = 0;
  public static long connOutRate = 0;

  private static long totalByteCountRead;
  private static long totalByteCountWritten;

  private static final LinkedList timeCountL = new LinkedList();
  private static final LinkedList timeCountInL = new LinkedList();
  private static final LinkedList timeCountOutL = new LinkedList();
  
  private static long lastUpdateRate;
  private static long lastUpdateRateIn;
  private static long lastUpdateRateOut;
  
  private static long lastUpdateStamp;
  private static long lastUpdateStampIn;
  private static long lastUpdateStampOut;

  public static final String PROPERTY_NAME_GLOBAL_IN_RATE = "SpeedLimiter_globalInRate";
  public static final String PROPERTY_NAME_GLOBAL_OUT_RATE = "SpeedLimiter_globalOutRate";
  public static final String PROPERTY_NAME_GLOBAL_COMBINED_RATE = "SpeedLimiter_globalCombinedRate";
  public static final String PROPERTY_NAME_CONN_IN_RATE = "SpeedLimiter_connInRate";
  public static final String PROPERTY_NAME_CONN_OUT_RATE = "SpeedLimiter_connOutRate";

  static {
    // initiate global transfer rate limits
    if (PROPERTY_SAVE_ENABLED)
      readSpeedProperties();

    // Make a thread to reset the global stats from time to time
    // so that visual throughput display gets reset from time to time even if there are no requests
    // going through.
    Thread globalStatReseter = new ThreadTraced("Global Stat Reseter") {
      public void runTraced() {
        Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "SpeedLimiter.static.globalStatReseter.runTraced()");
        boolean forever = true;
        while (forever) {
          try {
            Thread.sleep(1000);
            if (trace != null) trace.data(100, "update global transfer rate label... -- looping every 1000ms");
            moreBytesGlobal(0); // this will update global transfer rate label
          } catch (InterruptedException e) {
          }
          if (Thread.interrupted())
            break;
        }
        if (trace != null) trace.exit(getClass());
      }
    };
    globalStatReseter.setDaemon(true);
    globalStatReseter.start();
  }

  public static long getTotalByteCountRead() {
    return totalByteCountRead;
  }
  public static long getTotalByteCountWritten() {
    return totalByteCountWritten;
  }
  public static long getTotalByteCount() {
    return totalByteCountRead+totalByteCountWritten;
  }

  private static long sumMillis(LinkedList timeCountList, long currentDateMillis) {
    if (timeCountList.size() > 0)
      return Math.max(0L, currentDateMillis - ((TimeCountPair) timeCountList.getFirst()).getTime());
    else
      return 0;
  }
  private static long sumBytes(LinkedList timeCountList) {
    long sum = 0;
    Iterator iter = timeCountList.iterator();

    // skip the first value
    if (iter.hasNext()) iter.next();

    while (iter.hasNext()) {
      sum += ((TimeCountPair) iter.next()).getCount();
    }
    return sum;
  }

  /**
   * Adds byte count to the time-window lists, maintains current counts in lists, delays to enforce maxRate
   */
  protected static void moreBytesSlowDown(int additionalBytes, LinkedList timeCountList, long maxRate) {
    long elapsedMillis = 0;
    long totalBytes = 0;
    synchronized (timeCountList) {
      long currentDateMillis = System.currentTimeMillis();
      boolean added = false;
      // if last entry is less than 200 ms away, add it to the last, else create a new one.
      if (timeCountList.size() > 0) {
        TimeCountPair timeCount = (TimeCountPair) timeCountList.getLast();
        long lastUpdateMillis = timeCount.getTime();
        if (currentDateMillis - lastUpdateMillis < MIN_GRANUALITY_MILLIS) {
          // leave the old--previous timestamp and add the bytes to the last entry already in the list
          timeCount.addCount(additionalBytes);
          added = true;
        }
      }

      if (!added) {
        timeCountList.addLast(new TimeCountPair(currentDateMillis, additionalBytes));
      }

      // consume expired history
      consumeExpiredHistory(timeCountList, currentDateMillis);
      //System.out.println("additionalBytes="+additionalBytes+", list size="+timeList.size());
      elapsedMillis = sumMillis(timeCountList, currentDateMillis);
      totalBytes = sumBytes(timeCountList);
    }
    SpeedLimiter.slowDown(elapsedMillis, totalBytes, maxRate);
  }

  public static long calculateRate(LinkedList timeCountList) {
    long elapsedMillis = 0;
    long totalBytes = 0;
    synchronized (timeCountList) {
      long currentDateMillis = System.currentTimeMillis();
      consumeExpiredHistory(timeCountList, currentDateMillis);
      elapsedMillis = sumMillis(timeCountList, currentDateMillis);
      totalBytes = sumBytes(timeCountList);
    }
    long byteRate = elapsedMillis > 0 ? (long) (totalBytes / (elapsedMillis / 1000.0)) : 0;
    return byteRate;
  }

  private static void consumeExpiredHistory(LinkedList timeCountList, long currentDateMillis) {
    synchronized (timeCountList) {
      while (timeCountList.size() > 0) {
        long dateL = ((TimeCountPair) timeCountList.getFirst()).getTime();
        // If expired or invalid date, remove it
        if (dateL + KEEP_HISTORY_MILLIS < currentDateMillis || dateL > currentDateMillis) {
          timeCountList.removeFirst();
        } else {
          break;
        }
      }
    }
  }

  public static void moreBytesRead(int additionalBytes) {
    totalByteCountRead += additionalBytes;
    moreBytesSlowDown(additionalBytes, timeCountInL, globalInRate);
    updateTransferStatsIn();
    moreBytesGlobal(additionalBytes);
  }
  public static void moreBytesWritten(int additionalBytes) {
    totalByteCountWritten += additionalBytes;
    moreBytesSlowDown(additionalBytes, timeCountOutL, globalOutRate);
    updateTransferStatsOut();
    moreBytesGlobal(additionalBytes);
  }
  private static void moreBytesGlobal(int additionalBytes) {
    moreBytesSlowDown(additionalBytes, timeCountL, globalCombinedRate);
    updateTransferRateStats();
  }

  /**
   * Updates user displayable total throughput label.
   */
  private static void updateTransferRateStats() {
    synchronized (timeCountL) {
      long currentDateMillis = System.currentTimeMillis();
      long elapsedMillis = sumMillis(timeCountL, currentDateMillis);
      long totalBytes = sumBytes(timeCountL);
      // avoid division by 0
      long byteRate = elapsedMillis > 0 ? (long) (totalBytes / (elapsedMillis / 1000.0)) : 0;

      // Update when rate changes more than 30% or if 1 second since last update has passed.
      // Don't update if the global counter has started less than 600 ms ago
      if (elapsedMillis > 600) {
        if (byteRate > (lastUpdateRate * 1.3) || byteRate < (lastUpdateRate / 1.3) ||
            currentDateMillis - lastUpdateStamp > 1000)
        {
          Stats.setTransferRate(byteRate);
          lastUpdateRate = byteRate;
          lastUpdateStamp = currentDateMillis;
        }
      }
    }
  }
  private static void updateTransferStatsIn() {
    synchronized (timeCountInL) {
      long currentDateMillis = System.currentTimeMillis();
      long elapsedMillis = sumMillis(timeCountInL, currentDateMillis);
      long totalBytes = sumBytes(timeCountInL);
      // avoid division by 0
      long byteRate = elapsedMillis > 0 ? (long) (totalBytes / (elapsedMillis / 1000.0)) : 0;

      // Update when rate changes more than 30% or if 1 second since last update has passed.
      // Don't update if the global counter has started less than 600 ms ago
      if (elapsedMillis > 600) {
        if (byteRate > (lastUpdateRateIn * 1.3) || byteRate < (lastUpdateRateIn / 1.3) ||
            currentDateMillis - lastUpdateStampIn > 1000)
        {
          Stats.setTransferRateIn(byteRate);
          lastUpdateRateIn = byteRate;
          lastUpdateStampIn = currentDateMillis;
        }
      }
    }
  }
  private static void updateTransferStatsOut() {
    synchronized (timeCountOutL) {
      long currentDateMillis = System.currentTimeMillis();
      long elapsedMillis = sumMillis(timeCountOutL, currentDateMillis);
      long totalBytes = sumBytes(timeCountOutL);
      // avoid division by 0
      long byteRate = elapsedMillis > 0 ? (long) (totalBytes / (elapsedMillis / 1000.0)) : 0;

      // Update when rate changes more than 30% or if 1 second since last update has passed.
      // Don't update if the global counter has started less than 600 ms ago
      if (elapsedMillis > 600) {
        if (byteRate > (lastUpdateRateOut * 1.3) || byteRate < (lastUpdateRateOut / 1.3) ||
            currentDateMillis - lastUpdateStampOut > 1000)
        {
          Stats.setTransferRateOut(byteRate);
          lastUpdateRateOut = byteRate;
          lastUpdateStampOut = currentDateMillis;
        }
      }
    }
  }


  /**
   * @param elapsedMillis elapsed time from which statistics are counted.
   * @param totalBytes Total bytes transfered from the startDate
   * @param maxThroughput Maximum allowed throughput in kpbs (kilo bits per second)
   */
  public static void slowDown(long elapsedMills, long totalBytes, long maxThroughput) {
    //System.out.println("elapsedMills="+elapsedMills+", totalBytes="+totalBytes+", maxThroughput="+maxThroughput);
    if (totalBytes == 0) return;
    if (maxThroughput == 0) return;

    // in bits
    long totalBits = 8 * totalBytes;
    // in bits per millisecond
    double maxThroughput_BP_ms = (1024.0 * maxThroughput) / 1000;

    //long currentDateMillis = System.currentTimeMillis();

    //long elapsedMills = currentDateMillis - startDateMillis;

    long millsThatShouldHavePassed = (long) (totalBits / maxThroughput_BP_ms);
    long additionalWaitMills = millsThatShouldHavePassed - elapsedMills;

    if (additionalWaitMills > 0 && additionalWaitMills < 30000) { // just in case system clock was changed!
      try {
        //System.out.println("to sleep millis="+additionalWaitMills);
        Thread.sleep(additionalWaitMills);
      } catch (InterruptedException e) {
        // Interruption here is OK.
        // For example when writer of HEAVY job finished and interrupts the reader,
        // if the reader is sleaping here, it will wake up.
        /*
        Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SpeedLimitedInputStream.class, "exception handling in slowDown()");
        if (trace != null) trace.exception(SpeedLimitedInputStream.class, 100, e);
        if (trace != null) trace.exit(SpeedLimitedInputStream.class);
        throw new InterruptedSleepException(e.getMessage());
        */
      }
    }

  }

  private static void readSpeedProperties() {
    if (PROPERTY_SAVE_ENABLED) {
      try {
        String sGloInRate = GlobalProperties.getProperty(PROPERTY_NAME_GLOBAL_IN_RATE);
        String sGloOutRate = GlobalProperties.getProperty(PROPERTY_NAME_GLOBAL_OUT_RATE);
        String sGloCombRate = GlobalProperties.getProperty(PROPERTY_NAME_GLOBAL_COMBINED_RATE);
        String sConInRate = GlobalProperties.getProperty(PROPERTY_NAME_CONN_IN_RATE);
        String sConOutRate = GlobalProperties.getProperty(PROPERTY_NAME_CONN_OUT_RATE);
        if (sGloInRate != null)
          globalInRate = Integer.parseInt(sGloInRate);
        if (sGloOutRate != null)
          globalOutRate = Integer.parseInt(sGloOutRate);
        if (sGloCombRate != null)
          globalCombinedRate = Integer.parseInt(sGloCombRate);
        if (sConInRate != null)
          connInRate = Integer.parseInt(sConInRate);
        if (sConOutRate != null)
          connOutRate = Integer.parseInt(sConOutRate);
      } catch (Throwable t) {
      }
    }
  }

  public static void writeSpeedProperties() {
    if (PROPERTY_SAVE_ENABLED) {
      GlobalProperties.setProperty(PROPERTY_NAME_GLOBAL_IN_RATE, ""+globalInRate);
      GlobalProperties.setProperty(PROPERTY_NAME_GLOBAL_OUT_RATE, ""+globalOutRate);
      GlobalProperties.setProperty(PROPERTY_NAME_GLOBAL_COMBINED_RATE, ""+globalCombinedRate);
      GlobalProperties.setProperty(PROPERTY_NAME_CONN_IN_RATE, ""+connInRate);
      GlobalProperties.setProperty(PROPERTY_NAME_CONN_OUT_RATE, ""+connOutRate);
      GlobalProperties.store();
    }
  }

  private static class TimeCountPair {
    private long time;
    private long count;
    private TimeCountPair(long time, long count) {
      this.time = time;
      this.count = count;
    }
    private void addCount(long additionalCount) {
      count += additionalCount;
    }
    private long getCount() {
      return count;
    }
    private long getTime() {
      return time;
    }
  }
}