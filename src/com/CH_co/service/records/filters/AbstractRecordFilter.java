/*
 * Copyright 2001-2010 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_co.service.records.filters;

import java.lang.reflect.*;
import java.util.*;

import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2010
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
 * <b>$Revision: 1.8 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
abstract public class AbstractRecordFilter extends Object implements RecordFilter {

  /**
   * Filter records and return an array of runtime instance of the source elements.
   */
  public Record[] filterInclude(Record[] recs) {
    return filter(recs, true);
  }
  public Record[] filterExclude(Record[] recs) {
    return filter(recs, false);
  }
  private Record[] filter(Record[] recs, boolean isKeep) {
    Vector recsV = new Vector();
    Record[] keepRecs = null;
    if (recs != null) {
      for (int i=0; i<recs.length; i++) {
        if (keep(recs[i]) == isKeep)
          recsV.addElement(recs[i]);
      }
      keepRecs = (Record[]) ArrayUtils.toArray(recsV, recs.getClass().getComponentType());
    }
    return keepRecs;
  }

  abstract public boolean keep(Record record);

}