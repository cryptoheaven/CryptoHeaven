/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_cl.service.records;

import java.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2011
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
public class FolderRecUtil extends Object {

  // When folder contents is fetched, lets keep these marks here.
  private static final Hashtable fldFetchRequestsIssuedHT = new Hashtable();

  // When folder view is invalidated, lets keep these marks here.
  private static final Hashtable fldViewInvalidatedHT = new Hashtable();


  public static void markFolderFetchRequestIssued(Long folderId) {
    fldFetchRequestsIssuedHT.put(folderId, Boolean.TRUE);
  }
  public static boolean wasFolderFetchRequestIssued(Long folderId) {
    Boolean b = Boolean.FALSE;
    if (folderId != null)
      b = (Boolean) fldFetchRequestsIssuedHT.get(folderId);
    return b != null ? b.booleanValue() : false;
  }

  public static void markFolderViewInvalidated(Long folderId, boolean isInvalidated) {
    fldViewInvalidatedHT.put(folderId, Boolean.valueOf(isInvalidated));
  }
  public static boolean wasFolderViewInvalidated(Long folderId) {
    Boolean b = Boolean.FALSE;
    if (folderId != null)
      b = (Boolean) fldViewInvalidatedHT.get(folderId);
    return b != null ? b.booleanValue() : false;
  }

  public static void clear() {
    fldFetchRequestsIssuedHT.clear();
    fldViewInvalidatedHT.clear();
  }

  public static void clearFetchedIDs() {
    fldFetchRequestsIssuedHT.clear();
  }

}