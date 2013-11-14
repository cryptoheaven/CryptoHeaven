/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.actions.eml;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.ops.*;

import com.CH_co.service.msg.*;
import com.CH_co.service.records.*;
import com.CH_co.service.msg.dataSets.*;
import com.CH_co.trace.Trace;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.6 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class EmlAGet extends ClientMessageAction {

  /** Creates new EmlAGet */
  public EmlAGet() {
  }

  /**
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(EmlAGet.class, "runAction(Connection)");


    EmailRecord[] emailRecords = ((Eml_Get_Rp) getMsgDataSet()).emailRecords;

    UserOps.fetchUnknownUsers(getServerInterfaceLayer(), emailRecords);

    FetchedDataCache cache = getFetchedDataCache();
    cache.addEmailRecords(emailRecords);

    if (trace != null) trace.exit(EmlAGet.class, null);
    return null;
  }

}