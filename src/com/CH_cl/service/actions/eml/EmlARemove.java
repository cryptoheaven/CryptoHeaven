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

import com.CH_co.trace.Trace;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.records.*;
import com.CH_co.service.msg.dataSets.obj.*;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.6 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class EmlARemove extends ClientMessageAction {

  /** Creates new EmlARemove */
  public EmlARemove() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(EmlARemove.class, "EmlARemove()");
    if (trace != null) trace.exit(EmlARemove.class);
  }

  /**
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(EmlARemove.class, "runAction(Connection)");

    Obj_IDList_Co reply = (Obj_IDList_Co) getMsgDataSet();
    Long[] emlIDs = reply.IDs;

    getFetchedDataCache().removeEmailRecords(emlIDs);

    if (trace != null) trace.exit(EmlARemove.class, null);
    return null;
  }

}