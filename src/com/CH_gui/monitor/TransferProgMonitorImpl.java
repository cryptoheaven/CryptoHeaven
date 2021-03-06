/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.monitor;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.ops.DownloadUtilities;
import com.CH_cl.service.ops.FileLobUpEditMonitor;
import com.CH_co.monitor.*;
import com.CH_co.service.records.FileDataRecord;
import com.CH_co.service.records.FileLinkRecord;
import com.CH_co.trace.ThreadTraced;
import com.CH_co.trace.Trace;
import com.CH_co.util.GlobalProperties;
import com.CH_co.util.ImageNums;
import com.CH_co.util.Misc;
import com.CH_co.util.Sounds;
import com.CH_gui.gui.JMyButton;
import com.CH_gui.gui.JMyCheckBox;
import com.CH_gui.gui.JMyLabel;
import com.CH_gui.gui.MyInsets;
import com.CH_gui.util.BrowserLauncher;
import com.CH_gui.util.Images;
import com.CH_gui.util.MiscGui;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import javax.swing.*;

/** 
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.20 $</b>
*
* @author  Marcin Kurzawa
*/
public final class TransferProgMonitorImpl extends JFrame implements ProgMonitorTransferI {

  private static boolean ENABLE_NOTE_FROM = false;
  private static boolean ENABLE_NOTE_TO = false;

  private static int NOTE_FROM_INDEX = 1;
  private static int NOTE_TO_INDEX = 2;

  private Object context;
  private String title;

  private JLabel jImageLabel;

  private Object[] tasks;
  private JCheckBox[] jCheckTasks;
  private JLabel jStatus1;
  private JLabel jStatus2;
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

  /** Creates new TransferProgMonitorImpl */
  public TransferProgMonitorImpl() {
  }

  /** Initialized new TransferProgMonitorImpl */
  private void init(Object context, String title, Object[] tasks, String[] noteHeadings, String[] notes,
              int initProgBarMin, int initProgBarMax, int monitoringType, boolean suppressTransferSoundsAndAutoClose)
  {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "init(Object context, String title, String[] tasks, String[] noteHeadings, String[] notes, int initProgBarMin, int initProgBarMax, int monitoringType, boolean suppressTransferSoundsAndAutoClose)");
    if (trace != null) trace.args(title, tasks, noteHeadings, notes);
    if (trace != null) trace.args(initProgBarMin);
    if (trace != null) trace.args(initProgBarMax);
    if (trace != null) trace.args(monitoringType);
    if (trace != null) trace.args(suppressTransferSoundsAndAutoClose);

    this.context = context;
    setTitle(title);

    synchronized (counterMonitor) {
      name = TransferProgMonitorImpl.class.getName() + " #" + counter;
      if (trace != null) trace.data(10, "creating... ", name);
      counter ++;
      if (counter == Integer.MAX_VALUE)
        counter = 0;
    }

    this.title = title;
    this.tasks = tasks;
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
      try {
        setIconImage(frameIcon.getImage());
      } catch (NoSuchMethodError e) {
        // API since 1.6!!! - ignore it as it is not crytical
      }
    }

    pack();
    // make it a bit wider
    Dimension prefDim = getPreferredSize();
    setSize(prefDim.width + 150, prefDim.height + 20);

    boolean isLocationSet = false;
    try { // try-catch in case context is invalid
      if (context instanceof Component) {
        MiscGui.setSuggestedWindowLocation((Component) context, this);
        isLocationSet = true;
      }
    } catch (Throwable t) {
    }
    if (!isLocationSet) {
      setLocation(MiscGui.getSuggestedSpreadedWindowLocation(this));
    }

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

    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }


  /**
  * File Download/Open
  * Context is usually the caller GUI
  */
  public void init(Object context, String[] tasks, File destDir, FileLinkRecord[] fileLinks, boolean isDownload, boolean suppressTransferSoundsAndAutoClose) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "init(String[] tasks, File destDir, FileLinkRecord[] fileLinks, boolean isDownload, boolean suppressTransferSoundsAndAutoClose))");
    if (trace != null) trace.args(tasks, destDir, fileLinks);
    if (trace != null) trace.args(isDownload);
    if (trace != null) trace.args(suppressTransferSoundsAndAutoClose);
    init(context, isDownload ? "File Download" : "File Open", tasks,
              new String[] { "Estimated Time:", "From:", "To:", "Transfer Rate:" },
              new String[] { " ... ", " ... ", " ... ", " ... " },
              0, 100, isDownload ? MONITORING_DOWNLOAD : MONITORING_OPEN, suppressTransferSoundsAndAutoClose
              );
    setDestinationDir(destDir);
    setFiles(fileLinks);
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }

  /**
  * File Upload
  */
  public void init(Object context, File[] tasks) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "init(String[] tasks)");
    if (trace != null) trace.args(tasks);
    init(context, "File Upload", tasks,
              new String[] { "Estimated Time:", "From:", "To:", "Transfer Rate:" },
              new String[] { " ... ", " ... ", " ... ", " ... " },
              0, 100, MONITORING_UPLOAD, false
              );
    if (tasks != null && tasks.length > 0) {
      jStatus2.setText(tasks[0].getAbsolutePath());
      jStatus2.setVisible(true);
    }
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }

  private void setDestinationDir(File destDir) {
    this.destDir = destDir;
    jOpenFolderButton.setEnabled(destDir != null);
    if (destDir != null && !destDir.equals(DownloadUtilities.getDefaultTempDir())) {
      jStatus2.setText(destDir.getAbsolutePath());
      jStatus2.setVisible(true);
    }
  }

  private void setFiles(FileLinkRecord[] fileLinks) {
    this.fileLinks = fileLinks;
  }


  private void initPanelComponents(Object[] tasks, String[] noteHeadings, String[] notes, int initProgBarMin, int initProgBarMax) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "initPanelComponents(String[] tasks, String[] noteHeadings, String[] notes, int initProgBarMin, int initProgBarMax)");
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
      String task = tasks[i] instanceof File ? ((File) tasks[i]).getName() : tasks[i].toString();
      jCheckTasks[i] = new JMyCheckBox(task);
      MiscGui.setPlainFont(jCheckTasks[i]);
      jCheckTasks[i].setEnabled(false);
      jCheckTasks[i].setSelected(false);
    }

    jStatus1 = new JMyLabel("Status:");
    jStatus2 = new JMyLabel("");
    jStatus2.setVisible(false);

    jProgressBar = new JProgressBar();
    jProgressBar.setMinimum(initProgBarMin);
    jProgressBar.setMaximum(initProgBarMax);

    jNoteHeadings = new JLabel[noteHeadings.length];
    for (int i=0; i<noteHeadings.length; i++) {
      jNoteHeadings[i] = new JMyLabel(noteHeadings[i]);
    }
    jNoteHeadings[NOTE_FROM_INDEX].setVisible(ENABLE_NOTE_FROM);
    jNoteHeadings[NOTE_TO_INDEX].setVisible(ENABLE_NOTE_TO);

    jNotes = new JLabel[notes.length];
    for (int i=0; i<notes.length; i++) {
      jNotes[i] = new JMyLabel(notes[i]);
    }
    jNotes[NOTE_FROM_INDEX].setVisible(ENABLE_NOTE_FROM);
    jNotes[NOTE_TO_INDEX].setVisible(ENABLE_NOTE_TO);

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
            GlobalProperties.setProperty("ProgMonitor.TransferProgMonitor.closeOnDone.upload", "" + TransferProgMonitorImpl.this.jCloseOnDone.isSelected());
          else
            GlobalProperties.setProperty("ProgMonitor.TransferProgMonitor.closeOnDone.download", "" + TransferProgMonitorImpl.this.jCloseOnDone.isSelected());
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
              if (fileData != null)
                BrowserLauncher.openFile(context, fileData.getPlainDataFile());
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
        interruptAndCancel();
        if (trace != null) trace.exit(getClass());
      }
    });

    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }

  private JPanel createMainPanel() {

    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

    Insets insetFive = new MyInsets(5, 5, 5, 5);
    Insets insetNote = new MyInsets(2, 5, 2, 5);

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
    panel.add(jStatus1, new GridBagConstraints(0, index, 2, 1, 10, 0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insetNote, 0, 0));
    index ++;

    panel.add(jStatus2, new GridBagConstraints(0, index, 2, 1, 10, 0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insetNote, 0, 0));
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

  private void closeProgMonitor() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "closeProgMonitor()");
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

    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }

  private LinkedList historyTotalBytes;
  private LinkedList historyUpdateDates;

  private void resetStats(long newSize) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "resetStats(long newSize)");
    if (trace != null) trace.args(newSize);
    if (trace != null) trace.data(10, name);
    historyTotalBytes = null;
    historyUpdateDates = null;

    totalBytes = 0;
    totalTransferSize = newSize;
    transferStartDateMillis = System.currentTimeMillis();
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }

  /**
  * only last 'numOfStats' are kept to make current calculations.
  */
  private void updateStats(long currentDateMillis, int numOfStats) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "updateStats(long currentDateMillis, int numOfStats)");
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

    String transferRate = "";
    if (totalTransferSize == -1) {
      transferRate = Misc.getFormattedSize(currentTransferRate, 4, 3) + "/sec (" +
                    Misc.getFormattedSize(totalBytes, 4, 3) + " transferred so far.)";
    } else {
      transferRate = Misc.getFormattedSize(currentTransferRate, 4, 3) + "/sec (" +
                    Misc.getFormattedSize(totalBytes, 4, 3) + " of " + Misc.getFormattedSize(totalTransferSize, 4, 3) + ")";
    }
    jNotes[3].setText(transferRate);

    // in (seconds)
    long totalTimeElapsed = (long) ((currentDateMillis - transferStartDateMillis) / 1000.0);
    totalTimeElapsed = totalTimeElapsed >= 0 ? totalTimeElapsed : 0; // never negative

    String estimate = "";
    if (totalTransferSize == -1)
      estimate = "Cannot estimate, " + Misc.getFormattedTime(totalTimeElapsed) + " elapsed so far.";
    else {
      // now, update estimated time with respect to passed time and current transfer rate
      long bytesToGo = totalTransferSize - totalBytes;
      bytesToGo = bytesToGo >= 0 ? bytesToGo : 0; // never negative

      // in (seconds)
      long timeToGo = 0;
      if (currentTransferRate > 0)
        timeToGo = (long) ( ((double) bytesToGo) / ((double) currentTransferRate) );

      // in (seconds)
      long totalTime = (long) (totalTimeElapsed + timeToGo);
      estimate = Misc.getFormattedTime(timeToGo) + " (" + Misc.getFormattedTime(totalTimeElapsed) + " of " + Misc.getFormattedTime(totalTime) + ")";
    }
    jNotes[0].setText(estimate);

    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }


  /*================================================*/
  /*   P r o g M o n i t o r    Interface Methods   */
  /*================================================*/


  private long totalBytes;
  private long totalTransferSize;
  private long transferStartDateMillis;
  private long lastTransferUpdateDateMillis;
  private int currentTask = -1;
  private boolean allDone;
  private boolean jobKilled;

  public void enqueue(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "enqueue(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    Stats.moveGlobe(this);
//    Stats.setStatus("New transfer request... [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }
  public void dequeue(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "dequeue(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    //Stats.setStatus("Preparing to transfer ... [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }
  public void startSend(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "startSend(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }
  public void startSendAction(String actionName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "startSendAction(String actionName)");
    if (trace != null) trace.args(actionName);
    if (trace != null) trace.data(10, name);
    //Stats.setStatus("Sending request ... [" + actionName + "]");
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }
  public void startSendData(String dataName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "startSendData(String dataName)");
    if (trace != null) trace.args(dataName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }
  public void doneSend(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "doneSend(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    //Stats.setStatus("Waiting for reply ... [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }
  public void doneSendAction(String actionName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "doneSendAction(String actionName)");
    if (trace != null) trace.args(actionName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }
  public void doneSendData(String dataName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "doneSendData(String dataName)");
    if (trace != null) trace.args(dataName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }
  public void startReceive(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "startReceive(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    //Stats.setStatus("Receiving reply ... [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }
  public void startReceiveAction(String actionName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "startReceiveAction(String actionName)");
    if (trace != null) trace.args(actionName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }
  public void startReceiveData(String dataName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "startReceiveData(String dataName)");
    if (trace != null) trace.args(dataName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }
  public void doneReceive(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "doneReceive(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    //Stats.setStatus("Reply received. [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }
  public void doneReceiveAction(String actionName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "doneReceiveAction(String actionName)");
    if (trace != null) trace.args(actionName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }
  public void doneReceiveData(String dataName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "doneReceiveData(String dataName)");
    if (trace != null) trace.args(dataName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }
  public void startExecution(int actionCode) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "startExecution(int actionCode)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.data(10, name);
    //Stats.setStatus("Executing reply ... [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }
  public void doneExecution(int actionCode) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "doneExecution(int actionCode)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.data(10, name);
//    Stats.setStatus("Action completed. [" + MessageActionNameSwitch.getActionInfoName(actionCode) + "]");
    Stats.stopGlobe(this);
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }
  public String getLastStatusInfo() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "getLastStatusInfo()");
    String lastStatus = jStatus1.getText();
    if (trace != null) trace.exit(TransferProgMonitorImpl.class, lastStatus);
    return lastStatus;
  }
  public String getLastStatusTitle() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  public void setCurrentStatus(String currentStatus) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "setCurrentStatus(String currentStatus)");
    if (trace != null) trace.args(currentStatus);
    if (trace != null) trace.data(10, name);
    jStatus1.setText(currentStatus);
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }
  public void setFileNameSource(String fileName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "setFileNameSource(String fileName)");
    if (trace != null) trace.args(fileName);
    if (trace != null) trace.data(10, name);
    jNotes[1].setText(fileName);
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }
  public void setFileNameDestination(String fileName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "setFileNameDestination(String fileName)");
    if (trace != null) trace.args(fileName);
    if (trace != null) trace.data(10, name);
    // no-op, we are using full path name instead
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }
  public void setFilePathDestination(String filePath) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "setFileNameDestination(String filePath)");
    if (trace != null) trace.args(filePath);
    if (trace != null) trace.data(10, name);
    jNotes[2].setText(filePath);
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }
  public long getTransferred() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "getTransferred()");
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(TransferProgMonitorImpl.class, totalBytes);
    return totalBytes;
  }
  public long getTransferSize() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "getTransferSize()");
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(TransferProgMonitorImpl.class, totalTransferSize);
    return totalTransferSize;
  }
  public void setTransferSize(long size) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "setTransferSize(long size)");
    if (trace != null) trace.args(size);
    if (trace != null) trace.data(10, name);
    jProgressBar.setMinimum(0);
    jProgressBar.setMaximum((int) (size/100));
    resetStats(size);
    updateStats(System.currentTimeMillis(), 5);
    jProgressBar.setVisible(size != -1);
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }
  public void updateTransferSize(long size) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "updateTransferSize(long size)");
    if (trace != null) trace.args(size);
    if (trace != null) trace.data(10, name);
    jProgressBar.setMaximum((int) (size/100));
    updateStats(System.currentTimeMillis(), 5);
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }
  public void addBytes(long bytes) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "addBytes(long bytes)");
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
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }
  public void doneTransfer() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "doneTransfer");
    if (trace != null) trace.data(10, name);
    if (currentTask >= 0) {
      MiscGui.setPlainFont(jCheckTasks[currentTask]);
      jCheckTasks[currentTask].setSelected(true);
    }
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }
  public void nextTask() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "nextTask");
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
    if (monitoringType == MONITORING_DOWNLOAD && destDir != null && !destDir.equals(DownloadUtilities.getDefaultTempDir()) && jStatus2.isVisible()) {
      jStatus2.setText(destDir.getAbsolutePath() + File.separator + jCheckTasks[currentTask].getText());
    }
    if (monitoringType == MONITORING_UPLOAD && jStatus2.isVisible()) {
      jStatus2.setText(((File) tasks[currentTask]).getAbsolutePath());
    }
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  } // end nextTask()

  public void allDone() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "allDone");
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
          FetchedDataCache cache = FetchedDataCache.getSingleInstance();
          FileLinkRecord fileLink = fileLinks[i];
          FileDataRecord fileData = cache.getFileDataRecord(fileLink.fileId);
          if (fileData != null) {
            File tempFile = fileData.getPlainDataFile();
            if (tempFile != null) {
              boolean shouldBeReadOnly = false;
              if (!FileLobUpEditMonitor.canMonitor(cache, fileLink)) {
                shouldBeReadOnly = true;
                tempFile.setReadOnly();
              }
              GlobalProperties.addTempFileToCleanup(tempFile);
              boolean openned = BrowserLauncher.openFile(context, tempFile);
              if (openned && !shouldBeReadOnly)
                FileLobUpEditMonitor.registerForMonitoring(cache, tempFile, fileLink, fileData);
            }
          }
        }
      }
    }
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  } // end allDone()

  public void jobKilled() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "jobKilled()");
    if (trace != null) trace.data(10, name);
    setCurrentStatus("Job Failed!");
    jobKilled = true;
    Stats.stopGlobe(this);
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }

  public void jobForRetry() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "jobForRetry()");
    if (trace != null) trace.data(10, name);
    setCurrentStatus("Job Queued for Retry.");
    Stats.stopGlobe(this);
    closeProgMonitor();
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }

  public void setInterrupt(Interruptible interruptible) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "setInterrupt(Interruptible interruptible)");
    if (trace != null) trace.args(interrupt);
    if (trace != null) trace.data(10, name);
    this.interrupt = interruptible;
    if (cancelled) {
      closeProgMonitor();
      interruptible.interrupt();
    }
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }

  public boolean isMonitoringDownload() {
    return monitoringType == MONITORING_DOWNLOAD || monitoringType == MONITORING_OPEN;
  }

  public boolean isMonitoringOpen() {
    return monitoringType == MONITORING_OPEN;
  }

  public void setOpenWhenFinished(boolean flag) {
    if (isMonitoringDownload()) {
      monitoringType = flag ? MONITORING_OPEN : MONITORING_DOWNLOAD;
    } else {
      throw new IllegalStateException("Cannot set 'open' flag when not in download mode.");
    }
  }

  public boolean isAllDone() {
    return allDone;
  }
  public boolean isCancelled() {
    return cancelled;
  }
  public boolean isJobKilled() {
    return jobKilled;
  }

  public void setCancellable(Cancellable cancellable) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "setCancellable(Cancellable cancellable)");
    if (trace != null) trace.args(cancellable);
    if (trace != null) trace.data(10, name);
    this.cancellable = cancellable;
    if (cancelled) {
      closeProgMonitor();
      cancellable.setCancelled();
    }
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }

  public void interruptAndCancel() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(TransferProgMonitorImpl.class, "interruptAndCancel()");
    cancelled = true;
    Cancellable ourCancellable = cancellable;
    if (ourCancellable != null)
      ourCancellable.setCancelled();
    Interruptible ourInterrupt = interrupt;
    if (allDone || jobKilled) {
      closeProgMonitor();
    } else if (ourInterrupt != null) {
      closeProgMonitor();
      ourInterrupt.interrupt();
    }
    if (trace != null) trace.exit(TransferProgMonitorImpl.class);
  }

  public void nextTask(String task) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void appendLine(String info) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean isLoginMonitor() {
    return false;
  }
}