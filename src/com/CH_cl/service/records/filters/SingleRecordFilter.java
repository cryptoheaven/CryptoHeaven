/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_cl.service.records.filters;

import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.trace.Trace;

/**
 * <b>Copyright</b> &copy; 2001-2009
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
 * <b>$Revision: $</b>
 * @author  Marcin Kurzawa
 * @version
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