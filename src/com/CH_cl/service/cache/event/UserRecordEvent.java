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
import com.CH_co.service.records.UserRecord;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.9 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class UserRecordEvent extends RecordEvent {

  /** Creates new UserRecordEvent */
  public UserRecordEvent(FetchedDataCache source, UserRecord[] userRecords, int eventType) {
    super(source, userRecords, eventType);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserRecordEvent.class, "UserRecordEvent()");
    if (trace != null) trace.exit(UserRecordEvent.class);
  }

  public UserRecord[] getUserRecords() {
    return (UserRecord[]) records;
  }

}