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

package com.CH_cl.service.engine;

import com.CH_co.trace.Trace;
import com.CH_co.monitor.*;

import com.CH_co.service.msg.MessageAction;
import com.CH_cl.service.actions.ClientMessageAction;

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
 * <b>$Revision: 1.18 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class DefaultReplyRunner extends Thread {

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

    // change the priority of this thread to minimum
    setPriority(MIN_PRIORITY);

    if (trace != null) trace.exit(DefaultReplyRunner.class);
  }

  public void run() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultReplyRunner.class, "run()");
    if (trace != null) trace.data(10, "calling method that does real work");
    try {
      nonThreadedRun(serverInterfaceLayer, msgAction);
    } catch (Throwable t) {
      if (trace != null) trace.data(100, "Exception while running action", msgAction);
      if (trace != null) trace.exception(DefaultReplyRunner.class, 101, t);
    }

    // help garbage collection
    this.serverInterfaceLayer = null;
    this.msgAction = null;

    if (trace != null) trace.data(100, Thread.currentThread().getName() + " done.");
    if (trace != null) trace.exit(DefaultReplyRunner.class);
    if (trace != null) trace.clear();
  }

  /** Same as run() but without clearing thread trace stack. */
  public static void nonThreadedRun(ServerInterfaceLayer serverInterfaceLayer, ClientMessageAction msgAction) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(DefaultReplyRunner.class, "nonThreadedRun(ServerInterfaceLayer serverInterfaceLayer, ClientMessageAction msgAction)");
    if (trace != null) trace.args(serverInterfaceLayer);
    if (trace != null) trace.args(msgAction);

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
      ProgMonitor progressMonitor = ProgMonitorPool.getProgMonitor(msgAction.getStamp());
      // Don't want dumping monitor here on the client side, server side would be ok.
      if (ProgMonitorPool.isDummy(progressMonitor))
        progressMonitor = new DefaultProgMonitor(false);

      int actionCode = msgAction.getActionCode();
      progressMonitor.startExecution(actionCode);
      // RUN
      try {
        reply = msgAction.runAction();
      } catch (Throwable t) {
        if (trace != null) trace.exception(DefaultReplyRunner.class, 100, t);
      }
      progressMonitor.doneExecution(actionCode);

      if (reply == null && msgAction.getStamp() != 0)
        progressMonitor.allDone();
    }

    if (trace != null) trace.exit(DefaultReplyRunner.class, reply);
    return reply;
  }

}