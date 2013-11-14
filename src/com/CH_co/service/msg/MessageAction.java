/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.msg;

import com.CH_co.io.DataInputStream2;
import com.CH_co.io.DataOutputStream2;
import com.CH_co.monitor.*;
import com.CH_co.service.engine.CommonSessionContext;
import com.CH_co.service.msg.dataSets.Str_Rp;
import com.CH_co.service.msg.dataSets.obj.Obj_List_Co;
import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;
import java.io.IOException;
import java.util.ArrayList;

/** 
* Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
*
* Basic action of the client or the server in response to the message data received.
* Cancel relates to individual requests as canceled by for example a progress monitor.
* Interrupt relates to chains of requests where some reply may cause subsequent requests.
*
* @author  Marcin Kurzawa
*/
public class MessageAction extends Message implements Cancellable {

  // The IO source of this message.  Each message which was recreated from the stream should have this set.
  private CommonSessionContext sessionContext;


  private int actionCode;
  private long uniqueStamp;
  private static final Object stampMonitor = new Object();
  private static long lastUniqueTimeStamp;
  private static int stampSubNumber;
  private static int stampSubNumberMax = 1000;

  private boolean cancelled;
  private Interrupter interrupter;
  private Interruptible interruptible;
  private int sendTryCount;
  private final static int MAX_SEND_TRY_COUNT = 2;

  // Manual override of job priority used in queueing requests.
  private Long priority = null;

  /** Creates new MessageAction */
  protected MessageAction() {
  }

  public MessageAction(int actionCode, ProtocolMsgDataSet protocolMsgDataSet, Interrupter interrupter, Interruptible interruptible) {
    this(actionCode, protocolMsgDataSet, nextStamp(), false, interrupter, interruptible);
  }
  public MessageAction(int actionCode, ProtocolMsgDataSet protocolMsgDataSet) {
    this(actionCode, protocolMsgDataSet, true);
  }
  public MessageAction(int actionCode, String strMsg) {
    this(actionCode, new Str_Rp(strMsg), true);
  }
  public MessageAction(int actionCode, ProtocolMsgDataSet protocolMsgDataSet, boolean timeStamp) {
    this(actionCode, protocolMsgDataSet, timeStamp == true ? nextStamp() : 0, false, null, null);
  }
  private MessageAction(int actionCode, ProtocolMsgDataSet protocolMsgDataSet, long uniqueStamp, boolean cancelled, Interrupter interrupter, Interruptible interruptible) {
    super(protocolMsgDataSet);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageAction.class, "MessageAction(int actionCode, ProtocolMsgDataSet protocolMsgDataSet, long uniqueStamp, boolean cancelled, Interrupter interrupter, Interruptible interruptible)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args("protocolMsgDataSet:... (too Long)");
    if (trace != null) trace.args(uniqueStamp);
    if (trace != null) trace.args(cancelled);
    if (trace != null) trace.args(interrupter, interruptible);

    this.actionCode = actionCode;
    this.uniqueStamp = uniqueStamp;
    this.cancelled = cancelled;
    this.interrupter = interrupter;
    this.interruptible = interruptible;

    if (trace != null) trace.exit(MessageAction.class);
  }


  /**
  * Protected constructors used in ReplyMessageAction where stamp is copied from request message.
  */
  protected MessageAction(int actionCode, ProtocolMsgDataSet protocolMsgDataSet, MessageAction originalRequest) {
    this(actionCode, protocolMsgDataSet, originalRequest.uniqueStamp, originalRequest.cancelled, null, null);
  }
  protected MessageAction(int actionCode, String strMsg, MessageAction originalRequest) {
    this(actionCode, new Str_Rp(strMsg), originalRequest.uniqueStamp, originalRequest.cancelled, null, null);
  }
  protected MessageAction(int actionCode, MessageAction originalRequest) { // called by ReplyMessageAction
    this(actionCode, (ProtocolMsgDataSet) null, originalRequest.uniqueStamp, originalRequest.cancelled, null, null);
  }


  /**
  * Create a Message Action without message data set.
  * @param timeStamp true if message should be time stamped, false otherwise (stamp=0)
  */
  public MessageAction(int actionCode, boolean timeStamp) {
    this(actionCode, (ProtocolMsgDataSet) null, timeStamp);
  }
  /**
  * Create a Message Action without message data set and time-stamped.
  */
  public MessageAction(int actionCode) {
    this(actionCode, (ProtocolMsgDataSet) null, true);
  }


  public int getActionCode() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageAction.class, "getActionCode()");
    if (trace != null) trace.exit(MessageAction.class, actionCode);
    return actionCode;
  }
  public void setActionCode(int code) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageAction.class, "setActionCode(int code)");
    if (trace != null) trace.args(code);
    actionCode = code;
    if (trace != null) trace.exit(MessageAction.class);
  }

  private static long nextStamp() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageAction.class, "nextStamp()");
    long returnStamp = 0;
    synchronized (stampMonitor) {
      long nextUniqueTimeStamp = System.currentTimeMillis();
      if (nextUniqueTimeStamp != lastUniqueTimeStamp) {
        stampSubNumber = 0;
      } else {
        stampSubNumber ++;
        if (stampSubNumber >= stampSubNumberMax) {
          stampSubNumber = 0;
          while (nextUniqueTimeStamp == lastUniqueTimeStamp) {
            try { Thread.sleep(1); } catch (InterruptedException e) { }
            nextUniqueTimeStamp = System.currentTimeMillis();
          }
        }
      }
      lastUniqueTimeStamp = nextUniqueTimeStamp;
      returnStamp = nextUniqueTimeStamp*stampSubNumberMax + stampSubNumber;
    }
    if (trace != null) trace.exit(MessageAction.class, returnStamp);
    return returnStamp;
  }

  public long getStamp() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageAction.class, "getStamp()");
    if (trace != null) trace.exit(MessageAction.class, uniqueStamp);
    return uniqueStamp;
  }
  public long getStampTime() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageAction.class, "getStampTime()");
    long stampTime = uniqueStamp/stampSubNumberMax;
    if (trace != null) trace.exit(MessageAction.class, stampTime);
    return stampTime;
  }
  /**
  * Change the stamp to a current and next value.
  */
  public void restamp() {
    uniqueStamp = nextStamp();
  }

  public Long getPriority() {
    return priority;
  }
  public void setPriority(long priorityValue) {
    priority = new Long(priorityValue);
  }

  /** Output this Message to a specified stream. */
  public void writeToStream(DataOutputStream2 out, short clientBuild, short serverBuild) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageAction.class, "writeToStream(DataOutputStream2, short clientBuild, short serverBuild)");
    if (trace != null) trace.args(clientBuild);
    if (trace != null) trace.args(serverBuild);
    // Other thread may try to push notification as well, so
    // MessageAction synchronizes write to ensure than massages are atomic.

    // only one thread should be writing to a stream at a time
    synchronized(out) {
      // This code will be consumed by code which determines what instance of MessageAction to reconstruct.
      if (trace != null) trace.data(10, "actionCode", actionCode);
      out.writeInt(actionCode);

      if (trace != null) trace.data(20, "uniqueStamp", uniqueStamp);
      out.writeLong(uniqueStamp);

      if (trace != null) trace.data(30, "this", this);

      ProgMonitorI progressMonitor = ProgMonitorPool.getProgMonitor(uniqueStamp);
      progressMonitor.startSend(actionCode, uniqueStamp);
      String actionInfoName = MessageActionNameSwitch.getActionInfoName(actionCode);
      progressMonitor.startSendAction(actionInfoName);
      super.writeToStream(out, progressMonitor, clientBuild, serverBuild);
      progressMonitor.doneSendAction(actionInfoName);
      progressMonitor.doneSend(actionCode, uniqueStamp);

      out.flush();
    }
    if (trace != null) trace.exit(MessageAction.class);
  }


  /**
  * @return the action code of the incoming message
  */
  public static int readActionCodeFromStream(DataInputStream2 dataIn) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageAction.class, "readActionCodeFromStream(DataInputStream2 dataIn)");
    // Thread is held here until integer becomes available
    int code = 0;
    try {
      code = dataIn.readInt();
    } catch (IOException e) {
      if (trace != null) trace.exception(MessageAction.class, 100, e);
      throw e;
    }
    if (trace != null) trace.exit(MessageAction.class, code);
    return code;
  }

  /**
  * @return Action stamp of the incoming message.  Stamp follows after Action Code.
  */
  public static long readActionStampFromStream(DataInputStream2 in) throws IOException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageAction.class, "readStampFromStream(DataInputStream2 in)");
    long stamp = 0;
    try {
      stamp = in.readLong();
    } catch (IOException e) {
      if (trace != null) trace.exception(MessageAction.class, 100, e);
      throw e;
    }
    if (trace != null) trace.exit(MessageAction.class, stamp);
    return stamp;
  }

  // Other thread may try to read notification as well, so
  // MessageAction synchronizes read to ensure than massages are atomic.
  protected void initFromStream(DataInputStream2 in, int actionCode, short clientBuild, short serverBuild) throws IOException, DataSetException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageAction.class, "initFromStream(DataInputStream2, int actionCode, short clientBuild, short serverBuild)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(clientBuild);
    if (trace != null) trace.args(serverBuild);

    // only one thread should be reading a message at a time
    synchronized(in) {
      initFromStream(in, actionCode, readActionStampFromStream(in), clientBuild, serverBuild);
    }
    if (trace != null) trace.exit(MessageAction.class);
  }

  protected void initFromStream(DataInputStream2 in, int actionCode, long stamp, short clientBuild, short serverBuild) throws IOException, DataSetException {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MessageAction.class, "initFromStream(DataInputStream2 in, int actionCode, long stamp, short clientBuild, short serverBuild)");
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(stamp);
    if (trace != null) trace.args(clientBuild);
    if (trace != null) trace.args(serverBuild);

    synchronized(in) {
      this.actionCode = actionCode;
      this.uniqueStamp = stamp;

      ProgMonitorI progressMonitor = ProgMonitorPool.getProgMonitor(stamp);
      progressMonitor.startReceive(actionCode, stamp);
      String actionInfoName = MessageActionNameSwitch.getActionInfoName(actionCode);
      progressMonitor.startReceiveAction(actionInfoName);
      super.initFromStream(in, progressMonitor, clientBuild, serverBuild);
      progressMonitor.doneReceiveAction(actionInfoName);
      progressMonitor.doneReceive(actionCode, stamp);
    }
    if (trace != null) trace.exit(MessageAction.class);
  }

  public void copyAllFromAction(MessageAction sourceAction) {
    this.actionCode = sourceAction.actionCode;
    this.uniqueStamp = sourceAction.uniqueStamp;
    this.sessionContext = sourceAction.sessionContext;
    super.copyDataSetFromMessage(sourceAction);
  }

  public void copyStampFromAction(MessageAction sourceAction) {
    this.uniqueStamp = sourceAction.uniqueStamp;
  }

  /**
  * Initialize the Message Action with the context of the communications through which it came.
  */
  protected void setCommonContext(CommonSessionContext sessionContext) {
    this.sessionContext = sessionContext;
  }

  protected CommonSessionContext getCommonContext() {
    return sessionContext;
  }

  public void markSendTry() {
    sendTryCount ++;
  }
  public boolean areRetriesExceeded() {
    return sendTryCount >= MAX_SEND_TRY_COUNT-1;
  }

  public void setCancelled() {
    this.cancelled = true;
  }
  public boolean isCancelled() {
    return cancelled;
  }
  public boolean isInterrupted() {
    if (interrupter == null) {
      return false;
    } else {
      return interrupter.isInterrupted();
    }
  }
  public boolean isInterruptible() {
    return interruptible != null;
  }
  public void interrupt() {
    if (interruptible != null)
      interruptible.interrupt();
  }
  public void setInterruptsFrom(MessageAction fromAction) {
    interrupter = fromAction.interrupter;
    interruptible = fromAction.interruptible;
  }

  public String toString() {
    return "["+Misc.getClassNameWithoutPackage(getClass())
      + ": actionCode="     + actionCode
      + ", uniqueStamp="    + uniqueStamp
      + ", sessionContext=" + sessionContext
      + ", super="          + super.toString()
      + "]";
  }

  public String formatForDisplay() {
    String text = ""
      + " actionCode = " + getActionCode()
      + "\n uniqueStamp = " + getStamp()
      + "\n data set class= " + Misc.getClassNameWithoutPackage(getMsgDataSet().getClass())
      + "\n data set = " + getMsgDataSet().toStringLongFormat()
      + "\n session context = " + sessionContext;
    return text;
  }

  public ProtocolMsgDataSet wrapIntoTransferable() {
    ArrayList objsL = new ArrayList();
    objsL.add(new Integer(actionCode));
    objsL.add(new Long(uniqueStamp));
    objsL.add(getMsgDataSet());
    Obj_List_Co set = new Obj_List_Co(objsL);
    return set;
  }

  public static MessageAction unwrapFromTransferable(ProtocolMsgDataSet dataSet) {
    Obj_List_Co set = (Obj_List_Co) dataSet;
    int actionCode = ((Integer) set.objs[0]).intValue();
    MessageAction action = new MessageAction(actionCode);
    action.uniqueStamp = ((Long) set.objs[1]).longValue();
    action.setMsgDataSet((ProtocolMsgDataSet) set.objs[2]);
    return action;
  }

}