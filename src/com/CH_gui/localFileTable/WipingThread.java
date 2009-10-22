/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_gui.localFileTable;

import java.io.*;
import javax.swing.*;

import com.CH_cl.monitor.*;

import com.CH_co.cryptx.*;
import com.CH_co.io.*;
import com.CH_co.monitor.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2009
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
 * <b>$Revision: 1.15 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class WipingThread extends Thread {

  private File[] filesToWipe;
  private JFileChooser jFileChooser;


  protected WipingThread(File[] filesToWipe, JFileChooser jFileChooser) {
    super("Wiping Thread");
    this.filesToWipe = filesToWipe;
    this.jFileChooser = jFileChooser;
    setPriority(Thread.MIN_PRIORITY);
    setDaemon(true);
  }

  public void run() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "run()");
    WipeProgMonitor progMonitor = new WipeProgMonitor();
    InterruptibleInputStream in = new InterruptibleInputStream(new RandomInputStream(Rnd.getSecureRandom()));
    progMonitor.setInterrupt(in);
    for (int i=0; i<filesToWipe.length; i++) {
      File file = filesToWipe[i];
       boolean toRescan = false;
      if (jFileChooser != null && jFileChooser.getCurrentDirectory().equals(file.getParentFile())) {
        toRescan = true;
      }
      if (!CleanupAgent.wipe(file, in, jFileChooser, progMonitor)) {
        break;
      }
      if (toRescan) {
        jFileChooser.rescanCurrentDirectory();
      }
    } // end for
    progMonitor.allDone();
    if (trace != null) trace.data(300, Thread.currentThread().getName() + " done.");
    if (trace != null) trace.exit(getClass());
    if (trace != null) trace.clear();
  }

}