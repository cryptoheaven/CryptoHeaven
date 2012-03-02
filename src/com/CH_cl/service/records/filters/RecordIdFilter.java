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

package com.CH_cl.service.records.filters;

import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.trace.Trace;

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
 * <b>$Revision: 1.3 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class RecordIdFilter extends AbstractRecordFilter implements RecordFilter {

  private Long id;

  /** Creates new RecordIdFilter */
  public RecordIdFilter(Long id) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordIdFilter.class, "RecordIdFilter()");
    this.id = id;
    if (trace != null) trace.exit(RecordIdFilter.class);
  }

  public boolean keep(Record record) {
    return id.equals(record.getId());
  }

}