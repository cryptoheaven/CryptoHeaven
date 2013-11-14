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
import com.CH_co.service.records.EmailRecord;


/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.7 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class EmailRecordEvent extends RecordEvent {

  /** Creates new EmailRecordEvent */
  public EmailRecordEvent(FetchedDataCache source, EmailRecord[] records, int eventType) {
    super(source, records, eventType);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(EmailRecordEvent.class, "EmailRecordEvent()");
    if (trace != null) trace.exit(EmailRecordEvent.class);
  }

  public EmailRecord[] getEmailRecords() {
    return (EmailRecord[]) records;
  }

}