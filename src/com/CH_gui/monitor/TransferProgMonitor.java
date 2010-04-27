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

import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.LinkedList;

import com.CH_cl.service.cache.*;

import com.CH_co.gui.*;
import com.CH_co.monitor.*;
import com.CH_co.service.msg.MessageActionNameSwitch;
import com.CH_co.service.records.*;
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
 * <b>$Revision: 1.20 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public final class TransferProgMonitor extends JFrame implements ProgMonitorTransferI {

  private String title;

  private JLabel jImageLabel;

  private JCheckBox[] jCheckTasks;
  private JLabel jStatus;
  private JProgressBar jProgressBar;
  private JLabel[] jNoteHeadings;
  private JLabel[] jNotes;
  private JCheckBox jCloseOnDone;

  private JButton jOpenButton;
  private JButton jOpenFolderButton;
  private JButton jCancelButton;

  private boolean cancelled;
  private Interruptible interrupt;
  private Cancellable cancellable;

  private int monitoringType;
  private boolean suppressTransferSoundsAndAutoClose;

  private static final int MONITORING_DOWNLOAD = 2;
  private static final int MONITORING_UPLOAD = 3;
  private static final int MONITORING_OPEN = 4;

  private final Object monitor = new Object();
  private boolean isClosed = false;


  private String name;

  private final static Object counterMonitor = new Object();
  private static int counter = 0;

  private File destDir;
  private FileLinkRecord[] fileLinks;

  /** Creates new TransferProgMonitor */
  public TransferProgMonitor() {
  }

  /** Initialized new TransferProgMonitor */
  private void init(String title, String[] tasks, String[] noteHeadings, String[] notes,
               int initProgBarMin, int initProgBarMax, int monitoringType, boolean suppressTransferSoundsAndAutoClose)
  {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "init(String title, String[] tasks, String[] noteHeadings, String[] notes, int initProgBarMin, int initProgBarMax, int monitoringType, boolean suppressTransferSoundsAndAutoClose)");
    if (trace != null) trace.args(title, tasks, noteHeadings, notes);
    if (trace != null) trace.args(initProgBarMin);
    if (trace != null) trace.args(initProgBarMax);
    if (trace != null) trace.args(monitoringType);
    if (trace != null) trace.args(suppressTransferSoundsAndAutoClose);

    setTitle(title);

    synchronized (counterMonitor) {
      name = TransferProgMonitor.class.getName() + " #" + counter;
      if (trace != null) trace.data(10, "creating... ", name);
      counter ++;
      if (counter == Integer.MAX_VALUE)
        counter = 0;
    }


    this.title = title;
    this.monitoringType = monitoringType;
    this.suppressTransferSoundsAndAutoClose = suppressTransferSoundsAndAutoClose;

    initPanelComponents(tasks, noteHeadings, notes, initProgBarMin, initProgBarMax);

    getRootPane().setDefaultButton(jCancelButton);
    getContentPane().add("Center", createMainPanel());

    if (monitoringType == MONITORING_DOWNLOAD || monitoringType == MONITORING_UPLOAD || monitoringType == MONITORING_OPEN) {
      getContentPane().add("South", MiscGui.createButtonPanel(new JButton[] { jOpenButton, jOpenFolderButton, jCancelButton }));
    }
    ImageIcon frameIcon = Images.get(ImageNums.FRAME_LOCK32);
    if (frameIcon != null) {
      setIconImage(frameIcon.getImage());
    }

    pack();
    // make it a bit wider
    Dimension prefDim = getPreferredSize();
    setSize(prefDim.width + 260, prefDim.height + 20);
    setLocation(MiscGui.getSuggestedSpreadedWindowLocation(this));
    
    // delay visibility of this frame in case it will close super fast
    Thread th = new ThreadTraced("Delayed Visibility of TransferProgMonitor") {
      public void runTraced() {
        try { Thread.sleep(2000); } catch (InterruptedException x) { }
        synchronized (monitor) {
          if (!isClosed) {
            setVisible(true);
          }
        }
      }
    };
    th.setDaemon(true);
    th.start();

    if (trace != null) trace.exit(TransferProgMonitor.class);
  }


  /** 
   * File Download/Open
   */
  public void init(String[] tasks, File destDir, FileLinkRecord[] fileLinks, boolean isDownload, boolean suppressTransferSoundsAndAutoClose) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "init(String[] tasks, File destDir, FileLinkRecord[] fileLinks, boolean isDownload, boolean suppressTransferSoundsAndAutoClose))");
    if (trace != null) trace.args(tasks, destDir, fileLinks);
    if (trace != null) trace.args(isDownload);
    if (trace != null) trace.args(suppressTransferSoundsAndAutoClose);
    init(isDownload ? "File Download" : "File Open", tasks,
              new String[] { "Estimated Time:", "From:", "To:", "Transfer Rate:" }, 
              new String[] { " ... ", " ... ", " ... ", " ... " },
              0, 100, isDownload ? MONITORING_DOWNLOAD : MONITORING_OPEN, suppressTransferSoundsAndAutoClose
              );
    setDestinationDir(destDir);
    setFiles(fileLinks);
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }

  /** 
   * File Upload
   */
  public void init(String[] tasks) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "init(String[] tasks)");
    if (trace != null) trace.args(tasks);
    init("File Upload", tasks,
              new String[] { "Estimated Time:", "From:", "To:", "Transfer Rate:" }, 
              new String[] { " ... ", " ... ", " ... ", " ... " },
              0, 100, MONITORING_UPLOAD, false
              );
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }


  public void setDestinationDir(File destDir) {
    this.destDir = destDir;
    jOpenFolderButton.setEnabled(destDir != null);
  }


  public void setFiles(FileLinkRecord[] fileLinks) {
    this.fileLinks = fileLinks;
  }


  private void initPanelComponents(String[] tasks, String[] noteHeadings, String[] notes, int initProgBarMin, int initProgBarMax) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "initPanelComponents(String[] tasks, String[] noteHeadings, String[] notes, int initProgBarMin, int initProgBarMax)");
    if (trace != null) trace.args(tasks, noteHeadings, notes);
    if (trace != null) trace.args(initProgBarMin);
    if (trace != null) trace.args(initProgBarMin);
    if (trace != null) trace.args(initProgBarMax);

    if (monitoringType == MONITORING_UPLOAD)
      jImageLabel = new JMyLabel(Images.get(ImageNums.ANIM_LOCK));
    else if (monitoringType == MONITORING_DOWNLOAD || monitoringType == MONITORING_OPEN)
      jImageLabel = new JMyLabel(Images.get(ImageNums.ANIM_TRANSFER));

    jImageLabel.setPreferredSize(new Dimension(110, 60));

    jCheckTasks = new JCheckBox[tasks.length];
    for (int i=0; i < tasks.length; i++) {
      jCheckTasks[i] = new JMyCheckBox(tasks[i]);
      MiscGui.setPlainFont(jCheckTasks[i]);
      jCheckTasks[i].setEnabled(false);
      jCheckTasks[i].setSelected(false);
    }

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

    if (monitoringType != MONITORING_OPEN && !suppressTransferSoundsAndAutoClose) {
      if (monitoringType == MONITORING_UPLOAD) {
        jCloseOnDone = new JMyCheckBox("Close this dialog when all Upload processes finish.");
        jCloseOnDone.setSelected(Boolean.valueOf(GlobalProperties.getProperty("ProgMonitor.TransferProgMonitor.closeOnDone.upload", "true")).booleanValue());
      } else {
        jCloseOnDone = new JMyCheckBox("Close this dialog when all Download processes finish.");
        jCloseOnDone.setSelected(Boolean.valueOf(GlobalProperties.getProperty("ProgMonitor.TransferProgMonitor.closeOnDone.download", "true")).booleanValue());
      }
      jCloseOnDone.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "actionPerformed(ActionEvent event)");
          if (trace != null) trace.args(event);
          if (trace != null) trace.data(10, name);
          if (monitoringType == MONITORING_UPLOAD)
            GlobalProperties.setProperty("ProgMonitor.TransferProgMonitor.closeOnDone.upload", "" + TransferProgMonitor.this.jCloseOnDone.isSelected());
          else
            GlobalProperties.setProperty("ProgMonitor.TransferProgMonitor.closeOnDone.download", "" + TransferProgMonitor.this.jCloseOnDone.isSelected());
          if (trace != null) trace.exit(getClass());
        }
      });
    }

    if (monitoringType == MONITORING_DOWNLOAD) {
      jOpenButton = new JMyButton("Open");
      jOpenButton.setEnabled(false);
      jOpenButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "actionPerformed(ActionEvent event)");
          if (trace != null) trace.args(event);
          if (trace != null) trace.data(10, name);
          closeProgMonitor();
          if (fileLinks != null) {
            for (int i=0; i<fileLinks.length; i++) {
              FileDataRecord fileData = FetchedDataCache.getSingleInstance().getFileDataRecord(fileLinks[i].fileId);
              FileLauncher.openFile(fileData);
            }
          }
          if (trace != null) trace.exit(getClass());
        }
      });
    }
    if (monitoringType == MONITORING_DOWNLOAD || monitoringType == MONITORING_OPEN) {
      jOpenFolderButton = new JMyButton("Open Folder");
      jOpenFolderButton.setEnabled(destDir != null);
      jOpenFolderButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "actionPerformed(ActionEvent event)");
          if (trace != null) trace.args(event);
          if (trace != null) trace.data(10, name);
          if (isAllDone())
            closeProgMonitor();
          try {
            BrowserLauncher.openFile(destDir);
          } catch (IOException e) {
            if (trace != null) trace.exception(getClass(), 100, e);
          }
          if (trace != null) trace.exit(getClass());
        }
      });
    }
    jCancelButton = new JMyButton("Cancel");
    jCancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "actionPerformed(ActionEvent event)");
        if (trace != null) trace.args(event);
        if (trace != null) trace.data(10, name);

        cancelled = true;

        Cancellable ourCancellable = cancellable;
        if (ourCancellable != null)
          ourCancellable.setCancelled(true);

        Interruptible ourInterrupt = interrupt;
        if (allDone || jobKilled) {
          closeProgMonitor();
        } else if (ourInterrupt != null) {
          closeProgMonitor();
          ourInterrupt.interrupt();
        }
        if (trace != null) trace.exit(getClass());
      }
    });

    if (trace != null) trace.exit(TransferProgMonitor.class);
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

    /*
    panel.add(createCheckPane(), new GridBagConstraints(gridX, index, gridWidth, 1, 10, 10, 
        GridBagConstraints.WEST, GridBagConstraints.BOTH, insetNote, 0, 0));
    */

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

    if (jCloseOnDone != null) {
      panel.add(jCloseOnDone, new GridBagConstraints(0, index, 2, 1, 10, 0, 
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insetFive, 0, 0));
      index ++;
    }

    // filler
    panel.add(new JMyLabel(), new GridBagConstraints(0, index, 2, 1, 10, 10, 
      GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0,0,0,0), 0, 0));

    return panel;
  }

  public void closeProgMonitor() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "closeProgMonitor()");
    if (trace != null) trace.data(10, name);
    ProgMonitorPool.removeProgMonitor(this);
    Stats.stopGlobe(this);

    synchronized (monitor) {
      isClosed = true;
      setVisible(false);
      MiscGui.removeAllComponentsAndListeners(this);
      for (int i=0; i<3; i++) {
        try {
          dispose();
          break;
        } catch (Throwable e) {
        }
      }
    }

    if (trace != null) trace.exit(TransferProgMonitor.class);
  }

  public boolean isCancelled() {
    return cancelled;
  }

  private LinkedList historyTotalBytes;
  private LinkedList historyUpdateDates;

  private void resetStats(long newSize) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "resetStats(long newSize)");
    if (trace != null) trace.args(newSize);
    if (trace != null) trace.data(10, name);
    historyTotalBytes = null;
    historyUpdateDates = null;

    totalBytes = 0;
    totalTransferSize = newSize;
    transferStartDateMillis = System.currentTimeMillis();
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }

  /**
   * only last 'numOfStats' are kept to make current calculations.
   */
  private void updateStats(long currentDateMillis, int numOfStats) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "updateStats(long currentDateMillis, int numOfStats)");
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

    String estimate = Misc.getFormattedTime(timeToGo) + " (" + Misc.getFormattedTime(totalTimeElapsed) + " of " + Misc.getFormattedTime(totalTime) + ")";
    jNotes[0].setText(estimate);

    if (trace != null) trace.exit(TransferProgMonitor.class);
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
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "enqueue(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    Stats.moveGlobe(this);
    Stats.setStatus("New transfer request... [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }
  public void dequeue(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "dequeue(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    Stats.setStatus("Preparing to transfer ... [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }
  public void startSend(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "startSend(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    //Stats.moveGlobe(this); // moving globe in enqueue()
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }
  public void startSendAction(String actionName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "startSendAction(String actionName)");
    if (trace != null) trace.args(actionName);
    if (trace != null) trace.data(10, name);
    Stats.setStatus("Sending request ... [" + actionName + "]");
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }
  public void startSendData(String dataName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "startSendData(String dataName)");
    if (trace != null) trace.args(dataName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }
  public void doneSend(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "doneSend(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    Stats.setStatus("Waiting for reply ... [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }
  public void doneSendAction(String actionName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "doneSendAction(String actionName)");
    if (trace != null) trace.args(actionName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }
  public void doneSendData(String dataName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "doneSendData(String dataName)");
    if (trace != null) trace.args(dataName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }
  public void startReceive(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "startReceive(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    Stats.setStatus("Receiving reply ... [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }
  public void startReceiveAction(String actionName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "startReceiveAction(String actionName)");
    if (trace != null) trace.args(actionName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }
  public void startReceiveData(String dataName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "startReceiveData(String dataName)");
    if (trace != null) trace.args(dataName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }
  public void doneReceive(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "doneReceive(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    Stats.setStatus("Reply received. [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }
  public void doneReceiveAction(String actionName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "doneReceiveAction(String actionName)");
    if (trace != null) trace.args(actionName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }
  public void doneReceiveData(String dataName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "doneReceiveData(String dataName)");
    if (trace != null) trace.args(dataName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }
  public void startExecution(int actionCode) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "startExecution(int actionCode)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.data(10, name);
    Stats.setStatus("Executing reply ... [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }
  public void doneExecution(int actionCode) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "doneExecution(int actionCode)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.data(10, name);
    Stats.setStatus("Action completed. [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    Stats.stopGlobe(this);
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }
  public void setCurrentStatus(String currentStatus) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "setCurrentStatus(String currentStatus)");
    if (trace != null) trace.args(currentStatus);
    if (trace != null) trace.data(10, name);
    jStatus.setText(currentStatus);
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }
  public void setFileNameSource(String fileName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "setFileNameSource(String fileName)");
    if (trace != null) trace.args(fileName);
    if (trace != null) trace.data(10, name);
    jNotes[1].setText(fileName);
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }
  public void setFileNameDestination(String fileName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "setFileNameDestination(String fileName)");
    if (trace != null) trace.args(fileName);
    if (trace != null) trace.data(10, name);
    jNotes[2].setText(fileName);
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }
  public void setTransferSize(long size) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "setTransferSize(long size)");
    if (trace != null) trace.args(size);
    if (trace != null) trace.data(10, name);
    jProgressBar.setMinimum(0);
    jProgressBar.setMaximum((int) (size/100));

    resetStats(size);
    updateStats(System.currentTimeMillis(), 5);
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }
  public void addBytes(long bytes) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "addBytes(long bytes)");
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
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }
  public void doneTransfer() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "doneTransfer");
    if (trace != null) trace.data(10, name);
    if (currentTask >= 0) {
      MiscGui.setPlainFont(jCheckTasks[currentTask]);
      jCheckTasks[currentTask].setSelected(true);
    }
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }
  public void nextTask() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "nextTask");
    if (trace != null) trace.data(10, name);
    currentTask ++;
    if (currentTask < jCheckTasks.length) {
      MiscGui.setBoldFont(jCheckTasks[currentTask]);
      setTitle(jCheckTasks[currentTask].getText() + " " + title);
    }
    else {
      if (monitoringType == MONITORING_UPLOAD)
        jImageLabel.setIcon(Images.get(ImageNums.ANIM_TRANSFER));
      else if (monitoringType == MONITORING_DOWNLOAD || monitoringType == MONITORING_OPEN) 
        jImageLabel.setIcon(Images.get(ImageNums.ANIM_LOCK));

      currentTask = -1;
      nextTask();
    }
    if (trace != null) trace.exit(TransferProgMonitor.class);
  } // end nextTask()

  public void allDone() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "allDone");
    if (trace != null) trace.data(10, name);
    allDone = true;
    Stats.stopGlobe(this);
    if (!suppressTransferSoundsAndAutoClose) Sounds.playAsynchronous(Sounds.TRANSFER_DONE);
    setTitle("Done " + title);
    setCurrentStatus("Done");
    jCancelButton.setText("OK");
    if (jOpenButton != null) {
      jOpenButton.setEnabled(fileLinks != null && fileLinks.length == 1);
      jOpenFolderButton.setEnabled(true);
    }
    if (monitoringType == MONITORING_UPLOAD) {
      jImageLabel.setIcon(Images.get(ImageNums.ANIM_TRANSFER_STOP));
    } else if (monitoringType == MONITORING_DOWNLOAD || monitoringType == MONITORING_OPEN) {
      jImageLabel.setIcon(Images.get(ImageNums.LOCK_OPENED));
    }
    if (monitoringType == MONITORING_OPEN || (jCloseOnDone != null && jCloseOnDone.isSelected()) || suppressTransferSoundsAndAutoClose) {
      closeProgMonitor();
    }
    if (monitoringType == MONITORING_OPEN) {
      if (fileLinks != null) {
        for (int i=0; i<fileLinks.length; i++) {
          FileDataRecord fileData = FetchedDataCache.getSingleInstance().getFileDataRecord(fileLinks[i].fileId);
          if (fileData != null) {
            File tempFile = fileData.getPlainDataFile();
            if (tempFile != null) {
              tempFile.setReadOnly();
              GlobalProperties.addTempFileToCleanup(tempFile);
            }
          }
          FileLauncher.openFile(fileData);
        }
      }
    }
    if (trace != null) trace.exit(TransferProgMonitor.class);
  } // end allDone()

  public void jobKilled() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "jobKilled()");
    if (trace != null) trace.data(10, name);
    setCurrentStatus("Job Failed!");
    jobKilled = true;
    Stats.stopGlobe(this);
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }

  public void jobForRetry() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "jobForRetry()");
    if (trace != null) trace.data(10, name);
    setCurrentStatus("Job Queued for Retry.");
    Stats.stopGlobe(this);
    closeProgMonitor();
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }

  public void setInterrupt(Interruptible interruptible) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "setInterrupt(Interruptible interruptible)");
    if (trace != null) trace.args(interrupt);
    if (trace != null) trace.data(10, name);
    this.interrupt = interruptible;
    if (cancelled) {
      closeProgMonitor();
      interruptible.interrupt();
    }
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }

  public boolean isAllDone() {
    return allDone;
  }
  public boolean isJobKilled() {
    return jobKilled;
  }

  public void setCancellable(Cancellable cancellable) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitor.class, "setCancellable(Cancellable cancellable)");
    if (trace != null) trace.args(cancellable);
    if (trace != null) trace.data(10, name);
    this.cancellable = cancellable;
    if (cancelled) {
      closeProgMonitor();
      cancellable.setCancelled(true);
    }
    if (trace != null) trace.exit(TransferProgMonitor.class);
  }

  public void nextTask(String task) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void appendLine(String info) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}