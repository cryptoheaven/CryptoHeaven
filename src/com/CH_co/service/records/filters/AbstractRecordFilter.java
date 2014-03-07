/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.records.filters;

import com.CH_co.service.records.*;
import com.CH_co.util.*;

import java.util.*;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.8 $</b>
 *
 * @author  Marcin Kurzawa
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
    ArrayList recsL = new ArrayList();
    Record[] keepRecs = null;
    if (recs != null) {
      for (int i=0; i<recs.length; i++) {
        if (keep(recs[i]) == isKeep)
          recsL.add(recs[i]);
      }
      keepRecs = (Record[]) ArrayUtils.toArray(recsL, recs.getClass().getComponentType());
    }
    return keepRecs;
  }

  abstract public boolean keep(Record record);

}