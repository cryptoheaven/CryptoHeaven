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

package com.CH_cl.service.actions;

import com.CH_co.monitor.DefaultProgMonitor;
import com.CH_co.monitor.ProgMonitorI;
import com.CH_co.monitor.ProgMonitorPool;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.MessageActionNameSwitch;
import com.CH_co.trace.Trace;
import com.CH_co.util.NotificationCenter;
import com.CH_co.util.SingleTokenArbiter;
import com.CH_co.util.URLs;

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
* <b>$Revision: 1.22 $</b>
* @author  Marcin Kurzawa
* @version 
*/
public class InterruptMessageAction extends ClientMessageAction {

  // Only one message dialog per interrupted action, issue tokens for the dialogs to 
  // suppress multiple dialogs for a single actionCode, before the shown dialog is dismissed.
  private static SingleTokenArbiter singleInterruptedDialogArbiter = new SingleTokenArbiter();


  /** Creates new InterruptMessageAction */
  public InterruptMessageAction(MessageAction interruptedAction) {
    super(interruptedAction);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(InterruptMessageAction.class, "InterruptMessageAction()");
    if (trace != null) trace.exit(InterruptMessageAction.class);
  }

  /** 
  * The action handler performs all actions related to the received message (reply),
  * and optionally returns a request Message.  If there is no request, null is returned.
  */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(InterruptMessageAction.class, "runAction()");

    ProgMonitorI pm = ProgMonitorPool.getProgMonitor(getStamp());
    if (pm != null)
      pm.jobKilled();

    if (!isCancelled()) {
      int actionCode = getActionCode();
      if (!DefaultProgMonitor.isSuppressProgressDialog(actionCode)) {
        // Display 'interrupt' messages only when logged in... skip them when logout is in progress
        if (getServerInterfaceLayer().isLastLoginMsgActionSet()) {
          boolean isSuppressed = false;

          String title = "Interrupted";
          String interruptedSuffix = " operation was interrupted.";
          Object key = interruptedSuffix; // show 1 interrupted msg at a time, and skip the other
          int msgType = NotificationCenter.ERROR_MESSAGE;
          String msg = null;

          // if initial login failed, give better help message
          if (!getServerInterfaceLayer().hasPersistentMainWorker() && (actionCode == CommandCodes.USR_Q_LOGIN_SECURE_SESSION || actionCode == CommandCodes.USR_A_LOGIN_SECURE_SESSION)) {
            title = "Login failed";
            String server = "";
            String port = "";
            try {
              java.net.Socket s = getClientContext().getSocket();
              server = "" + s.getInetAddress();
              port = "" + s.getPort();
            } catch (Throwable t) {
              if (trace != null) trace.exception(InterruptMessageAction.class, 100, t);
            }
            msg = "<html>Error occurred while trying to connect to the "+URLs.get(URLs.SERVICE_SOFTWARE_NAME)+" Data Server" + (server.length() > 0 ? (" at " + server + " on port " + port) : "") + ". "
                + "Please verify your internet connectivity. "
                + "If the problem persists please visit <a href=\""+URLs.get(URLs.CONNECTIVITY_PAGE)+"\">"+URLs.get(URLs.CONNECTIVITY_PAGE)+"</a> for help. <p>";
            msgType = NotificationCenter.ERROR_CONNECTION;
            key = new Integer(actionCode);
          } else if (!getServerInterfaceLayer().hasPersistentMainWorker()) {
            // if action failed due to connection problem, give offline notice
            title = "No Connection";
            msg = "Could not communicate with the server.  Please check your internet connectivity.\n\n"+MessageActionNameSwitch.getActionInfoName(actionCode)+" failed.";
            msgType = NotificationCenter.ERROR_CONNECTION;
          } else if (getServerInterfaceLayer().hasPersistentMainWorker() && (actionCode == CommandCodes.USR_Q_LOGIN_SECURE_SESSION || actionCode == CommandCodes.USR_A_LOGIN_SECURE_SESSION)) {
            // Suppress login errors if we already have at least one working connection.
            isSuppressed = true;
            if (trace != null) trace.data(50, "suppressing login error, we already have a valid connection");
          } else {
            msg = MessageActionNameSwitch.getActionInfoName(actionCode) + interruptedSuffix;
          }

          if (!isSuppressed)
            NotificationCenter.show(singleInterruptedDialogArbiter, key, msgType, title, msg);
        } else {
          if (trace != null) trace.data(100, "suppress interrupt msg");
        }
      }
    }

    if (trace != null) trace.exit(InterruptMessageAction.class, null);
    return null;
  }

}