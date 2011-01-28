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

import java.util.*;
import java.util.ArrayList;

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
 * <b>$Revision: 1.10 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class ProgMonitorPool extends Object { // no-argument implicit constructor

  private static HashMap hm;
  private static ProgMonitorI dummy;

  // static initializer
  static {
    hm = new HashMap();
    //dummy = new ProgMonitorDummy();
    dummy = new ProgMonitorDumping();
  }

  /**
   * All static fields and methods -- hide the constructor.
   */
  private ProgMonitorPool() {
  }

  /**
   * @return true if specified progress monitor is a dummy monitor.
   */
  public static boolean isDummy(ProgMonitorI progressMonitor) {
    return progressMonitor == dummy;
  }

  public static synchronized void registerProgMonitor(ProgMonitorI progressMonitor, long id) {
    registerProgMonitor(progressMonitor, new Long(id));
  }

  public static synchronized void registerProgMonitor(ProgMonitorI progressMonitor, Long id) {
    Object obj = hm.get(id);
    if (obj != null)
      throw new IllegalArgumentException("Specified progress monitor is already registered, cannot register again!");
    hm.put(id, progressMonitor);
  }


  public static synchronized ProgMonitorI getProgMonitor(long id) {
    return (ProgMonitorI) getProgMonitor(new Long(id));
  }
  public static synchronized ProgMonitorI getProgMonitor(Long id) {
    ProgMonitorI progressMonitor = null;

    if (id != null)
      progressMonitor = (ProgMonitorI) hm.get(id);

    if (progressMonitor == null)
      progressMonitor = dummy;

    return progressMonitor;
  }

  public static synchronized void killAll() {
    ArrayList list = new ArrayList(hm.values());
    for (int i=0; i<list.size(); i++) {
      ProgMonitorI pm = (ProgMonitorI) list.get(i);
      pm.jobKilled();
    }
  }

  public static synchronized void removeProgMonitor(long id) {
    removeProgMonitor(new Long(id));
  }
  public static synchronized void removeProgMonitor(Long id) {
    hm.remove(id);
  }

  public static synchronized void removeProgMonitor(ProgMonitorI progressMonitor) {
    Iterator keys = hm.keySet().iterator();
    while (keys.hasNext()) {
      Object key = keys.next();
      if (hm.get(key) == progressMonitor) {
        hm.remove(key);
        break;
      }
    }
  }

}