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

package com.CH_cl.service.records;

import java.util.*;

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
public class FolderRecUtil extends Object {

  // When folder contents is fetched, lets keep these folderIDs here.
  private static final HashSet fldFetchRequestsIssuedHT = new HashSet();

  // When folder view is invalidated, lets keep these marks here.
  private static final Hashtable fldViewInvalidatedHT = new Hashtable();

  public static synchronized List getFoldersFetched() {
    ArrayList list = new ArrayList();
    Iterator iter = fldFetchRequestsIssuedHT.iterator();
    while (iter.hasNext()) {
      Long folderId = (Long) iter.next();
      list.add(folderId);
    }
    return list;
  }

  public static synchronized List getFoldersFetchedAndInvalidated() {
    ArrayList list = new ArrayList();
    Iterator iter = fldFetchRequestsIssuedHT.iterator();
    while (iter.hasNext()) {
      Long folderId = (Long) iter.next();
      if (wasFolderViewInvalidated(folderId))
        list.add(folderId);
    }
    return list;
  }

  public static synchronized void markFolderFetchRequestIssued(Long folderId) {
    fldFetchRequestsIssuedHT.add(folderId);
  }
  public static synchronized boolean wasFolderFetchRequestIssued(Long folderId) {
    return folderId != null && fldFetchRequestsIssuedHT.contains(folderId);
  }

  public static synchronized void markFolderViewInvalidated(Long folderId, boolean isInvalidated) {
    fldViewInvalidatedHT.put(folderId, Boolean.valueOf(isInvalidated));
  }
  public static synchronized boolean wasFolderViewInvalidated(Long folderId) {
    Boolean b = Boolean.FALSE;
    if (folderId != null)
      b = (Boolean) fldViewInvalidatedHT.get(folderId);
    return b != null ? b.booleanValue() : false;
  }

  public static synchronized void clear() {
    fldFetchRequestsIssuedHT.clear();
    fldViewInvalidatedHT.clear();
  }

  public static synchronized void clearFetchedIDs() {
    fldFetchRequestsIssuedHT.clear();
  }

}