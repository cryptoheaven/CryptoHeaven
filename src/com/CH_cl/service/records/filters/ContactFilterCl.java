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

package com.CH_cl.service.records.filters;

import com.CH_cl.service.cache.FetchedDataCache;

import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.trace.Trace;

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
 * <b>$Revision: 1.5 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class ContactFilterCl extends ContactFilterCo {

  private Boolean keepIncoming;
  
  /** 
   * Creates new ContactFilterCl for incoming contacts (other's contacts with you) 
   * Outgoing Contacts are kept too.
   */
  public ContactFilterCl(Long keepFolderId, boolean keepIncoming) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactFilterCl.class, "ContactFilter(Long keepFolderId, boolean keepIncoming)");
    if (trace != null) trace.args(keepFolderId);
    if (trace != null) trace.args(keepIncoming);

    super.keepFolderId = keepFolderId;
    this.keepIncoming = Boolean.valueOf(keepIncoming);

    if (trace != null) trace.exit(ContactFilterCl.class);
  }
  
  /** 
   * Creates new ContactFilterCl for incoming contacts (other's contacts with you) 
   * Outgoing Contacts are kept too.
   */
  public ContactFilterCl(boolean keepIncoming) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactFilterCl.class, "ContactFilter(boolean keepIncoming)");
    if (trace != null) trace.args(keepIncoming);

    this.keepIncoming = Boolean.valueOf(keepIncoming);

    if (trace != null) trace.exit(ContactFilterCl.class);
  }
  
  
  public boolean keep(Record record) {
    boolean keep = false;
    if (record instanceof ContactRecord) {
      keep = super.keep(record);
      
      ContactRecord contact = (ContactRecord) record;

      if (keep == true && keepIncoming != null && keepIncoming.booleanValue() == false) {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        if (!contact.ownerUserId.equals(cache.getMyUserId())) {
          if (contact.status == null || contact.status.shortValue() != ContactRecord.STATUS_INITIATED)
            keep = false;
        }
      }
    }
    return keep;
  }

}