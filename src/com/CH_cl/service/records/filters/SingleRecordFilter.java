/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.records.filters;

import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.trace.Trace;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class SingleRecordFilter extends AbstractRecordFilter implements RecordFilter {

  private Record rec;

  /** Creates new SingleRecordFilter */
  public SingleRecordFilter(Record record) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SingleRecordFilter.class, "SingleRecordFilter()");
    rec = record;
    if (trace != null) trace.exit(SingleRecordFilter.class);
  }

  public boolean keep(Record record) {
    return rec.equals(record);
  }

}