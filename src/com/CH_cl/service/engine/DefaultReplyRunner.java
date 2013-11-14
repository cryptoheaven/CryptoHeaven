/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.engine;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_co.util.MyUncaughtExceptionHandlerOps;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.trace.ThreadTraced;
import com.CH_co.trace.Trace;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.18 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class DefaultReplyRunner extends ThreadTraced {

  private static int defaultReplyRunnerCount;
  private ClientMessageAction msgAction;
  private ServerInterfaceLayer serverInterfaceLayer;

  /** Creates new DefaultReplyRunner */
  public DefaultReplyRunner(ServerInterfaceLayer serverInterfaceLayer, ClientMessageAction msgAction) {
    super("Default Reply Runner # " + defaultReplyRunnerCount);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultReplyRunner.class, "DefaultReplyRunner()");
    if (trace != null) trace.args(serverInterfaceLayer);
    if (trace != null) trace.args(msgAction);
    this.serverInterfaceLayer = serverInterfaceLayer;
    this.msgAction = msgAction;
    defaultReplyRunnerCount ++;
    defaultReplyRunnerCount %= Integer.MAX_VALUE-1;
    setDaemon(true);
    if (trace != null) trace.exit(DefaultReplyRunner.class);
  }

  public void runTraced() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultReplyRunner.class, "DefaultReplyRunner.runTraced()");
    try {
      nonThreadedRun(serverInterfaceLayer, msgAction);
    } catch (Throwable t) {
      if (trace != null) trace.data(100, "Exception while running action", msgAction);
      if (trace != null) trace.exception(DefaultReplyRunner.class, 101, t);
      MyUncaughtExceptionHandlerOps.unhandledException(t);
    }

    // help garbage collection
    this.serverInterfaceLayer = null;
    this.msgAction = null;

    if (trace != null) trace.exit(DefaultReplyRunner.class);
  }

  public static void nonThreadedRun(ServerInterfaceLayer serverInterfaceLayer, ClientMessageAction msgAction) {
    nonThreadedRun(serverInterfaceLayer, msgAction, false);
  }
  /** Same as run() but without clearing thread trace stack. */
  public static void nonThreadedRun(ServerInterfaceLayer serverInterfaceLayer, ClientMessageAction msgAction, boolean suppressAnyErrMsg) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultReplyRunner.class, "nonThreadedRun(ServerInterfaceLayer serverInterfaceLayer, ClientMessageAction msgAction, boolean suppressAnyErrMsg)");
    if (trace != null) trace.args(serverInterfaceLayer);
    if (trace != null) trace.args(msgAction);
    if (trace != null) trace.args(suppressAnyErrMsg);

    // Only set the suppression flag if requested, do not overwrite previous flag with 'false'.
    if (suppressAnyErrMsg && msgAction != null)
      msgAction.isGUIsuppressed = true;

    MessageAction reply = runAction(msgAction);

    // if there is a reply, put it in the Writing Queue
    if (reply != null) {
      if (trace != null) trace.data(50, "response to server reply generated");
      if (trace != null) trace.data(51, "response=");
      if (trace != null) trace.data(52, reply);
      serverInterfaceLayer.submitAndWait(reply, 120000);
    }

    if (trace != null) trace.exit(DefaultReplyRunner.class);
  }

  /**
   * Runs the action and return the client reply to the action coming from the server.
   */
  public static MessageAction runAction(ClientMessageAction msgAction) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultReplyRunner.class, "runAction(ClientMessageAction msgAction)");
    if (trace != null) trace.args(msgAction);

    MessageAction reply = null;

    if (msgAction != null) {
      ServerInterfaceLayer SIL = msgAction.getServerInterfaceLayer();
      // Don't want dumping monitor here on the client side, server side would be ok.
      ProgMonitorI progressMonitor = SIL.assignProgMonitor(msgAction, Boolean.FALSE);

      int actionCode = msgAction.getActionCode();
      if (progressMonitor != null)
        progressMonitor.startExecution(actionCode);
      // RUN
      try {
        reply = msgAction.runAction();
      } catch (Throwable t) {
        if (trace != null) trace.exception(DefaultReplyRunner.class, 100, t);
      }
      if (progressMonitor != null)
        progressMonitor.doneExecution(actionCode);

      if (reply == null && msgAction.getStamp() != 0) {
        if (progressMonitor != null)
          progressMonitor.allDone();
      }
    }

    if (trace != null) trace.exit(DefaultReplyRunner.class, reply);
    return reply;
  }

}