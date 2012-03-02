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

import com.CH_cl.service.cache.FetchedDataCache;

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
 * <b>$Revision: 1.6 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class InvEmlFilter extends AbstractRecordFilter implements RecordFilter {

  private boolean onlySelfSent;
  private boolean includeRemoved;

  /** Creates new InvEmlFilter */
  public InvEmlFilter(boolean onlySelfSent, boolean includeRemoved) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(InvEmlFilter.class, "InvEmlFilter(boolean onlySelfSent, boolean includeRemoved)");
    this.onlySelfSent = onlySelfSent;
    this.includeRemoved = includeRemoved;
    if (trace != null) trace.exit(InvEmlFilter.class);
  }

  public boolean keep(Record record) {
    boolean keep = false;

    if (record instanceof InvEmlRecord) {
      InvEmlRecord invEmlRecord = (InvEmlRecord) record;
      if (includeRemoved || !invEmlRecord.removed.booleanValue()) {
        if (onlySelfSent) {
          FetchedDataCache cache = FetchedDataCache.getSingleInstance();
          Long userId = cache.getMyUserId();
          if (userId != null)
            keep = userId.equals(invEmlRecord.sentByUID);
        } else {
          keep = true;
        }
      }
    }

    return keep;
  }

}