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
import com.CH_cl.service.cache.*;

import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.usr.*;
import com.CH_co.trace.Trace;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.1 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class UsrAPassRecoveryGetComplete extends ClientMessageAction {

  /** Creates new UsrAPassRecoveryGetComplete */
  public UsrAPassRecoveryGetComplete() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UsrAPassRecoveryGetComplete.class, "UsrAPassRecoveryGetComplete()");
    if (trace != null) trace.exit(UsrAPassRecoveryGetComplete.class);
  }

  /**
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UsrAPassRecoveryGetComplete.class, "runAction(Connection)");

    FetchedDataCache cache = getFetchedDataCache();
    Usr_PassRecovery_Co reply = (Usr_PassRecovery_Co) getMsgDataSet();
    cache.setPassRecoveryRecord(reply.passRecoveryRecord);

    if (trace != null) trace.exit(UsrAPassRecoveryGetComplete.class, null);
    return null;
  }

}