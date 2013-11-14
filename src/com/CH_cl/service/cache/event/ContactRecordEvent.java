/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.cache.event;

import com.CH_cl.service.cache.*;

import com.CH_co.trace.Trace;
import com.CH_co.service.records.ContactRecord;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.8 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class ContactRecordEvent extends RecordEvent {

  /** Creates new ContactRecordEvent */
  public ContactRecordEvent(FetchedDataCache source, ContactRecord[] contactRecords, int eventType) {
    super(source, contactRecords, eventType);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactRecordEvent.class, "ContactRecordEvent()");
    if (trace != null) trace.exit(ContactRecordEvent.class);
  }

  public ContactRecord[] getContactRecords() {
    return (ContactRecord[]) records;
  }

}