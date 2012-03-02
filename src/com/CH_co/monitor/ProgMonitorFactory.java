/*
 * Copyright 2001-2012 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_co.monitor;

import com.CH_co.service.records.FileLinkRecord;
import java.io.File;

/**
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class ProgMonitorFactory {

  private static Class implJournal;
  private static Class implLogin;
  private static Class implMulti;
  private static Class implTransfer;
  private static Class implWipe;


  public static void setImplJournal(Class impl) {
    implJournal = impl;
  }

  public static void setImplLogin(Class impl) {
    implLogin = impl;
  }

  public static void setImplMulti(Class impl) {
    implMulti = impl;
  }

  public static void setImplTransfer(Class impl) {
    implTransfer = impl;
  }

  public static void setImplWipe(Class impl) {
    implWipe = impl;
  }

  public static ProgMonitorJournalI newInstanceJournal(String title) {
    ProgMonitorJournalI monitor = null;
    if (implJournal != null) {
      try {
        monitor = (ProgMonitorJournalI) implJournal.newInstance();
        monitor.init(title);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    if (monitor == null) {
      monitor = new DefaultProgMonitorJournal();
    }
    return monitor;
  }

  public static ProgMonitorI newInstanceLogin(String title, String[] tasks, String infoNote) {
    ProgMonitorI monitor = null;
    if (implLogin != null) {
      try {
        monitor = (ProgMonitorLoginI) implLogin.newInstance();
        ((ProgMonitorLoginI) monitor).init(title, tasks, infoNote);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    if (monitor == null) {
      monitor = new DefaultProgMonitor();
    }
    return monitor;
  }

  public static ProgMonitorMultiI newInstanceMulti(Object parentComponent, Object message, String note, int min, int max) {
    ProgMonitorMultiI monitor = null;
    if (implMulti != null) {
      try {
        monitor = (ProgMonitorMultiI) implMulti.newInstance();
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

  public static ProgMonitorI newInstanceTransferDown(String[] tasks, File destDir, FileLinkRecord[] fileLinks, boolean isDownload, boolean suppressTransferSoundsAndAutoClose) {
    ProgMonitorI monitor = null;
    if (implTransfer != null) {
      try {
        monitor = (ProgMonitorTransferI) implTransfer.newInstance();
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

  public static ProgMonitorI newInstanceTransferUp(File[] tasks) {
    ProgMonitorI monitor = null;
    if (implTransfer != null) {
      try {
        monitor = (ProgMonitorTransferI) implTransfer.newInstance();
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
    if (implWipe != null) {
      try {
        monitor = (ProgMonitorWipeI) implWipe.newInstance();
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