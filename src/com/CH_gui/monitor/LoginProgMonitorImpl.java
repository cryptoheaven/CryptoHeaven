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

package com.CH_gui.monitor;

import com.CH_gui.util.*;

import com.CH_co.monitor.*;
import com.CH_co.service.msg.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.gui.*;

import java.awt.*;
import javax.swing.*;

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
 * <b>$Revision: 1.12 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class LoginProgMonitorImpl extends JFrame implements ProgMonitorLoginI {

  private String title;

  private JLabel jImageLabel;

  private JCheckBox[] jCheckTasks;
  private JLabel jStatus;
  private JProgressBar jProgressBar;

  /** Creates new LoginProgMonitorImpl */
  public LoginProgMonitorImpl() {
  }

  /**
   * Creates new LoginProgMonitorImpl
   * Every task takes 4 steps, start send, done send, start receive, done receive.
   */
  public void init(String title, String[] tasks) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(LoginProgMonitorImpl.class, "init(String title, String[] tasks)");
    if (trace != null) trace.args(title, tasks);

    this.title = title;
    setTitle(title);

    int initProgBarMin = 0;
    int initProgBarMax = tasks.length * 4;

    initPanelComponents(tasks, initProgBarMin, initProgBarMax);

    getContentPane().add("Center", createMainPanel());
    ImageIcon frameIcon = Images.get(ImageNums.FRAME_LOCK32);
    if (frameIcon != null) {
      setIconImage(frameIcon.getImage());
    }

    pack();
    Dimension dimension = getSize();
    Dimension screenDimension = MiscGui.getScreenUsableSize(0, 0, this);
    setLocation(screenDimension.width/2 - dimension.width/2, screenDimension.height/2 - dimension.height/2);
    setVisible(true);
    if (trace != null) trace.exit(LoginProgMonitorImpl.class);
  }

  private void initPanelComponents(String[] tasks, int initProgBarMin, int initProgBarMax) {

    jImageLabel = new JMyLabel(Images.get(ImageNums.ANIM_KEY));
    jImageLabel.setPreferredSize(new Dimension(110, 60));

    jCheckTasks = new JCheckBox[tasks.length];
    for (int i=0; i < tasks.length; i++) {
      jCheckTasks[i] = new JMyCheckBox(tasks[i]);
      MiscGui.setPlainFont(jCheckTasks[i]);
      jCheckTasks[i].setEnabled(false);
      jCheckTasks[i].setSelected(false);
    }

    jStatus = new JMyLabel("Opening a Secure Channel and requesting Login, waiting for reply ...");

    jProgressBar = new JProgressBar();
    jProgressBar.setMinimum(initProgBarMin);
    jProgressBar.setMaximum(initProgBarMax);

  }

  private JPanel createMainPanel() {

    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

    Insets insetFive = new MyInsets(5, 5, 5, 5);
    Insets insetNote = new MyInsets(0, 5, 0, 5);

    /* Add all tasks as check boxes */
    int index = 0;

    // if there is an icon, move tasks to the second column and make them take 1 column width
    int gridX = 0;
    int gridWidth = 2;
    if (jImageLabel != null) {
      gridWidth = 1;
      gridX = 1;
    }

    for ( ; index < jCheckTasks.length; index++) {
      panel.add(jCheckTasks[index], new GridBagConstraints(gridX, index, gridWidth, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insetNote, 0, 0));
    }

    if (index == 0)
      index = 1;

    // insert icon
    if (jImageLabel != null) {
      panel.add(jImageLabel, new GridBagConstraints(0, 0, 1, index, 0, 0,
          GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, insetFive, 0, 0));
    }

    /* Add other info such as time left, transfer rate, ... */
    panel.add(jStatus, new GridBagConstraints(0, index, 2, 1, 10, 0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insetFive, 0, 0));
    index ++;

    panel.add(jProgressBar, new GridBagConstraints(0, index, 2, 1, 10, 0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insetFive, 0, 0));

    return panel;
  }


  public void closeProgMonitor() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(LoginProgMonitorImpl.class, "closeProgMonitor()");
    ProgMonitorPool.removeProgMonitor(this);
    Stats.stopGlobe(this);

    setVisible(false);
    MiscGui.removeAllComponentsAndListeners(this);
    dispose();
    if (trace != null) trace.exit(LoginProgMonitorImpl.class);
  }




  /*================================================*/
  /*   P r o g M o n i t o r    Interface Methods   */
  /*================================================*/

  private int currentTask = -1;
  private int progressValue;
  private boolean allDone;
  private boolean killed;

  public void enqueue(int actionCode, long stamp) {
  }
  public void dequeue(int actionCode, long stamp) {
  }
  public void startSend(int actionCode, long stamp) {
  }
  public void startSendAction(String actionName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(LoginProgMonitorImpl.class, "startSendAction(String actionName)");
    if (trace != null) trace.args(actionName);
    setCurrentStatus("Requesting: " + actionName + " ... ");
    progressValue ++;
    jProgressBar.setValue(progressValue);
    if (trace != null) trace.exit(LoginProgMonitorImpl.class);
  }
  public void startSendData(String dataName) {
  }
  public void doneSend(int actionCode, long stamp) {
  }
  public void doneSendAction(String actionName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(LoginProgMonitorImpl.class, "doneSendAction(String actionName)");
    if (trace != null) trace.args(actionName);
    setCurrentStatus("Requesting: " + actionName + ", waiting for reply...");
    progressValue ++;
    jProgressBar.setValue(progressValue);
    if (trace != null) trace.exit(LoginProgMonitorImpl.class);
  }
  public void doneSendData(String dataName) {
  }
  public void startReceive(int actionCode, long stamp) {
  }
  public void startReceiveAction(String actionName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(LoginProgMonitorImpl.class, "startReceiveAction(String actionName)");
    if (trace != null) trace.args(actionName);
    setCurrentStatus("Receiving: " + actionName + " ... ");
    progressValue ++;
    jProgressBar.setValue(progressValue);
    if (trace != null) trace.exit(LoginProgMonitorImpl.class);
  }
  public void startReceiveData(String dataName) {
  }
  public void doneReceive(int actionCode, long stamp) {
  }
  public void doneReceiveAction(String actionName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(LoginProgMonitorImpl.class, "doneReceiveAction(String actionName)");
    if (trace != null) trace.args(actionName);
    setCurrentStatus("Receiving: " + actionName + " ... done.");
    progressValue ++;
    jProgressBar.setValue(progressValue);
    if (trace != null) trace.exit(LoginProgMonitorImpl.class);
  }
  public void doneReceiveData(String dataName) {
  }
  public void startExecution(int actionCode) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(LoginProgMonitorImpl.class, "startExecution(int actionCode)");
    if (trace != null) trace.args(actionCode);
    String actionName = MessageActionNameSwitch.getActionInfoName(actionCode);
    setCurrentStatus("Executing: " + actionName + " ... ");
    progressValue ++;
    jProgressBar.setValue(progressValue);
    Stats.setStatus("Executing reply ... [" + actionName + "]");
    if (trace != null) trace.exit(LoginProgMonitorImpl.class);
  }
  public void doneExecution(int actionCode) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(LoginProgMonitorImpl.class, "doneExecution(int actionCode)");
    if (trace != null) trace.args(actionCode);
    String actionName = MessageActionNameSwitch.getActionInfoName(actionCode);
    setCurrentStatus("Executing: " + actionName + " ... done.");
    progressValue ++;
    jProgressBar.setValue(progressValue);
    Stats.setStatus("Action completed. [" + actionName + "]");
    Stats.stopGlobe(this);
    if (trace != null) trace.exit(LoginProgMonitorImpl.class);
  }
  public void setCurrentStatus(String currentStatus) {
    jStatus.setText(currentStatus);
  }
  public void setFileNameSource(String fileName) {
  }
  public void setFileNameDestination(String fileName) {
  }
  public void setTransferSize(long size) {
  }
  public void addBytes(long bytes) {
  }
  public void doneTransfer() {
  }
  public void nextTask() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(LoginProgMonitorImpl.class, "nextTask()");
    if (currentTask >= 0 && currentTask < jCheckTasks.length) {
      MiscGui.setPlainFont(jCheckTasks[currentTask]);
      jCheckTasks[currentTask].setSelected(true);
      jImageLabel.setIcon(Images.get(ImageNums.ANIM_TRANSFER));
    }
    currentTask ++;
    if (currentTask < jCheckTasks.length) {
      MiscGui.setBoldFont(jCheckTasks[currentTask]);
      setTitle(title + ": " + jCheckTasks[currentTask].getText());
    }
    if (trace != null) trace.exit(LoginProgMonitorImpl.class);
  } // end nextTask()

  public void allDone() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(LoginProgMonitorImpl.class, "allDone()");
    allDone = true;
    setTitle(title + ": " + "Done");
    closeProgMonitor();
    if (trace != null) trace.exit(LoginProgMonitorImpl.class);
  } // end allDone()

  public void jobKilled() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(LoginProgMonitorImpl.class, "jobKilled()");
    killed = true;
    setCurrentStatus("Failed: Waiting to retry...");
    Stats.stopGlobe(this);
    if (trace != null) trace.exit(LoginProgMonitorImpl.class);
  }
  public void jobForRetry() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(LoginProgMonitorImpl.class, "jobForRetry()");
    setCurrentStatus("Job Queued for Retry");
    Stats.stopGlobe(this);
    if (trace != null) trace.exit(LoginProgMonitorImpl.class);
  }

  public void setInterrupt(Interruptible interruptible) {
  }

  public boolean isAllDone() {
    return allDone;
  }

  public boolean isCancelled() {
    return false;
  }

  public void setCancellable(Cancellable cancellable) {
  }

  public boolean isJobKilled() {
    return killed;
  }

  public void nextTask(String task) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void appendLine(String info) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}