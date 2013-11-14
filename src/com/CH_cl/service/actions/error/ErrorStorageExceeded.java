/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.actions.error;

import com.CH_cl.service.actions.*;

import com.CH_co.monitor.*;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.*;
import com.CH_co.util.*;
import com.CH_co.trace.Trace;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.8 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class ErrorStorageExceeded extends ClientMessageAction {

  /** Creates new ErrorStorageExceeded */
  public ErrorStorageExceeded() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ErrorStorageExceeded.class, "ErrorStorageExceeded()");
    if (trace != null) trace.exit(ErrorStorageExceeded.class);
  }

  /**
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ErrorStorageExceeded.class, "runAction(Connection)");

    ProgMonitorI pm = ProgMonitorPool.getProgMonitor(getStamp());
    if (pm != null)
      pm.jobKilled();

    // Check individual action GUI suppression, global flag will be checked by NotificationCenter
    if (!isGUIsuppressed) {
      Str_Rp reply = (Str_Rp) getMsgDataSet();
      int msgType = NotificationCenter.ERROR_MESSAGE;
      String title = "Server Storage Limit Exceeded";
      String msg = reply.message;
      String key = msgType+title;
      NotificationCenter.show(key, msgType, title, msg);
    }

    if (trace != null) trace.exit(ErrorStorageExceeded.class, null);
    return null;
  }

}