/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_cl.service.cache.event;

import com.CH_cl.service.cache.*;

import com.CH_co.trace.Trace;
import com.CH_co.service.records.EmailRecord;


/**
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.7 $</b>
 * @author  Marcin Kurzawa
 * @version
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