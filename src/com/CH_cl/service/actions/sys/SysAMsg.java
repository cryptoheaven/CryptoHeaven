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

package com.CH_cl.service.actions.sys;

import com.CH_cl.service.actions.*;

import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.obj.*;

import com.CH_co.trace.Trace;
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
 * <b>$Revision: 1.7 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class SysAMsg extends ClientMessageAction {

  private static SingleTokenArbiter msgDialogArbiter = null;

  /** Creates new SysAMsg */
  public SysAMsg() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SysAMsg.class, "SysAMsg()");
    if (trace != null) trace.exit(SysAMsg.class);
  }

  /**
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SysAMsg.class, "runAction(Connection)");

    Obj_List_Co reply = (Obj_List_Co) getMsgDataSet();
    String msgType = (String) reply.objs[0];
    Boolean modal = (Boolean) reply.objs[1]; // depreciated
    String title = (String) reply.objs[2];
    String body = (String) reply.objs[3];

    // Suppress the dialog if title and body are empty
    if (title != null && body != null) {
      if (msgDialogArbiter == null) msgDialogArbiter = new SingleTokenArbiter();
      String key = msgType+title+body;
      int dialogType = NotificationCenter.ERROR_MESSAGE;
      if (msgType.equalsIgnoreCase("i")) {
        dialogType = NotificationCenter.INFORMATION_MESSAGE;
      } else if (msgType.equalsIgnoreCase("w")) {
        dialogType = NotificationCenter.WARNING_MESSAGE;
      } else if (msgType.equalsIgnoreCase("e")) {
        dialogType = NotificationCenter.ERROR_MESSAGE;
      }
      NotificationCenter.show(msgDialogArbiter, key, dialogType, title, body);
    }

    if (trace != null) trace.exit(SysAMsg.class, null);
    return null;
  }

}