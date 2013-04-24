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

import java.util.EventObject;

import com.CH_cl.service.cache.*;

import com.CH_co.trace.Trace;
import com.CH_co.service.records.Record;
import com.CH_co.util.Misc;

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
 * <b>$Revision: 1.7 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class RecordEvent extends EventObject {

  public static final int SET = 1;
  public static final int REMOVE = 2;
  public static final int FOLDER_FETCH_COMPLETED  = 3;
  public static final int FOLDER_FETCH_INTERRUPTED  = 4;

  Record[] records;
  int eventType;

  /** Creates new RecordEvent */
  public RecordEvent(FetchedDataCache source, Record[] records, int eventType) {
    super(source);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordEvent.class, "RecordEvent()");
    if (trace != null) trace.data(10, "source", source);
    if (trace != null) trace.data(11, "eventType", eventType);
    if (trace != null) trace.data(12, "records[]", records);
    this.records = records;
    this.eventType = eventType;
    if (trace != null) trace.exit(RecordEvent.class);
  }


  public Record[] getRecords() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordEvent.class, "getRecords()");
    if (trace != null) trace.exit(RecordEvent.class);
    return records;
  }

  public int getEventType() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordEvent.class, "getEventType()");
    if (trace != null) trace.exit(RecordEvent.class, eventType);
    return eventType;
  }

  public String toString() {
    return "[RecordEvent"
      + ": eventType="        + eventType
      + ", records="          + Misc.objToStr(records)
      + "]";
  }
}