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
 * <b>$Revision: 1.12 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class StatFilter extends AbstractRecordFilter implements RecordFilter {

  // Keep only the stats for this object id.
  private Long keepObjId;

  /** Creates new StatFilter */
  public StatFilter(Long keepObjId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(StatFilter.class, "StatFilter(Long keepObjId)");
    if (trace != null) trace.args(keepObjId);

    this.keepObjId = keepObjId;

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
    }

    return keep;
  }

}