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

import com.CH_co.service.records.FileLinkRecord;
import java.io.File;

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
public class ProgMonitorFactory {

  private static Class implementationLogin;
  private static Class implementationMulti;
  private static Class implementationTransfer;
  private static Class implementationWipe;

  public static void setImplementationLogin(Class impl) {
    implementationLogin = impl;
  }
  public static Class getImplementationLogin() {
    return implementationLogin;
  }

  public static void setImplementationMulti(Class impl) {
    implementationMulti = impl;
  }
  public static Class getImplementationMulti() {
    return implementationMulti;
  }

  public static void setImplementationTransfer(Class impl) {
    implementationTransfer = impl;
  }
  public static Class getImplementationTransfer() {
    return implementationTransfer;
  }

  public static void setImplementationWipe(Class impl) {
    implementationWipe = impl;
  }
  public static Class getImplementationWipe() {
    return implementationWipe;
  }

  public static ProgMonitorMultiI newInstanceMulti(Object parentComponent, Object message, String note, int min, int max) {
    ProgMonitorMultiI monitor = null;
    if (implementationMulti != null) {
      try {
        monitor = (ProgMonitorMultiI) implementationMulti.newInstance();
        monitor.init(parentComponent, message, note, min, max);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    if (monitor == null) {
      monitor = new DefaultProgMonitorMulti();
    }
    return monitor;
  }

  public static ProgMonitorI newInstanceLogin(String title, String[] tasks) {
    ProgMonitorI monitor = null;
    if (implementationLogin != null) {
      try {
        monitor = (ProgMonitorLoginI) implementationLogin.newInstance();
        ((ProgMonitorLoginI) monitor).init(title, tasks);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    if (monitor == null) {
      monitor = new DefaultProgMonitor();
    }
    return monitor;
  }

  public static ProgMonitorI newInstanceTransferDown(String[] tasks, File destDir, FileLinkRecord[] fileLinks, boolean isDownload, boolean suppressTransferSoundsAndAutoClose) {
    ProgMonitorI monitor = null;
    if (implementationTransfer != null) {
      try {
        monitor = (ProgMonitorTransferI) implementationTransfer.newInstance();
        ((ProgMonitorTransferI) monitor).init(tasks, destDir, fileLinks, isDownload, suppressTransferSoundsAndAutoClose);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    if (monitor == null) {
      monitor = new DefaultProgMonitor();
    }
    return monitor;
  }

  public static ProgMonitorI newInstanceTransferUp(String[] tasks) {
    ProgMonitorI monitor = null;
    if (implementationTransfer != null) {
      try {
        monitor = (ProgMonitorTransferI) implementationTransfer.newInstance();
        ((ProgMonitorTransferI) monitor).init(tasks);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    if (monitor == null) {
      monitor = new DefaultProgMonitor();
    }
    return monitor;
  }

  public static ProgMonitorI newInstanceWipe(Interruptible interruptible) {
    ProgMonitorI monitor = null;
    if (implementationWipe != null) {
      try {
        monitor = (ProgMonitorWipeI) implementationWipe.newInstance();
        ((ProgMonitorWipeI) monitor).init(interruptible);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    if (monitor == null) {
      monitor = new DefaultProgMonitor();
    }
    return monitor;
  }

}