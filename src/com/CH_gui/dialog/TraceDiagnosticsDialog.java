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


package com.CH_gui.dialog;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.records.*;

import com.CH_co.service.records.*;
import com.CH_co.trace.*;
import com.CH_co.util.*;

import com.CH_gui.frame.*;
import com.CH_gui.gui.*;
import com.CH_gui.util.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;

/**
 * This class is used to gather trace and send it off to support for debuging. <p>
 *
 * <b>Copyright</b> &copy; 2001-2012
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * <b>$Revision: 1.00 $</b>
 * @author  Marcin Kurzawa
 * @version 1.0
 */
public class TraceDiagnosticsDialog extends GeneralDialog {

  private static String STATUS_RUNNING = "Diagnostics is Running.";
  private static String STATUS_NOT_RUNNING = "Diagnostics is NOT Running.";

  JLabel jHeader;

  JButton jStart;
  JButton jStopSend;
  JButton jCancel;

  JLabel jStatus;
  JLabel jFilename;
  JLabel jFilesize;

  boolean closed;
  static final int DEFAULT_CANCEL_BUTTON = 2;

  /** Creates new TraceDiagnosticsDialog */
  public TraceDiagnosticsDialog(Frame owner) {
    super(owner, "Problem Reporting");
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TraceDiagnosticsDialog.class, "TraceDiagnosticsDialog(Frame owner)");
    if (trace != null) trace.args(owner);
    initialize(owner);
    if (trace != null) trace.exit(TraceDiagnosticsDialog.class);
  }
  /** Creates new TraceDiagnosticsDialog */
  public TraceDiagnosticsDialog(Dialog owner) {
    super(owner, "Problem Reporting");
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TraceDiagnosticsDialog.class, "TraceDiagnosticsDialog(Dialog owner)");
    if (trace != null) trace.args(owner);
    initialize(owner);
    if (trace != null) trace.exit(TraceDiagnosticsDialog.class);
  }

  private void initialize(Component owner) {
    JButton[] jButtons = createButtons();
    JPanel jPanel = createPanel();
    super.init(owner, jButtons, jPanel, DEFAULT_CANCEL_BUTTON, DEFAULT_CANCEL_BUTTON);
  }

  private JButton[] createButtons() {
    JButton[] jButtons = new JButton[3];

    jButtons[0] = new JMyButton("Start Diagnostics");
    jButtons[0].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        TraceDiagnostics.traceStart(FetchedDataCache.getSingleInstance().getMyUserId());
      }
    });
    jStart = jButtons[0];
    jButtons[1] = new JMyButton("Stop and Send");
    jButtons[1].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        File traceFile = TraceProperties.getLastTraceFile();
        TraceDiagnostics.traceStop();
        new MessageFrame(new Record[] { new EmailAddressRecord(URLs.get(URLs.SUPPORT_EMAIL)) }, "Support Request with Diagnostics", URLs.get(URLs.SUPPORT_BODY), new File[] { traceFile });
        closeDialog();
      }
    });
    jStopSend = jButtons[1];
    jButtons[2] = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Cancel"));
    jButtons[2].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        closeDialog();
      }
    });
    jCancel = jButtons[2];

    return jButtons;
  }

  private JPanel createPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

    jHeader = new JMyLabel("<html><font size='+1'>Diagnostics Information Gathering and Reporting</font><br><ol><li>Press 'Start Diagnostics'.</li><li>Perform the shortest usage case that<br>demonstrates the problem.</li><li>Press 'Stop and Send' and briefly describe the<br>problem in a message window.</li></ol></html>");
    jHeader.setIcon(Images.get(ImageNums.TOOLS_FIX32));
    jHeader.setHorizontalTextPosition(SwingConstants.TRAILING);
    jHeader.setVerticalTextPosition(SwingConstants.TOP);

    jStatus = new JMyLabel(STATUS_NOT_RUNNING);
    jFilename = new JMyLabel("File: ");
    jFilesize = new JMyLabel("Size: ");

    Thread th = new ThreadTraced("Trace Size Updater") {
      public void runTraced() {
        while (!closed) {
          boolean tracing = TraceProperties.isTraceEnabled();
          if (tracing && !jStatus.getText().equals(STATUS_RUNNING))
            jStatus.setText(STATUS_RUNNING);
          else if (!tracing && !jStatus.getText().equals(STATUS_NOT_RUNNING))
            jStatus.setText(STATUS_NOT_RUNNING);
          if (tracing) {
            File traceFile = TraceProperties.getLastTraceFile();
            jFilename.setText(traceFile != null ? "File: " + traceFile.getName() : "File: ");
            jFilesize.setText(traceFile != null ? "Size: " + Misc.getFormattedSize(traceFile.length(), 4, 3) : "Size: ");
            if (jStart.isEnabled())
              jStart.setEnabled(false);
            if (!jStopSend.isEnabled())
              jStopSend.setEnabled(true);
          } else {
            if (!jStart.isEnabled())
              jStart.setEnabled(true);
            if (jStopSend.isEnabled())
              jStopSend.setEnabled(false);
          }
          try {
            Thread.sleep(200);
          } catch (InterruptedException e) {
          }
        }
      }
    };
    th.setDaemon(true);
    th.start();

    int posY = 0;
    panel.add(jHeader, new GridBagConstraints(0, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(10, 10, 5, 10), 0, 0));
    posY ++;
    panel.add(jStatus, new GridBagConstraints(0, posY, 2, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 10, 3, 10), 0, 0));
    posY ++;
    panel.add(jFilename, new GridBagConstraints(0, posY, 2, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 10, 3, 10), 0, 0));
    posY ++;
    panel.add(jFilesize, new GridBagConstraints(0, posY, 2, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 10, 10, 10), 0, 0));
    posY ++;

    return panel;
  }

  public void closeDialog() {
    TraceDiagnostics.traceStop();
    closed = true;
    super.closeDialog();
  }

}