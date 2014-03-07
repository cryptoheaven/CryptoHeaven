/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.actions.eml;

import com.CH_cl.service.actions.*;

import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.records.*;
import com.CH_co.service.msg.dataSets.*;
import com.CH_co.service.msg.dataSets.obj.*;

import com.CH_co.trace.Trace;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.4 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class EmlAGetDomains extends ClientMessageAction {

  /** Creates new EmlAGetDomains */
  public EmlAGetDomains() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(EmlAGetDomains.class, "EmlAGetDomains()");
    if (trace != null) trace.exit(EmlAGetDomains.class);
  }

  /**
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(EmlAGetDomains.class, "runAction(Connection)");

    Obj_List_Co reply = (Obj_List_Co) getMsgDataSet();

    EmailRecord[] emailRecords = reply.objs.length > 1 ? ((Eml_Get_Rp) reply.objs[1]).emailRecords : null;

    getFetchedDataCache().addEmailRecords(emailRecords);

    if (trace != null) trace.exit(EmlAGetDomains.class, null);
    return null;
  }

}