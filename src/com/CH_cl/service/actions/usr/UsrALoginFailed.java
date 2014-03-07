/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.actions.usr;

import com.CH_cl.service.actions.*;

import com.CH_co.trace.Trace;
import com.CH_co.util.*;
import com.CH_co.service.msg.dataSets.*;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.msg.dataSets.obj.Obj_List_Co;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.10 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class UsrALoginFailed extends ClientMessageAction {

  /** Creates new UsrALoginFailed */
  public UsrALoginFailed() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UsrALoginFailed.class, "UsrALoginFailed()");
    if (trace != null) trace.exit(UsrALoginFailed.class);
  }

  /** The action handler performs all actions related to the received error message (reply),
      and optionally returns a request Message.  If there is no request, null is returned.
  */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UsrALoginFailed.class, "runAction()");
    // Unlock the Reader and Writer because login sequence is done, wether successful or not.
    getClientContext().releaseLoginStreamers();
    ProtocolMsgDataSet dataSet = getMsgDataSet();

    String msg = null;
    if (dataSet instanceof Str_Rp) {
      Str_Rp strDataSet = (Str_Rp) dataSet;
      msg = strDataSet.message;
    } else if (dataSet instanceof Obj_List_Co) {
      Obj_List_Co objDataSet = (Obj_List_Co) dataSet;
      msg = (String) objDataSet.objs[0];
      String retryHandle = (String) objDataSet.objs[1];
      String[] possibleHandles = (String[]) objDataSet.objs[2];
    }

    if (msg != null && msg.length() > 0 && msg.toLowerCase().indexOf("please specify the account pass") < 0) {
      String title = "Login Failed";
      NotificationCenter.show(NotificationCenter.WARNING_MESSAGE, title, msg);
    }

    if (trace != null) trace.exit(UsrALoginFailed.class, null);
    return null;
  }

}