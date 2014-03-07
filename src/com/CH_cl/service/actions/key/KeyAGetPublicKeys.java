/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.actions.key;

import com.CH_cl.service.actions.*;

import com.CH_co.trace.Trace;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.records.KeyRecord;
import com.CH_co.service.msg.dataSets.key.*;


/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class KeyAGetPublicKeys extends ClientMessageAction {

  /** Creates new KeyAGetPublicKeys */
  public KeyAGetPublicKeys() {
  }

  /** The action handler performs all actions related to the received message (reply),
      and optionally returns a request Message.  If there is no request, null is returned.
  */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(KeyAGetKeyPairs.class, "runAction(Connection)");

    KeyRecord[] keyRecords = ((Key_PubKeys_Rp) getMsgDataSet()).keyRecords;
    getFetchedDataCache().addKeyRecords(keyRecords);

    if (trace != null) trace.exit(KeyAGetKeyPairs.class, null);
    return null;
  }

}