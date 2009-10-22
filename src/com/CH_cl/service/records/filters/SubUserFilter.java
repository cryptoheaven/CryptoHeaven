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

import com.CH_cl.service.cache.FetchedDataCache;

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
 * <b>$Revision: 1.6 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class SubUserFilter extends AbstractRecordFilter implements RecordFilter {

  // keep only the user records which are children of the parent
  private Long parentUserId;
  private boolean includeSelf;
  private boolean includeAllLevels;


  /** Creates new SubUserFilter */
  public SubUserFilter(Long parentUserId, boolean includeSelf, boolean includeAllLevels) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(SubUserFilter.class, "SubUserFilter(UserRecord parentUser, boolean includeSelf, boolean includeAllLevels)");
    this.parentUserId = parentUserId;
    this.includeSelf = includeSelf;
    this.includeAllLevels = includeAllLevels;
    if (trace != null) trace.exit(SubUserFilter.class);
  }

  public boolean keep(Record record) {
    boolean keep = false;

    if (record instanceof UserRecord) {
      UserRecord userRecord = (UserRecord) record;
      if (parentUserId.equals(userRecord.userId)) {
        if (includeSelf)
          keep = true;
      } else if (parentUserId.equals(userRecord.parentId)) {
        keep = true;
      } else if (parentUserId.equals(userRecord.masterId))
        if (includeAllLevels) {
          keep = true;
      } else if (userRecord.parentId == null || userRecord.parentId.equals(userRecord.userId)) {
        keep = false;
      } else if (includeAllLevels) {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        UserRecord parentUser = cache.getUserRecord(userRecord.parentId);
        if (parentUser != null)
          keep = keep(parentUser);
      }
    }

    return keep;
  }

}