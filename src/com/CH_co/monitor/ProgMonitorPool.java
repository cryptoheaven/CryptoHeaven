/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.monitor;

import java.util.*;
import java.util.ArrayList;

/** 
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.10 $</b>
*
* @author  Marcin Kurzawa
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
    Long ID = new Long(id);
    Object obj = hm.get(ID);
    if (obj != null)
      throw new IllegalArgumentException("Specified progress monitor is already registered, cannot register again!");
    hm.put(ID, progressMonitor);
  }

  public static synchronized ProgMonitorI getProgMonitor(long id) {
    Long ID = new Long(id);
    ProgMonitorI progressMonitor = (ProgMonitorI) hm.get(ID);
    if (progressMonitor == null)
      progressMonitor = dummy;
    return progressMonitor;
  }

  public static synchronized Long getProgMonitorId(ProgMonitorI progressMonitor) {
    Long id = null;
    Iterator keys = hm.keySet().iterator();
    while (keys.hasNext()) {
      Object key = keys.next();
      if (hm.get(key) == progressMonitor) {
        id = (Long) key;
        break;
      }
    }
    return id;
  }

  public static synchronized void killAll() {
    ArrayList list = new ArrayList(hm.values());
    for (int i=0; i<list.size(); i++) {
      ProgMonitorI pm = (ProgMonitorI) list.get(i);
      pm.jobKilled();
    }
  }

  public static synchronized void removeProgMonitor(long id) {
    Long ID = new Long(id);
    hm.remove(ID);
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