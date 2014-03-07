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
import com.CH_co.service.records.UserSettingsRecord;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.3 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class UserSettingsRecordEvent extends RecordEvent {

  /** Creates new UserSettingsRecordEvent */
  public UserSettingsRecordEvent(FetchedDataCache source, UserSettingsRecord[] userSettingsRecords, int eventType) {
    super(source, userSettingsRecords, eventType);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserSettingsRecordEvent.class, "UserSettingsRecordEvent()");
    if (trace != null) trace.exit(UserSettingsRecordEvent.class);
  }

  public UserSettingsRecord[] getUserSettingRecords() {
    return (UserSettingsRecord[]) records;
  }

}