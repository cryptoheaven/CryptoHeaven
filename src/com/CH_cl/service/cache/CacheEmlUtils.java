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

package com.CH_cl.service.cache;

import com.CH_cl.service.records.EmailAddressRecord;
import com.CH_cl.service.records.filters.FolderFilter;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import java.util.HashSet;

/** 
* <b>Copyright</b> &copy; 2001-2012
* <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
* CryptoHeaven Corp.
* </a><br>All rights reserved.<p>
*
* Class Description:
*  Utilities to work with cache and records.
*
* Class Details:
*
*
* <b>$Revision: 1.14 $</b>
* @author  Marcin Kurzawa
* @version
*/
public class CacheEmlUtils extends Object {

  /** Hide the constructor, all methods are static. */
  private CacheEmlUtils() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CacheEmlUtils.class, "CacheUtilities()");
    if (trace != null) trace.exit(CacheEmlUtils.class);
  }

  /**
  * Familiar are those in AddressBooks, not in WhiteLists, first the first match, ignore others.
  */
  public static Record convertToFamiliarEmailRecord(String emailAddress) {
    Record familiar;
    MsgDataRecord addrRecord = null;
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    MsgDataRecord[] addrRecords = cache.getAddrRecords(emailAddress);
    Long[] addrIDs = RecordUtils.getIDs(addrRecords);
    if (addrRecords != null && addrRecords.length > 0) {
      // we know that something like that exists, now find it from ADDRESS BOOKS only
      UserRecord uRec = cache.getUserRecord();
      // First look in the primary Address Book
      FolderRecord addrBook = cache.getFolderRecord(uRec.addrFolderId);
      Long addrBookId = addrBook.folderId;
      MsgLinkRecord[] addrLinks = cache.getMsgLinkRecordsForMsgs(addrIDs);
      if (addrLinks != null && addrLinks.length > 0) {
        // See which of those links is in our main address book
        for (int i=0; i<addrLinks.length; i++) {
          MsgLinkRecord addrLink = addrLinks[i];
          if (addrLink.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER && addrLink.ownerObjId.equals(addrBookId)) {
            addrRecord = cache.getMsgDataRecord(addrLink.msgId);
            break;
          }
        }
        if (addrRecord == null) {
          // If nothing found, look in all Address Books
          // Prep all address book lookup table
          FolderRecord[] addrBooks = cache.getFolderRecords(new FolderFilter(FolderRecord.ADDRESS_FOLDER));
          HashSet addrBookIDs = new HashSet();
          for (int i=0; i<addrBooks.length; i++)
            addrBookIDs.add(addrBooks[i].folderId);
          // Look for matching links
          for (int i=0; i<addrLinks.length; i++) {
            MsgLinkRecord addrLink = addrLinks[i];
            if (addrLink.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER && addrBookIDs.contains(addrLink.ownerObjId)) {
              addrRecord = cache.getMsgDataRecord(addrLink.msgId);
              break;
            }
          }
        }
      }
    }
    familiar = addrRecord;
    if (familiar == null)
      familiar = new EmailAddressRecord(emailAddress);
    return familiar;
  }

}