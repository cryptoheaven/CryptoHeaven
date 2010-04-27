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

package com.CH_gui.localFileTable;

import java.io.*;
import javax.swing.*;

import com.CH_co.cryptx.*;
import com.CH_co.io.*;
import com.CH_co.monitor.*;
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
 * <b>$Revision: 1.15 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class WipingThread extends ThreadTraced {

  private File[] filesToWipe;
  private JFileChooser jFileChooser;


  protected WipingThread(File[] filesToWipe, JFileChooser jFileChooser) {
    super("Wiping Thread");
    this.filesToWipe = filesToWipe;
    this.jFileChooser = jFileChooser;
    setDaemon(true);
  }

  public void runTraced() {
    InterruptibleInputStream in = new InterruptibleInputStream(new RandomInputStream(Rnd.getSecureRandom()));
    ProgMonitorI progMonitor = ProgMonitorFactory.newInstanceWipe(in);
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
  }

}