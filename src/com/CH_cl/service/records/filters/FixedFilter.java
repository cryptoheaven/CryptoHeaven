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

import com.CH_co.trace.Trace;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;

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
 * <b>$Revision: 1.7 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class FixedFilter extends AbstractRecordFilter implements RecordFilter {

  private boolean keep;

  /** Creates new FixedFilter */
  public FixedFilter(boolean keep) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FixedFilter.class, "FixedFilter(boolean keep)");
    if (trace != null) trace.args(keep);
    this.keep = keep;
    if (trace != null) trace.exit(FixedFilter.class);
  }

  public boolean keep(Record record) {
    return keep;
  }

}