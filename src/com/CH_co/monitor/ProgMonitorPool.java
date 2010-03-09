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

package com.CH_co.monitor;

import java.util.*;

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
 * <b>$Revision: 1.10 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class ProgMonitorPool extends Object { // no-argument implicit constructor

  private static HashMap hm;
  private static ProgMonitor dummy;

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
  public static boolean isDummy(ProgMonitor progressMonitor) {
    return progressMonitor == dummy;
  }

  public static synchronized void registerProgMonitor(ProgMonitor progressMonitor, long id) {
    registerProgMonitor(progressMonitor, Long.valueOf(id));
  }

  public static synchronized void registerProgMonitor(ProgMonitor progressMonitor, Long id) {
    Object obj = hm.get(id);
    if (obj != null)
      throw new IllegalArgumentException("Specified progress monitor is already registered, cannot register again!");
    hm.put(id, progressMonitor);
  }


  public static synchronized ProgMonitor getProgMonitor(long id) {
    return (ProgMonitor) getProgMonitor(Long.valueOf(id));
  }
  public static synchronized ProgMonitor getProgMonitor(Long id) {
    ProgMonitor progressMonitor = null;

    if (id != null)
      progressMonitor = (ProgMonitor) hm.get(id);

    if (progressMonitor == null)
      progressMonitor = dummy;

    return progressMonitor;
  }


  public static synchronized void removeProgMonitor(long id) {
    removeProgMonitor(Long.valueOf(id));
  }
  public static synchronized void removeProgMonitor(Long id) {
    hm.remove(id);
  }

  public static synchronized void removeProgMonitor(ProgMonitor progressMonitor) {
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