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
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    Record familiar;
    MsgDataRecord[] addrRecords = cache.getAddrRecords(emailAddress);
    MsgDataRecord addrRecord = addrRecords != null && addrRecords.length > 0 ? addrRecords[0] : null;
    if (addrRecord != null) {
      // we know that something like that exists, now find it from ADDRESS BOOKS only
      FolderRecord[] addrBooks = cache.getFolderRecords(new FolderFilter(FolderRecord.ADDRESS_FOLDER));
      MsgLinkRecord[] addrLinks = cache.getMsgLinkRecordsForFolders(RecordUtils.getIDs(addrBooks));
      Long[] addrIDs = MsgLinkRecord.getMsgIDs(addrLinks);
      addrRecords = cache.getAddrRecords(emailAddress, addrIDs);
      addrRecord = addrRecords != null ? addrRecords[0] : null;
    }
    familiar = addrRecord;
    if (familiar == null)
      familiar = new EmailAddressRecord(emailAddress);
    return familiar;
  }

}