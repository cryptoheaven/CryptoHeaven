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

package com.CH_co.io;

import java.util.*;

import com.CH_co.monitor.Stats;
import com.CH_co.trace.*;
import com.CH_co.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2010
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
 * <b>$Revision: 1.14 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class SpeedLimiter extends Object {

  private static final boolean PROPERTY_SAVE_ENABLED = false;

  private static final int DEFAULT_THROUGHPUT = 1024*8; // kbps
  public static final long KEEP_HISTORY_MILLIS = 3000; // 5 seconds of history
  public static final long MIN_GRANUALITY_MILLIS = 150; // make new entries into history no more often than GRANUALITY specified

  public static long globalInRate = 0;
  public static long globalOutRate = 0;
  public static long globalCombinedRate = DEFAULT_THROUGHPUT;
  public static long connInRate = 0;
  public static long connOutRate = 0;

  private static final LinkedList inboundStartDateMillisL = new LinkedList();
  private static final LinkedList inboundTotalBytesL = new LinkedList();

  private static final LinkedList outboundStartDateMillisL = new LinkedList();
  private static final LinkedList outboundTotalBytesL = new LinkedList();

  private static final LinkedList globalStartDateMillisL = new LinkedList();
  private static final LinkedList globalTotalBytesL = new LinkedList();

  private static long lastTransferUpdateDateMillis;
  private static long lastTransferUpdateRate;

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


  private static long sumMillis(LinkedList timeList, long currentDateMillis) {
    if (timeList.size() > 0)
      return Math.max(0L, currentDateMillis - ((Long) timeList.getFirst()).longValue());
    else
      return 0;
  }
  private static long sumBytes(LinkedList byteList) {
    long sum = 0;
    Iterator iter = byteList.iterator();

    // skip the first value
    if (iter.hasNext()) iter.next();

    while (iter.hasNext()) {
      sum += ((Long) iter.next()).longValue();
    }
    return sum;
  }

  protected static void moreBytesSlowDown(int additionalBytes, LinkedList timeList, LinkedList byteList, long maxRate) {
    long elapsedMillis = 0;
    long totalBytes = 0;
    synchronized (timeList) {
      synchronized (byteList) {
        long currentDateMillis = System.currentTimeMillis();
        boolean added = false;
        // if last entry is less than 200 ms away, add it to the last, else create a new one.

        if (timeList.size() > 0) {
          long lastUpdateMillis = ((Long) timeList.getLast()).longValue();
          if (currentDateMillis - lastUpdateMillis < MIN_GRANUALITY_MILLIS) {
            // leave the old--previous timestamp and add the bytes to the last entry already in the list
            long lastBytes = ((Long) byteList.getLast()).longValue();
            byteList.removeLast();
            byteList.addLast(new Long(lastBytes + additionalBytes));
            added = true;
          }
        }

        if (!added) {
          timeList.addLast(new Long(currentDateMillis));
          byteList.addLast(new Long(additionalBytes));
        }

        // consume expired history
        consumeExpiredHistory(timeList, byteList, currentDateMillis);
        //System.out.println("additionalBytes="+additionalBytes+", list size="+timeList.size());
        elapsedMillis = sumMillis(timeList, currentDateMillis);
        totalBytes = sumBytes(byteList);
      }
    }
    SpeedLimiter.slowDown(elapsedMillis, totalBytes, maxRate);
  }

  private static void consumeExpiredHistory(LinkedList timeList, LinkedList byteList, long currentDateMillis) {
    synchronized (timeList) {
      synchronized (byteList) {
        while (timeList.size() > 0) {
          long dateL = ((Long) timeList.getFirst()).longValue();
          // If expired or invalid date, remove it
          if (dateL + KEEP_HISTORY_MILLIS < currentDateMillis || 
              dateL > currentDateMillis) 
          {
            timeList.removeFirst();
            byteList.removeFirst();
          }
          else 
            break;
        }
      }
    }
  }

  public static void moreBytesRead(int additionalBytes) {
    if (globalInRate > 0) {
      moreBytesSlowDown(additionalBytes, inboundStartDateMillisL, inboundTotalBytesL, globalInRate);
    }
    moreBytesGlobal(additionalBytes);
  }
  public static void moreBytesWritten(int additionalBytes) {
    if (globalOutRate > 0) {
      moreBytesSlowDown(additionalBytes, outboundStartDateMillisL, outboundTotalBytesL, globalOutRate);
    }
    moreBytesGlobal(additionalBytes);
  }
  private static void moreBytesGlobal(int additionalBytes) {
    moreBytesSlowDown(additionalBytes, globalStartDateMillisL, globalTotalBytesL, globalCombinedRate);
    updateTransferRateStats();
  }

  /**
   * Updates user displayable total throughput label.
   */
  private static void updateTransferRateStats() {
    synchronized (globalStartDateMillisL) {
      synchronized (globalTotalBytesL) {
        long currentDateMillis = System.currentTimeMillis();
        long elapsedMillis = sumMillis(globalStartDateMillisL, currentDateMillis);
        long totalBytes = sumBytes(globalTotalBytesL);
        long byteRate = (long) (totalBytes / (elapsedMillis / 1000.0));

        // Update when rate changes more than 30% or is 1 second since last update has passed.
        // Don't update if the global counter has started less than 200 ms ago
        if (elapsedMillis > 200) {
          if (byteRate > (lastTransferUpdateRate * 1.3) || byteRate < (lastTransferUpdateRate / 1.3) ||
              currentDateMillis - lastTransferUpdateDateMillis > 1000) 
          {
            Stats.setTransferRate(byteRate);
            lastTransferUpdateRate = byteRate;
            lastTransferUpdateDateMillis = currentDateMillis;
          }
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

}