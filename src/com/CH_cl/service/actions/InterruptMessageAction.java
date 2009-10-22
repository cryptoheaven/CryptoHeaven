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

package com.CH_cl.service.actions;

import javax.swing.JOptionPane;

import com.CH_co.monitor.*;
import com.CH_co.service.msg.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

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

    ProgMonitor pm = ProgMonitorPool.getProgMonitor(getStamp());
    if (pm != null)
      pm.jobKilled();

    if (!isCancelled()) {
      int actionCode = getActionCode();
      if (!DefaultProgMonitor.isSuppressProgressDialog(actionCode)) {
        // Display 'interrupt' messages only when logged in... skip them when logout is in progress
        if (getServerInterfaceLayer().isLastLoginMsgActionSet()) {
          String title = "Interrupted";
          String msg = MessageActionNameSwitch.getActionInfoName(actionCode) + " operation was interrupted.";
          // if initial login failed, give better help message
          if (!getServerInterfaceLayer().hasPersistantMainWorker() && (actionCode == CommandCodes.USR_Q_LOGIN_SECURE_SESSION || actionCode == CommandCodes.USR_A_LOGIN_SECURE_SESSION)) {
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
            msg = "<html>Error occurred while trying to connect to the "+URLs.get(URLs.SERVICE_SOFTWARE_NAME)+" Data Server" + (server.length() > 0 ? (" at " + server + " on port " + port) : "") + ".  "
                + "Please verify your computer network and/or modem cables are plugged-in and your computer is currently connected to the Internet.  When you have established and verified your Internet connectivity, please try connecting to "+URLs.get(URLs.SERVICE_SOFTWARE_NAME)+" again.  "
                + "If the problem persists please visit <a href=\""+URLs.get(URLs.CONNECTIVITY_PAGE)+"\">"+URLs.get(URLs.CONNECTIVITY_PAGE)+"</a> for help. <p>";
          } 
          Integer key = new Integer(actionCode);
          new SingleDialogShower(singleInterruptedDialogArbiter, key, null, MessageDialog.ERROR_MESSAGE, title, msg).start();
        } else {
          if (trace != null) trace.data(100, "suppress interrupt msg");
        }
      }
    }

    if (trace != null) trace.exit(InterruptMessageAction.class, null);
    return null;
  }

}