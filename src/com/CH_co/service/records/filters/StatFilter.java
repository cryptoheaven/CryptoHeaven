/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.records.filters;

import com.CH_co.service.records.Record;
import com.CH_co.service.records.StatRecord;
import com.CH_co.service.records.filters.AbstractRecordFilter;
import com.CH_co.service.records.filters.RecordFilter;
import com.CH_co.trace.Trace;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.12 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class StatFilter extends AbstractRecordFilter implements RecordFilter {

  // Keep only the stats for this object id.
  private Long keepObjId;

  // Keep only the stats for this ownerUserId.
  private Long keepOwnerUserId;

  /** Creates new StatFilter */
  public StatFilter(Long keepObjId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(StatFilter.class, "StatFilter(Long keepObjId)");
    if (trace != null) trace.args(keepObjId);
    this.keepObjId = keepObjId;
    if (trace != null) trace.exit(StatFilter.class);
  }
  /** Creates new StatFilter */
  public StatFilter(Long keepObjId, Long keepOwnerUserId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(StatFilter.class, "StatFilter(Long keepObjId, keepOwnerUserId)");
    if (trace != null) trace.args(keepObjId, keepOwnerUserId);
    this.keepObjId = keepObjId;
    this.keepOwnerUserId = keepOwnerUserId;
    if (trace != null) trace.exit(StatFilter.class);
  }

  public boolean keep(Record record) {
    boolean keep = false;

    if (record instanceof StatRecord) {
      StatRecord statRecord = (StatRecord) record;
      if (keepObjId != null) {
        if (statRecord.objId.equals(keepObjId)) {
          keep = true;
        }
      }
      if (keepOwnerUserId != null) {
        if (statRecord.ownerUserId.equals(keepOwnerUserId)) {
          keep = true;
        }
      }
    }

    return keep;
  }

}