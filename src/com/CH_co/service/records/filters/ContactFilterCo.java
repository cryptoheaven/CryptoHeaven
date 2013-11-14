/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_co.service.records.filters;

import com.CH_co.trace.Trace;
import com.CH_co.service.records.*;
import com.CH_co.util.ArrayUtils;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.5 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class ContactFilterCo extends AbstractRecordFilter implements RecordFilter {

  // if not null, keeps only if folderId matches specified
  private Long keepFolderId;
  // if not null, keeps only the status specified.
  private Short[] keepStatus;
  private Boolean keepOnlineStatuses;
  // if not null, keeps only the records with spacified owner.
  private Long[] keepOwners;
  private Long[] keepContactWith;
  
  public ContactFilterCo() {
  }
  
  /** Creates new ContactFilterCo */
  public ContactFilterCo(Long keepFolderId, Short[] keepStatus, Long keepOwner) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactFilterCo.class, "ContactFilter(Long keepFolderId, Short keepStatus, Long keepOwner)");
    if (trace != null) trace.args(keepFolderId, keepStatus, keepOwner);

    this.keepFolderId = keepFolderId;
    this.keepStatus = keepStatus;
    this.keepOwners = new Long[] { keepOwner };

    if (trace != null) trace.exit(ContactFilterCo.class);
  }

  /** Creates new ContactFilterCo */
  public ContactFilterCo(Long keepFolderId, Short[] keepStatus, boolean keepOnlineStatuses, Long keepOwner) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactFilterCo.class, "ContactFilter(Long keepFolderId, Short keepStatus, boolean keepOnlineStatuses, Long keepOwner)");
    if (trace != null) trace.args(keepFolderId, keepStatus);
    if (trace != null) trace.args(keepOnlineStatuses);
    if (trace != null) trace.args(keepOwner);

    this.keepFolderId = keepFolderId;
    this.keepStatus = keepStatus;
    this.keepOnlineStatuses = Boolean.valueOf(keepOnlineStatuses);
    this.keepOwners = new Long[] { keepOwner };

    if (trace != null) trace.exit(ContactFilterCo.class);
  }

  /** Creates new ContactFilterCo */
  public ContactFilterCo(Short keepStatus, Long keepOwner) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactFilterCo.class, "ContactFilter(Short keepStatus, Long keepOwner)");
    if (trace != null) trace.args(keepStatus, keepOwner);

    this.keepStatus = new Short[] { keepStatus };
    this.keepOwners = new Long[] { keepOwner };

    if (trace != null) trace.exit(ContactFilterCo.class);
  }

  /** Creates new ContactFilterCo */
  public ContactFilterCo(Long[] keepOwners) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactFilterCo.class, "ContactFilter(Long[] keepOwners)");
    if (trace != null) trace.args(keepOwners);

    this.keepOwners = keepOwners;

    if (trace != null) trace.exit(ContactFilterCo.class);
  }

  /** Creates new ContactFilterCo */
  public ContactFilterCo(Long[] keepOwners, Long[] keepContactWith) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactFilterCo.class, "ContactFilter(Long[] keepOwners, Long[] keepContactWith)");
    if (trace != null) trace.args(keepOwners, keepContactWith);

    this.keepOwners = keepOwners;
    this.keepContactWith = keepContactWith;

    if (trace != null) trace.exit(ContactFilterCo.class);
  }

  /** Creates new ContactFilterCo */
  public ContactFilterCo(Short[] keepStatus, Long keepOwner) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ContactFilterCo.class, "ContactFilter(Short[] keepStatus, Long keepOwner)");
    if (trace != null) trace.args(keepStatus, keepOwner);

    this.keepStatus = keepStatus;
    this.keepOwners = new Long[] { keepOwner };

    if (trace != null) trace.exit(ContactFilterCo.class);
  }


  public boolean keep(Record record) {
    boolean keep = false;
    if (record instanceof ContactRecord) {
      keep = true;
      ContactRecord contact = (ContactRecord) record;
      if (keep == true && keepFolderId != null) {
        if (!keepFolderId.equals(contact.folderId)) {
          keep = false;
        }
      }
      if (keep == true && (keepStatus != null || keepOnlineStatuses != null)) {
        keep = false;
        for (int i=0; keepStatus!=null && i<keepStatus.length; i++) {
          if (contact.status.equals(keepStatus[i])) {
            keep = true;
            break;
          }
        }
        if (!keep && keepOnlineStatuses != null) {
          keep = contact.status != null && ContactRecord.isOnlineStatus(contact.status) == keepOnlineStatuses.booleanValue();
        }
      }
      if (keep == true && keepOwners != null && keepOwners.length > 0) {
        if (ArrayUtils.find(keepOwners, contact.ownerUserId) < 0)
          keep = false;
      }
      if (keep == true && keepContactWith != null && keepContactWith.length > 0) {
        if (ArrayUtils.find(keepContactWith, contact.contactWithId) < 0)
          keep = false;
      }
    }
    return keep;
  }

}