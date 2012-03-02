/*
 * Copyright 2001-2012 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_co.service.records.filters;

import com.CH_co.trace.Trace;
import com.CH_co.service.records.*;

/**
 * <b>Copyright</b> &copy; 2001-2012
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
 * <b>$Revision: 1.2 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class MultiFilter extends AbstractRecordFilter implements RecordFilter {

  public static int AND = 1;
  public static int OR = 2;

  private RecordFilter[] filters;
  private int matchType;

  /** Creates new MultiFilter */
  public MultiFilter(RecordFilter[] filters, int matchType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MultiFilter.class, "MultiFilter()");
    this.filters = filters;
    this.matchType = matchType;
    if (trace != null) trace.exit(MultiFilter.class);
  }
  /** Creates new MultiFilter */
  public MultiFilter(RecordFilter filter1, RecordFilter filter2, int matchType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MultiFilter.class, "MultiFilter()");
    this.filters = new RecordFilter[] { filter1, filter2 };
    this.matchType = matchType;
    if (trace != null) trace.exit(MultiFilter.class);
  }

  public boolean keep(Record record) {
    boolean keep = false;
    for (int i=0; i<filters.length; i++) {
      keep = filters[i].keep(record);
      if (keep && matchType == OR)
        break;
      else if (!keep && matchType == AND)
        break;
    }
    return keep;
  }

}