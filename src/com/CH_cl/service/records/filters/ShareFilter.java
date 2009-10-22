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

import java.util.*;

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
 * <b>$Revision: 1.13 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class ShareFilter extends AbstractRecordFilter implements RecordFilter {

  // if not null, keep only the records with specified folderId
  private Long keepFolderId;
  private Long keepOwnerUserId;
  private Hashtable keepOwnerGroupIDsHT;

  /** Creates new ShareFilter */
  public ShareFilter(Long keepFolderId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ShareFilter.class, "ShareFilter(Long keepFolderId)");
    if (trace != null) trace.args(keepFolderId);
    this.keepFolderId = keepFolderId;
    if (trace != null) trace.exit(ShareFilter.class);
  }
  /** Creates new ShareFilter */
  public ShareFilter(Long keepFolderId, Long keepOwnerUserId, Hashtable keepOwnerGroupIDsHT) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ShareFilter.class, "ShareFilter(Long keepFolderId, Long keepOwnerUserId, Hashtable keepOwnerGroupIDsHT)");
    if (trace != null) trace.args(keepFolderId);
    if (trace != null) trace.args(keepOwnerUserId);
    if (trace != null) trace.args(keepOwnerGroupIDsHT);
    this.keepFolderId = keepFolderId;
    this.keepOwnerUserId = keepOwnerUserId;
    this.keepOwnerGroupIDsHT = keepOwnerGroupIDsHT;
    if (trace != null) trace.exit(ShareFilter.class);
  }


  public boolean keep(Record record) {
    boolean keep = false;
    if (record instanceof FolderShareRecord) {
      keep = true;
      FolderShareRecord share = (FolderShareRecord) record;
      if (keepFolderId != null) {
        if (!share.folderId.equals(keepFolderId))
          keep = false;
      }
      if (keep && (keepOwnerUserId != null || keepOwnerGroupIDsHT != null)) {
        keep = share.isOwnedBy(keepOwnerUserId, keepOwnerGroupIDsHT);
      }
    }
    return keep;
  }
}