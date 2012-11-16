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
  private static final HashSet fldIDsFetchRequestsIssuedHS = new HashSet();

  // When folder view is invalidated, lets keep these marks here.
  private static final Hashtable fldIDsViewInvalidatedHT = new Hashtable();

  public static synchronized List getFolderIDsFetched() {
    ArrayList list = new ArrayList();
    Iterator iter = fldIDsFetchRequestsIssuedHS.iterator();
    while (iter.hasNext()) {
      Long folderId = (Long) iter.next();
      list.add(folderId);
    }
    return list;
  }

  public static synchronized List getFolderIDsFetchedAndInvalidated() {
    ArrayList list = new ArrayList();
    Iterator iter = fldIDsFetchRequestsIssuedHS.iterator();
    while (iter.hasNext()) {
      Long folderId = (Long) iter.next();
      if (wasFolderViewInvalidated(folderId))
        list.add(folderId);
    }
    return list;
  }

  public static synchronized void markFolderFetchRequestIssued(Long folderId) {
    fldIDsFetchRequestsIssuedHS.add(folderId);
  }

  public static synchronized boolean wasFolderFetchRequestIssued(Long folderId) {
    return folderId != null && fldIDsFetchRequestsIssuedHS.contains(folderId);
  }

  public static synchronized void markFolderViewInvalidated(Long folderId, boolean isInvalidated) {
    fldIDsViewInvalidatedHT.put(folderId, Boolean.valueOf(isInvalidated));
  }

  public static synchronized void markFolderViewInvalidated(Collection folderIDsL, boolean isInvalidated) {
    Boolean flag = Boolean.valueOf(isInvalidated);
    Iterator iter = folderIDsL.iterator();
    while (iter.hasNext()) {
      fldIDsViewInvalidatedHT.put(iter.next(), flag);
    }
  }

  public static synchronized boolean wasFolderViewInvalidated(Long folderId) {
    Boolean b = Boolean.FALSE;
    if (folderId != null)
      b = (Boolean) fldIDsViewInvalidatedHT.get(folderId);
    return b != null ? b.booleanValue() : false;
  }

  public static synchronized void clear() {
    fldIDsFetchRequestsIssuedHS.clear();
    fldIDsViewInvalidatedHT.clear();
  }

  public static synchronized void clearFetchedIDs() {
    fldIDsFetchRequestsIssuedHS.clear();
  }

}