/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
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
import com.CH_co.service.records.StatRecord;

/** 
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: 
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.8 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class StatRecordEvent extends RecordEvent {

  /** Creates new StatRecordEvent */
  public StatRecordEvent(FetchedDataCache source, StatRecord[] statRecords, int eventType) {
    super(source, statRecords, eventType);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(StatRecordEvent.class, "StatRecordEvent()");
    if (trace != null) trace.exit(StatRecordEvent.class);
  }

  public StatRecord[] getStatRecords() {
    return (StatRecord[]) records;
  }

}