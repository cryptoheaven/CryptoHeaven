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
import com.CH_co.service.records.MsgLinkRecord;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.8 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class MsgLinkRecordEvent extends RecordEvent {

  /** Creates new MsgLinkRecordEvent */
  public MsgLinkRecordEvent(FetchedDataCache source, MsgLinkRecord[] linkRecords, int eventType) {
    super(source, linkRecords, eventType);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MsgLinkRecordEvent.class, "MsgLinkRecordEvent()");
    if (trace != null) trace.exit(MsgLinkRecordEvent.class);
  }

  public MsgLinkRecord[] getMsgLinkRecords() {
    return (MsgLinkRecord[]) records;
  }

}