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

package com.CH_co.monitor;

import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import java.util.*;

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
 * <b>$Revision: 1.19 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class Stats extends Object {

  protected static final LinkedList statusHistoryL = new LinkedList();
  protected static final LinkedList statusHistoryDatesL = new LinkedList(); // pair dates for the string entries.. always go hand-in-hand
  private static final int MAX_HISTORY_SIZE = 100;

  private static HashMap globeMoversTraceHM = new HashMap(); // for debug, stack traces of our Movers

  protected static String lastStatus;
  protected static Boolean lastMovingStatus;
  protected static Long pingMS;
  protected static Boolean onlineStatus;
  protected static Integer connectionsPlain;
  protected static Integer connectionsHTML;
  protected static Long transferRate;
  protected static Long sizeBytes;

  private static long maxTransferRate;
  private static Date initDate = new Date();

  protected static final Object monitor = new Object();
  private static ArrayList statsListeners = new ArrayList();


  public static String getStatusLabel() {
    return lastStatus;
  }
  public static Long getPingMS() {
    return pingMS;
  }
  public static Boolean getOnlineStatus() {
    return onlineStatus;
  }
  public static Integer getConnectionsPlain() {
    return connectionsPlain;
  }
  public static Integer getConnectionsHTML() {
    return connectionsHTML;
  }
  public static Long getTransferRate() {
    return transferRate;
  }
  public static long getMaxTransferRate() {
    return maxTransferRate;
  }
  public static Long getSizeBytes() {
    return sizeBytes;
  }
  public static Date getInitDate() {
    return initDate;
  }

  public static ArrayList[] getStatsHistoryLists() {
    ArrayList historyL = null;
    ArrayList historyDatesL = null;
    synchronized (monitor) {
      historyL = new ArrayList(statusHistoryL);
      historyDatesL = new ArrayList(statusHistoryDatesL);
    }
    return new ArrayList[] { historyL, historyDatesL };
  }

  public static ArrayList getGlobeMoversTraceL() {
    ArrayList moversTraceL = null;
    synchronized (monitor) {
      if (globeMoversTraceHM.size() > 0) {
        moversTraceL = new ArrayList(globeMoversTraceHM.values());
      }
    }
    return moversTraceL;
  }

  public static void moveGlobe(Object mover) {
    synchronized (monitor) {
      if (globeMoversTraceHM.size() == 0) {
        lastMovingStatus = Boolean.TRUE;
        for (int i=0; i<statsListeners.size(); i++) {
          StatsListenerI listener = (StatsListenerI) statsListeners.get(i);
          listener.setStatsGlobeMove(lastMovingStatus);
        }
      }
      if (!globeMoversTraceHM.containsKey(mover)) {
        globeMoversTraceHM.put(mover, Misc.getStack(new Throwable(""+mover+" at " + new Date())));
      }
    }
  }

  public static void stopGlobe(Object mover) {
    synchronized (monitor) {
      if (globeMoversTraceHM.containsKey(mover)) {
        globeMoversTraceHM.remove(mover);
        if (globeMoversTraceHM.size() == 0) {
          lastMovingStatus = Boolean.FALSE;
          for (int i=0; i<statsListeners.size(); i++) {
            StatsListenerI listener = (StatsListenerI) statsListeners.get(i);
            listener.setStatsGlobeMove(lastMovingStatus);
          }
        }
      }
    }
  }

  public static void setStatus(String newStatus) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(Stats.class, "setStatus(String newStatus)");
    if (trace != null) trace.args(newStatus);
    synchronized (monitor) {
      lastStatus = newStatus;
      for (int i=0; i<statsListeners.size(); i++) {
        StatsListenerI listener = (StatsListenerI) statsListeners.get(i);
        listener.setStatsLastStatus(lastStatus);
      }
      statusHistoryL.addFirst(newStatus);
      statusHistoryDatesL.addFirst(new Date());
      while (statusHistoryL.size() > MAX_HISTORY_SIZE) {
        statusHistoryL.removeLast();
        statusHistoryDatesL.removeLast();
      }
    }
    if (trace != null) trace.exit(Stats.class);
  }

  public static void setPing(long ms) {
    synchronized (monitor) {
      boolean updated = pingMS == null || pingMS.longValue() != ms;
      if (updated) {
        pingMS = new Long(ms);
        for (int i=0; i<statsListeners.size(); i++) {
          StatsListenerI listener = (StatsListenerI) statsListeners.get(i);
          listener.setStatsPing(pingMS);
        }
      }
    }
  }

  public static void setConnections(int connectionCount, int[] connectionTypeCounts) {
    synchronized (monitor) {
      boolean updated = connectionsPlain == null || connectionsPlain.intValue() != connectionTypeCounts[0]
              || connectionsHTML == null || connectionsHTML.intValue() != connectionTypeCounts[1];
      if (updated) {
        connectionsPlain = new Integer(connectionTypeCounts[0]);
        connectionsHTML = new Integer(connectionTypeCounts[1]);
        if (connectionCount > 0) {
          onlineStatus = Boolean.TRUE;
        } else {
          onlineStatus = Boolean.FALSE;
        }
        for (int i=0; i<statsListeners.size(); i++) {
          StatsListenerI listener = (StatsListenerI) statsListeners.get(i);
          listener.setStatsConnections(connectionsPlain, connectionsHTML);
        }
      }
    }
  }

  public static void setTransferRate(long bytesPerSecond) {
    synchronized (monitor) {
      boolean updated = transferRate == null || transferRate.longValue() != bytesPerSecond;
      if (updated) {
        maxTransferRate = Math.max(maxTransferRate, bytesPerSecond);
        transferRate = new Long(bytesPerSecond);
        for (int i=0; i<statsListeners.size(); i++) {
          StatsListenerI listener = (StatsListenerI) statsListeners.get(i);
          listener.setStatsTransferRate(transferRate);
        }
      }
    }
  }

  public static void setSizeBytes(long size) {
    synchronized (monitor) {
      boolean updated = sizeBytes == null || sizeBytes.longValue() != size;
      sizeBytes = new Long(size);
      if (updated) {
        for (int i=0; i<statsListeners.size(); i++) {
          StatsListenerI listener = (StatsListenerI) statsListeners.get(i);
          listener.setStatsSizeBytes(sizeBytes);
        }
      }
    }
  }

  public static void addStatsListener(StatsListenerI listener) {
    synchronized (monitor) {
      statsListeners.add(listener);
      listener.setStatsConnections(connectionsPlain, connectionsHTML);
      listener.setStatsGlobeMove(lastMovingStatus);
      listener.setStatsLastStatus(lastStatus);
      listener.setStatsPing(pingMS);
      listener.setStatsSizeBytes(sizeBytes);
      listener.setStatsTransferRate(transferRate);
    }
  }

  public static void removeStatsListener(StatsListenerI listener) {
    synchronized (monitor) {
      statsListeners.remove(listener);
    }
  }

}