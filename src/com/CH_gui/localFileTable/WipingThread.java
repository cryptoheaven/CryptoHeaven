/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.localFileTable;

import com.CH_co.cryptx.*;
import com.CH_co.io.*;
import com.CH_co.monitor.*;
import com.CH_co.trace.*;
import com.CH_co.util.*;
import com.CH_gui.util.MessageDialog;

import java.io.*;
import javax.swing.*;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.15 $</b>
 *
 * @author  Marcin Kurzawa
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
      StringBuffer errBuffer = new StringBuffer();
      boolean wiped = CleanupAgent.wipe(file, in, progMonitor, true, errBuffer);
      if (errBuffer.length() > 0)
        MessageDialog.showErrorDialog(jFileChooser, errBuffer.toString(), com.CH_cl.lang.Lang.rb.getString("msgTitle_Wipe_Error"));
      if (!wiped)
        break;
      if (toRescan) {
        jFileChooser.rescanCurrentDirectory();
      }
    } // end for
    progMonitor.allDone();
  }

}