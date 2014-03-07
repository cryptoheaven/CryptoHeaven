/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
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
import com.CH_co.service.records.InvEmlRecord;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * Class Description:
 *
 * Class Details:
 *
 * <b>$Revision: 1.8 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class InvEmlRecordEvent extends RecordEvent {

  /** Creates new InvEmlRecordEvent */
  public InvEmlRecordEvent(FetchedDataCache source, InvEmlRecord[] invEmlRecords, int eventType) {
    super(source, invEmlRecords, eventType);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(InvEmlRecordEvent.class, "InvEmlRecordEvent()");
    if (trace != null) trace.exit(InvEmlRecordEvent.class);
  }

  public InvEmlRecord[] getInvEmlRecords() {
    return (InvEmlRecord[]) records;
  }

}