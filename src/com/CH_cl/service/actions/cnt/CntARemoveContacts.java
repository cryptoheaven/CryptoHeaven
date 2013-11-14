/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.actions.cnt;

import java.util.Vector;

import com.CH_co.trace.Trace;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.FetchedDataCache;

import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.cnt.*;
import com.CH_co.service.records.*;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.10 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class CntARemoveContacts extends ClientMessageAction {

  /** Creates new CntARemoveContacts */
  public CntARemoveContacts() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CntARemoveContacts.class, "CntARemoveContacts()");
    if (trace != null) trace.exit(CntARemoveContacts.class);
  }

  /**
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CntARemoveContacts.class, "runAction(Connection)");

    ContactRecord[] contactRecords = ((Cnt_GetCnts_Rp) getMsgDataSet()).contactRecords;
    FetchedDataCache cache = getFetchedDataCache();
    Vector contactsToRemoveV = new Vector();
    // Find records we are about to remove so that we can display a message with its name.
    for (int i=0; i<contactRecords.length; i++) {
      ContactRecord cRec = contactRecords[i];
      ContactRecord cRecOld = cache.getContactRecord(contactRecords[i].contactId);
      if (cRecOld != null) {
        cRec.merge(cRecOld);
        contactsToRemoveV.addElement(cRec);
      }
    }
    cache.removeContactRecords(contactRecords);

    if (trace != null) trace.exit(CntARemoveContacts.class, null);
    return null;
  }

}