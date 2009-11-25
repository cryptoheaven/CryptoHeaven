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

package com.CH_cl.monitor;

import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.util.LinkedList;

import com.CH_co.gui.*;
import com.CH_co.monitor.*;
import com.CH_co.service.msg.MessageActionNameSwitch;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_co.util.GlobalProperties;

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
 * <b>$Revision: 1.13 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class WipeProgMonitor extends JFrame implements ProgMonitor {

  private String title;

  private JLabel jImageLabel;


  private JTextArea jTextArea;
  private JScrollPane jScrollPane;
  private JLabel jStatus;
  private JProgressBar jProgressBar;
  private JLabel[] jNoteHeadings;
  private JLabel[] jNotes;
  private JCheckBox jCloseOnDone;

  private JButton jCancelButton;

  private boolean cancelled;
  private Interruptible interrupt;

  private final Object monitor = new Object();


  private String name;

  private static final Object counterMonitor = new Object();
  private static int counter = 0;


  /** Creates new WipeProgMonitor */
  private WipeProgMonitor(String title, String[] noteHeadings, String[] notes, 
                          int initProgBarMin, int initProgBarMax) 
  {
    super(title);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "WipeProgMonitor()");

    synchronized (counterMonitor) {
      name = WipeProgMonitor.class.getName() + " #" + counter;
      if (trace != null) trace.data(10, "creating... ", name);
      counter ++;
      if (counter == Integer.MAX_VALUE)
        counter = 0;
    }


    this.title = title;

    initPanelComponents(noteHeadings, notes, initProgBarMin, initProgBarMax);

    getRootPane().setDefaultButton(jCancelButton);
    getContentPane().add("Center", createMainPanel());

    getContentPane().add("South", MiscGui.createButtonPanel(new JButton[] { jCancelButton }));

    ImageIcon frameIcon = Images.get(ImageNums.FRAME_LOCK32);
    if (frameIcon != null) {
      setIconImage(frameIcon.getImage());
    }

    pack();
    // make it a bit wider
    Dimension prefDim = getPreferredSize();
    setSize(prefDim.width + 200, prefDim.height + 20);
    setLocation(MiscGui.getSuggestedSpreadedWindowLocation(this));
    setVisible(true);

    if (trace != null) trace.exit(WipeProgMonitor.class);
  }


  /** 
   * File Wipe
   */
  public WipeProgMonitor() {
    this("Secure File Wipe", 
              new String[] { "Estimated Time:", "From:", "To:", "Transfer Rate:" }, 
              new String[] { " ... ", " ... ", " ... ", " ... " },
              0, 100);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "WipeProgMonitor()");
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }


  private void initPanelComponents(String[] noteHeadings, String[] notes, int initProgBarMin, int initProgBarMax) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "initPanelComponents(String[] noteHeadings, String[] notes, int initProgBarMin, int initProgBarMax)");
    if (trace != null) trace.args(noteHeadings, notes);
    if (trace != null) trace.args(initProgBarMin);
    if (trace != null) trace.args(initProgBarMax);

    jImageLabel = new JMyLabel(Images.get(ImageNums.ANIM_LOCK));

    jTextArea = new JMyTextArea();
    MiscGui.initKeyBindings(jTextArea);
    jTextArea.setEditable(false);
    jTextArea.setMinimumSize(new Dimension(300, 100));
    jScrollPane = new JScrollPane(jTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

    jStatus = new JMyLabel("Status:");

    jProgressBar = new JProgressBar();
    jProgressBar.setMinimum(initProgBarMin);
    jProgressBar.setMaximum(initProgBarMax);

    jNoteHeadings = new JLabel[noteHeadings.length];
    for (int i=0; i<noteHeadings.length; i++) {
      jNoteHeadings[i] = new JMyLabel(noteHeadings[i]);
    }

    jNotes = new JLabel[notes.length];
    for (int i=0; i<notes.length; i++) {
      jNotes[i] = new JMyLabel(notes[i]);
    }

    jCloseOnDone = new JMyCheckBox("Close this dialog when all processes finish.");
    jCloseOnDone.setSelected(Boolean.valueOf(GlobalProperties.getProperty("ProgMonitor.WipeProgMonitor.closeOnDone")).booleanValue());
    jCloseOnDone.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "actionPerformed(ActionEvent event)");
        if (trace != null) trace.args(event);
        if (trace != null) trace.data(10, name);
        GlobalProperties.setProperty("ProgMonitor.WipeProgMonitor.closeOnDone", "" + 
          WipeProgMonitor.this.jCloseOnDone.isSelected());
        if (trace != null) trace.exit(getClass());
      }
    });

    jCancelButton = new JMyButton("Cancel");
    jCancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "actionPerformed(ActionEvent event)");
        if (trace != null) trace.args(event);
        if (trace != null) trace.data(10, name);
        cancelled = true;
        Interruptible ourInterrupt = interrupt;
        if (allDone || jobKilled) {
          closeProgMonitor();
        }
        else if (ourInterrupt != null) {
          closeProgMonitor();
          ourInterrupt.interrupt();
        }
        if (trace != null) trace.exit(getClass());
      }
    });

    if (trace != null) trace.exit(WipeProgMonitor.class);
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

    panel.add(jScrollPane, new GridBagConstraints(gridX, index, gridWidth, 1, 10, 10, 
      GridBagConstraints.WEST, GridBagConstraints.BOTH, insetFive, 0, 0));
    index ++;

    if (index == 0)
      index = 1;

    // insert icon
    if (jImageLabel != null) {
      panel.add(jImageLabel, new GridBagConstraints(0, 0, 1, index, 0, 0, 
          GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insetFive, 0, 0));
    }

    /* Add other info such as time left, transfer rate, ... */
    panel.add(jStatus, new GridBagConstraints(0, index, 2, 1, 10, 0, 
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insetFive, 0, 0));
    index ++;

    panel.add(jProgressBar, new GridBagConstraints(0, index, 2, 1, 10, 0, 
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insetFive, 0, 0));
    index++;

    int k, m;
    for (k=0; k<jNoteHeadings.length; k++) {
      int hSpaces = 1;
      if (jNotes[k].getText().length() == 0)
        hSpaces = 2;

      panel.add(jNoteHeadings[k], new GridBagConstraints(0, index+k, hSpaces, 1, 0, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insetNote, 0, 0));
    }
    for (m=0; m<jNotes.length; m++) {
      panel.add(jNotes[m], new GridBagConstraints(1, index+m, 1, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insetNote, 0, 0));
    }
    index += k > m ? k : m; 

    panel.add(jCloseOnDone, new GridBagConstraints(0, index, 2, 1, 10, 0, 
      GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insetFive, 0, 0));

    index ++;

    // filler
    //panel.add(new JMyLabel(), new GridBagConstraints(0, index, 2, 1, 10, 10, 
      //GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0,0,0,0), 0, 0));

    return panel;
  }

  public void closeProgMonitor() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "closeProgMonitor()");
    if (trace != null) trace.data(10, name);

    ProgMonitorPool.removeProgMonitor(this);
    Stats.stopGlobe(this);

    synchronized (monitor) {
      setVisible(false);
      MiscGui.removeAllComponentsAndListeners(this);
      for (int i=0; i<3; i++) {
        try {
          dispose();
          break;
        } catch (Throwable e) {
          try {
            dispose();
          } catch (Throwable t) {
          }
        }
      }
    }

    if (trace != null) trace.exit(WipeProgMonitor.class);
  }

  private LinkedList historyTotalBytes;
  private LinkedList historyUpdateDates;

  private void resetStats(long newSize) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "resetStats(long newSize)");
    if (trace != null) trace.args(newSize);
    if (trace != null) trace.data(10, name);

    historyTotalBytes = null;
    historyUpdateDates = null;

    totalBytes = 0;
    totalTransferSize = newSize;
    transferStartDateMillis = System.currentTimeMillis();

    if (trace != null) trace.exit(WipeProgMonitor.class);
  }

  /**
   * only last 'numOfStats' are kept to make current calculations.
   */
  private void updateStats(long currentDateMillis, int numOfStats) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "updateStats(long currentDateMillis, int numOfStats)");
    if (trace != null) trace.args(currentDateMillis);
    if (trace != null) trace.args(numOfStats);
    if (trace != null) trace.data(10, name);

    // update history stats
    if (historyTotalBytes == null) {
      historyTotalBytes = new LinkedList();
      historyUpdateDates = new LinkedList();
    }

    if (historyTotalBytes.size() >= numOfStats) {
      historyTotalBytes.removeFirst();
      historyUpdateDates.removeFirst();
    }

    historyTotalBytes.add(new Long(totalBytes));
    historyUpdateDates.add(new Long(currentDateMillis));

    lastTransferUpdateDateMillis = currentDateMillis;

    // calculate current transfer rate from history values
    long historyTimeStartMillis = ((Long) historyUpdateDates.getFirst()).longValue();
    long historyTimeEndMillis = ((Long) historyUpdateDates.getLast()).longValue();

    // in (ms)
    long historyTimeElapsed = historyTimeEndMillis - historyTimeStartMillis;

    long historyBytesStart = ((Long) historyTotalBytes.getFirst()).longValue();
    long historyBytesEnd = ((Long) historyTotalBytes.getLast()).longValue();

    long historyBytesElapsed = historyBytesEnd - historyBytesStart;

    long currentTransferRate = 0;
    if (historyTimeElapsed > 0)
      currentTransferRate = (long) (historyBytesElapsed / (historyTimeElapsed / 1000.0));

    jNotes[3].setText(Misc.getFormattedSize(currentTransferRate, 4, 3) + "/sec (" +
                      Misc.getFormattedSize(totalBytes, 4, 3) + " of " + Misc.getFormattedSize(totalTransferSize, 4, 3) + ")");


    // now, update estimated time with respect to passed time and current transfer rate
    long bytesToGo = totalTransferSize - totalBytes;
    bytesToGo = bytesToGo >= 0 ? bytesToGo : 0; // never negative

    // in (seconds)
    long totalTimeElapsed = (long) ((currentDateMillis - transferStartDateMillis) / 1000.0);
    totalTimeElapsed = totalTimeElapsed >= 0 ? totalTimeElapsed : 0; // never negative

    // in (seconds)
    long timeToGo = 0;
    if (currentTransferRate > 0)
      timeToGo = (long) ( ((double) bytesToGo) / ((double) currentTransferRate) );

    // in (seconds)
    long totalTime = (long) (totalTimeElapsed + timeToGo);

    String estimate = Misc.getFormattedTime(timeToGo) + " [" + Misc.getFormattedTime(totalTimeElapsed) + " of " + Misc.getFormattedTime(totalTime) + ")";
    jNotes[0].setText(estimate);

    if (trace != null) trace.exit(WipeProgMonitor.class);
  }


  /*================================================*/
  /*   P r o g M o n i t o r    Interface Methods   */
  /*================================================*/


  private long totalBytes;
  private long totalTransferSize;
  private long transferStartDateMillis;
  private long lastTransferUpdateDateMillis;
  private int currentTask = -1;
  private int progressValue;
  private boolean allDone;
  private boolean jobKilled;

  public void enqueue(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "enqueue(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }
  public void dequeue(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "dequeue(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }
  public void startSend(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "startSend(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    Stats.moveGlobe(this);
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }
  public void startSendAction(String actionName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "startSendAction(String actionName)");
    if (trace != null) trace.args(actionName);
    if (trace != null) trace.data(10, name);
    Stats.setStatus("Sending request ... [" + actionName + "]");
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }
  public void startSendData(String dataName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "startSendData(String dataName)");
    if (trace != null) trace.args(dataName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }
  public void doneSend(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "doneSend(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    Stats.setStatus("Waiting for reply ... [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }
  public void doneSendAction(String actionName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "doneSendAction(String actionName)");
    if (trace != null) trace.args(actionName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }
  public void doneSendData(String dataName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "doneSendData(String dataName)");
    if (trace != null) trace.args(dataName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }
  public void startReceive(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "startReceive(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    Stats.setStatus("Receiving reply ... [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }
  public void startReceiveAction(String actionName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "startReceiveAction(String actionName)");
    if (trace != null) trace.args(actionName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }
  public void startReceiveData(String dataName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "startReceiveData(String dataName)");
    if (trace != null) trace.args(dataName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }
  public void doneReceive(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "doneReceive(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    Stats.setStatus("Reply received. [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }
  public void doneReceiveAction(String actionName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "doneReceiveAction(String actionName)");
    if (trace != null) trace.args(actionName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }
  public void doneReceiveData(String dataName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "doneReceiveData(String dataName)");
    if (trace != null) trace.args(dataName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }
  public void startExecution(int actionCode) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "startExecution(int actionCode)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.data(10, name);
    Stats.setStatus("Executing reply ... [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }
  public void doneExecution(int actionCode) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "doneExecution(int actionCode)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.data(10, name);
    Stats.setStatus("Action completed. [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    Stats.stopGlobe(this);
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }
  public void appendLine(String str) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "appendLine(String str)");
    if (trace != null) trace.args(str);
    if (trace != null) trace.data(10, name);
    String text = jTextArea.getText();
    if (text.length() > 0) {
      jTextArea.setText(text + "\n" + str);
      jTextArea.setCaretPosition(text.length()+2);
    }
    else {
      jTextArea.setText(str);
      jTextArea.setCaretPosition(0);
    }
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }
  public void setCurrentStatus(String currentStatus) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "setCurrentStatus(String currentStatus)");
    if (trace != null) trace.args(currentStatus);
    if (trace != null) trace.data(10, name);
    jStatus.setText(currentStatus);
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }
  public void setFileNameSource(String fileName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "setFileNameSource(String fileName)");
    if (trace != null) trace.args(fileName);
    if (trace != null) trace.data(10, name);
    jNotes[1].setText(fileName);
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }
  public void setFileNameDestination(String fileName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "setFileNameDestination(String fileName)");
    if (trace != null) trace.args(fileName);
    if (trace != null) trace.data(10, name);
    jNotes[2].setText(fileName);
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }
  public void setTransferSize(long size) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "setTransferSize(long size)");
    if (trace != null) trace.args(size);
    if (trace != null) trace.data(10, name);
    jProgressBar.setMinimum(0);
    jProgressBar.setMaximum((int) (size/100));

    resetStats(size);
    updateStats(System.currentTimeMillis(), 5);
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }
  public void addBytes(long bytes) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "addBytes(long bytes)");
    if (trace != null) trace.args(bytes);
    if (trace != null) trace.data(10, name);
    boolean updateNow = totalBytes == 0;

    totalBytes += bytes;
    jProgressBar.setValue((int) (totalBytes/100.0));

    // update transfer rate and estimated time every 1 second at the most!
    long currentDateMillis = System.currentTimeMillis();
    if (updateNow || totalBytes == totalTransferSize || (currentDateMillis - lastTransferUpdateDateMillis) > 1000) {
      updateStats(currentDateMillis, 5);
    }
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }
  public void doneTransfer() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "doneTransfer");
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(WipeProgMonitor.class);
   }
  public void nextTask(String title) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "nextTask");
    if (trace != null) trace.args(title);
    if (trace != null) trace.data(10, name);
    currentTask ++;
    setTitle(title);
    if (trace != null) trace.exit(WipeProgMonitor.class);
  } // end nextTask()
  public void nextTask() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "nextTask");
    if (trace != null) trace.data(10, name);
    nextTask("");
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }

  public void allDone() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "allDone");
    if (trace != null) trace.data(10, name);
    allDone = true;
    setTitle("Done Wiping");
    setCurrentStatus("Done");
    jCancelButton.setText("OK");

    jImageLabel.setIcon(Images.get(ImageNums.LOCK_OPENED));

    if (jCloseOnDone.isSelected()) {
      closeProgMonitor();
    }
    Stats.stopGlobe(this);

    Sounds.playAsynchronous(Sounds.TRANSFER_DONE);
    if (trace != null) trace.exit(WipeProgMonitor.class);
  } // end allDone()

  public void jobKilled() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "jobKilled()");
    if (trace != null) trace.data(10, name);
    setCurrentStatus("Job Failed!");
    jobKilled = true;
    Stats.stopGlobe(this);
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }

  public void jobForRetry() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "jobForRetry()");
    if (trace != null) trace.data(10, name);
    setCurrentStatus("Job Queued for Retry.");
    Stats.stopGlobe(this);
    closeProgMonitor();
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }


  public void setInterrupt(Interruptible interruptible) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(WipeProgMonitor.class, "setInterrupt(Interruptible interruptible)");
    if (trace != null) trace.args(interrupt);
    if (trace != null) trace.data(10, name);
    this.interrupt = interruptible;
    if (cancelled) {
      closeProgMonitor();
      interruptible.interrupt();
    }
    if (trace != null) trace.exit(WipeProgMonitor.class);
  }

  public boolean isAllDone() {
    return allDone;
  }

  public boolean isCancelled() {
    return cancelled;
  }

  public void setCancellable(Cancellable cancellable) {
  }

  public boolean isJobKilled() {
    return jobKilled;
  }

}