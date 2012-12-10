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

import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageActionNameSwitch;
import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;

/** 
* <b>Copyright</b> &copy; 2001-2012
* <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
* CryptoHeaven Corp.
* </a><br>All rights reserved.<p>
*
* Class Description:
*
*
* Class Details:
*
*
* <b>$Revision: 1.25 $</b>
* @@author  marcin
* @@version
*/
public class DefaultProgMonitor extends AbstractProgMonitor implements ProgMonitorI {

  private static int DELAY_DECIDE = 20000;
  private static int DELAY_POPUP = 15000;

  private static int MIN_VALUE = 0;
  private static int MAX_VALUE = 7;

  private ProgMonitorMultiI pm;
  private int value;
  private String actionName;
  private String name;
  private boolean withProgressDialog;

  private static final Object counterMonitor = new Object();
  private static int counter = 0;

  public DefaultProgMonitor() {
    this(false);
  }
  /** Creates new DefaultProgMonitor */
  public DefaultProgMonitor(boolean withProgressDialog) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "DefaultProgMonitor()");
    this.withProgressDialog = withProgressDialog;
    synchronized (counterMonitor) {
      name = DefaultProgMonitor.class.getName() + " #" + counter;
      if (trace != null) trace.data(10, "creating... ", name);
      counter ++;
      if (counter == Integer.MAX_VALUE)
        counter = 0;
    }
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }


  /**
  * Create the progress bar GUI instance that will display progress.
  */
  private void createProgMonitorGUIifNeeded(int actionCode) {
    if (withProgressDialog && pm == null && !allDone && !killed) {
      actionName = MessageActionNameSwitch.getActionInfoName(actionCode) ;
      pm = ProgMonitorFactory.newInstanceMulti(null, actionName+ " in progress...", "Waiting in queue...", MIN_VALUE, MAX_VALUE);
      pm.setMillisToDecideToPopup(DefaultProgMonitor.DELAY_DECIDE);
      pm.setMillisToPopup(DefaultProgMonitor.DELAY_POPUP);
    }
    if (pm != null) {
      //value = 0;
      pm.setProgress(value);
    }
  }

  /** Action enqueued in the job list - request created and prepared */
  public void enqueue(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "enqueue(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    if (!allDone) {
      if (!Misc.isAllGUIsuppressed()) {
        super.enqueue(actionCode, stamp);
        // reset progress value in case action was returned back to the queue for retry
        createProgMonitorGUIifNeeded(actionCode);
      }
    }
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }

  /** Action dequeued from the job list - worker prepares to send request */
  public void dequeue(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "dequeue(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    if (!allDone) {
      if (!Misc.isAllGUIsuppressed()) {
        super.dequeue(actionCode, stamp);
        // reset progress value in case action was returned back to the queue for retry
        createProgMonitorGUIifNeeded(actionCode);
        value++;
        if (withProgressDialog && pm != null) {
          pm.setProgress(value);
          pm.setNote("Preparing to send...");
        }
      }
    }
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }

  /** START SEND */
  public void startSend(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "startSend(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    if (!allDone) {
      if (!Misc.isAllGUIsuppressed()) {
        super.startSend(actionCode, stamp);
        // in case enqueue was not called while creating some special job, the communication layer will start with the "send" call
        // so make sure we have the default prog monitor
        createProgMonitorGUIifNeeded(actionCode);
        value++;
        if (withProgressDialog && pm != null) {
          pm.setProgress(value);
          pm.setNote("Sending request...");
        }
      }
    }
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }
  public void startSendAction(String actionName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "startSendAction(String actionName)");
    if (trace != null) trace.args(actionName);
    if (trace != null) trace.data(10, name);
    if (!allDone) {
      this.actionName = actionName;
    }
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }
  public void startSendData(String dataName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "startSendData(String dataName)");
    if (trace != null) trace.args(dataName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }


  /** DONE SEND */
  public void doneSend(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "doneSend(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    if (!allDone) {
      if (!Misc.isAllGUIsuppressed()) {
        super.doneSend(actionCode, stamp);
        value++;
        if (withProgressDialog && pm != null) {
          pm.setProgress(value);
          pm.setNote("Waiting for reply...");
        }
      }
    }
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }
  public void doneSendAction(String actionName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "doneSendAction(String actionName)");
    if (trace != null) trace.args(actionName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }
  public void doneSendData(String dataName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "doneSendData(String dataName)");
    if (trace != null) trace.args(dataName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }


  /** START RECEIVE */
  public void startReceive(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "startReceive(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    if (!allDone) {
      if (!Misc.isAllGUIsuppressed()) {
        super.startReceive(actionCode, stamp);
        value++;
        if (withProgressDialog && pm != null) {
          pm.setProgress(value);
          pm.setNote("Receiving reply... ");
        }
      }
    }
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }
  public void startReceiveAction(String actionName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "startReceiveAction(String actionName)");
    if (trace != null) trace.args(actionName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }
  public void startReceiveData(String dataName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "startReceiveData(String dataName)");
    if (trace != null) trace.args(dataName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }

  /** DONE RECEIVE */
  public void doneReceive(int actionCode, long stamp) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "doneReceive(int actionCode, long stamp)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.data(10, name);
    if (!allDone) {
      if (!Misc.isAllGUIsuppressed()) {
        super.doneReceive(actionCode, stamp);
        value++;
        if (withProgressDialog && pm != null) {
          pm.setProgress(value);
          pm.setNote("Receiving reply... done.");
        }
      }
    }
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }
  public void doneReceiveAction(String actionName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "doneReceiveAction(String actionName)");
    if (trace != null) trace.args(actionName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }
  public void doneReceiveData(String dataName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "doneReceiveData(String dataName)");
    if (trace != null) trace.args(dataName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }


  /** EXECUTION */
  public void startExecution(int actionCode) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "startExecution(int actionCode)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.data(10, name);
    if (!allDone) {
      if (!Misc.isAllGUIsuppressed()) {
        super.startExecution(actionCode);
        value++;
        if (withProgressDialog && pm != null) {
          pm.setProgress(value);
          pm.setNote("Start executing reply... ");
        }
      }
    }
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }
  public void doneExecution(int actionCode) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "doneExecution(int actionCode)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.data(10, name);
    if (!allDone) {
      if (!Misc.isAllGUIsuppressed()) {
        super.doneExecution(actionCode);
        value++;
        if (withProgressDialog && pm != null) {
          pm.setProgress(value);
          pm.setNote("Start executing reply... done.");
        }
      }
    }
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }



  public void setCurrentStatus(String currentStatus) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "setCurrentStatus(String currentStatus)");
    if (trace != null) trace.args(currentStatus);
    if (trace != null) trace.data(10, name);
    if (!allDone) {
      value++;
      if (!Misc.isAllGUIsuppressed() && withProgressDialog && pm != null) {
        pm.setProgress(value);
        pm.setNote(currentStatus);
      }
    }
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }

  public void setFileNameSource(String fileName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "setFileNameSource(String fileName)");
    if (trace != null) trace.args(fileName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }
  public void setFileNameDestination(String fileName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "setFileNameDestination(String fileName)");
    if (trace != null) trace.args(fileName);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }
  public void setFilePathDestination(String filePath) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "setFileNameDestination(String filePath)");
    if (trace != null) trace.args(filePath);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }
  public long getTransferred() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "getTransferred()");
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(DefaultProgMonitor.class, -1);
    return -1;
  }
  public void setTransferSize(long size) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "setTransferSize(long size)");
    if (trace != null) trace.args(size);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }
  public void updateTransferSize(long size) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "updateTransferSize(long size)");
    if (trace != null) trace.args(size);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }
  public void addBytes(long bytes) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "addBytes(long bytes)");
    if (trace != null) trace.args(bytes);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }
  public void doneTransfer() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "doneTransfer()");
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }
  public void nextTask() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "nextTask()");
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }
  public void allDone() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "allDone()");
    if (trace != null) trace.data(10, name);
    if (!allDone) {
      allDone = true;
      ProgMonitorPool.removeProgMonitor(this);
      // Don't check for isAllGUIsuppressed() because it might have been suppressed during LOGOUT or EXIT,
      // just close the ProgMonitor if any exists.
      if (withProgressDialog && pm != null) {
        pm.close();
      }
      super.allDone();
    }
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }
  public void setInterrupt(Interruptible interruptible) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "setInterrupt(Interruptible interruptible)");
    if (trace != null) trace.args(interruptible);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }

  public void setCancellable(Cancellable cancellable) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "setCancellable(Cancellable cancellable)");
    if (trace != null) trace.args(cancellable);
    if (trace != null) trace.data(10, name);
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }

  public void jobKilled() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultProgMonitor.class, "jobKilled()");
    if (trace != null) trace.data(10, name);
    if (pm != null) pm.cancel();
    super.jobKilled();
    if (trace != null) trace.exit(DefaultProgMonitor.class);
  }

  public static boolean isSuppressProgressDialog(int msgActionCode) {
    return isSuppressProgressInterruptDialog(msgActionCode) || 
            msgActionCode == CommandCodes.USR_Q_LOGIN_SECURE_SESSION ||
            msgActionCode == CommandCodes.USR_A_LOGIN_SECURE_SESSION;
  }

  public static boolean isSuppressProgressInterruptDialog(int msgActionCode) {
    return
            msgActionCode == CommandCodes.SYS_Q_GET_AUTO_UPDATE ||
            msgActionCode == CommandCodes.SYS_A_GET_AUTO_UPDATE ||
            msgActionCode == CommandCodes.SYS_Q_NOTIFY ||
            msgActionCode == CommandCodes.SYS_A_NOTIFY ||
            msgActionCode == CommandCodes.SYS_Q_PING ||
            msgActionCode == CommandCodes.SYS_A_PONG ||
            msgActionCode == CommandCodes.SYS_Q_VERSION ||
            msgActionCode == CommandCodes.SYS_A_VERSION ||
            msgActionCode == CommandCodes.STAT_Q_GET ||
            msgActionCode == CommandCodes.STAT_A_GET ||
            msgActionCode == CommandCodes.FLD_Q_RED_FLAG_COUNT ||
            msgActionCode == CommandCodes.FLD_A_RED_FLAG_COUNT ||
            msgActionCode == CommandCodes.FLD_Q_SYNC ||
            msgActionCode == CommandCodes.FLD_Q_SYNC_NEXT ||
            msgActionCode == CommandCodes.FLD_Q_SYNC_CONTACTS ||
            msgActionCode == CommandCodes.FLD_Q_SYNC_FOLDER_TREE ||
            msgActionCode == CommandCodes.FLD_A_SYNC ||
            msgActionCode == CommandCodes.FLD_A_SYNC_NEXT ||
            msgActionCode == CommandCodes.FILE_Q_GET_PROGRESS ||
            msgActionCode == CommandCodes.FILE_Q_NEW_FILE_STUDS_BACKGROUND ||
            msgActionCode == CommandCodes.FILE_Q_UPLOAD_CONTENT ||
            msgActionCode == CommandCodes.FILE_A_UPLOAD_COMPLETED ||
            msgActionCode == CommandCodes.FILE_Q_UPDATE_DIGESTS ||
            msgActionCode == CommandCodes.MSG_Q_TYPING ||
            msgActionCode == CommandCodes.MSG_A_TYPING ||
            msgActionCode == CommandCodes.USR_Q_CHANGE_ONLINE_STATUS ||
            msgActionCode == CommandCodes.USR_Q_GET_INIT_DATA ||
            msgActionCode == CommandCodes.USR_Q_LOGOUT ||
            msgActionCode == CommandCodes.USR_A_LOGOUT ||
            msgActionCode == CommandCodes.USR_Q_SEARCH;
  }

  public String toString() {
    return "[DefaultProgMonitor"
      + ": actionName="         + actionName
      + ", name="               + name
      + ", value="              + value
      + ", allDone="            + allDone
      + ", withProgressDialog=" + withProgressDialog
      + "]";
  }

  public void nextTask(String task) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void appendLine(String info) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}