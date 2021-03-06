/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
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
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
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
public class ContactFilterCl extends AbstractRecordFilter implements RecordFilter {

  private FetchedDataCache cache;
  private Boolean keepIncoming;
  
//  /**
//   * Creates new ContactFilterCl for incoming contacts (other's contacts with you)
//   * Outgoing Contacts are kept too.
//   */
//  public ContactFilterCl(Long keepFolderId, Long keepOwner, boolean keepIncoming) {
//    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactFilterCl.class, "ContactFilter(Long keepFolderId, Long keepOwner, boolean keepIncoming)");
//    if (trace != null) trace.args(keepFolderId);
//    if (trace != null) trace.args(keepIncoming);
//
//    super.keepFolderId = keepFolderId;
//    super.keepOwners = new Long[] { keepOwner };
//    this.keepIncoming = Boolean.valueOf(keepIncoming);
//
//    if (trace != null) trace.exit(ContactFilterCl.class);
//  }
  
  /** 
   * Creates new ContactFilterCl for incoming contacts (other's contacts with you) 
   * Outgoing Contacts are kept too.
   */
  public ContactFilterCl(final FetchedDataCache cache, boolean keepIncoming) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactFilterCl.class, "ContactFilter(boolean keepIncoming)");
    if (trace != null) trace.args(keepIncoming);

    this.cache = cache;
    this.keepIncoming = Boolean.valueOf(keepIncoming);

    if (trace != null) trace.exit(ContactFilterCl.class);
  }
  
  
  public boolean keep(Record record) {
    boolean keep = false;
    if (record instanceof ContactRecord) {
//      keep = super.keep(record);
      
      ContactRecord contact = (ContactRecord) record;

      Long myUserId = cache.getMyUserId();
      keep = contact.ownerUserId.equals(myUserId) || contact.contactWithId.equals(myUserId);
      if (keep == true && keepIncoming != null && keepIncoming.booleanValue() == false) {
        if (!contact.ownerUserId.equals(myUserId)) {
          if (contact.status == null || !contact.isOfInitiatedType())
            keep = false;
        }
      }
    }
    return keep;
  }

}