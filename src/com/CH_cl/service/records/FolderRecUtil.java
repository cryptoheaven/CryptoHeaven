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

package com.CH_cl.service.records;

import java.awt.Component;
import java.util.*;

import com.CH_cl.service.cache.*;

import com.CH_co.service.records.*;
import com.CH_co.util.MultiHashtable;

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
 * <b>$Revision: 1.6 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class FolderRecUtil extends Object {

  // When folder contents is fetched, lets keep these marks here.
  private static final Hashtable fldFetchRequestsIssuedHT = new Hashtable();

  // When folder view is invalidated, lets keep these marks here.
  private static final Hashtable fldViewInvalidatedHT = new Hashtable();
  
  // When update comes to the chatting folder, keep track of open Chat Frames or chat Components
  private static final MultiHashtable openChatFolders = new MultiHashtable();


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

  public static Vector getOpenChatFolders(Long folderId) {
    Vector v = null;
    synchronized (openChatFolders) {
      v = openChatFolders.getAll(folderId);
    }
    return v;
  }
  public static boolean isOpenChatFolder(Long folderId) {
    boolean rc = false;
    synchronized (openChatFolders) {
      rc = openChatFolders.get(folderId) != null;
    }
    return rc;
  }
  public static void setOpenChatFolder(Long folderId, Component comp) {
    synchronized (openChatFolders) {
      openChatFolders.put(folderId, comp);
    }
  }
  public static void clearOpenChatFolder(Long folderId, Component comp) {
    synchronized (openChatFolders) {
      openChatFolders.remove(folderId, comp);
    }
  }

  public static boolean isMySuperRoot(FolderRecord folderRecord) {
    UserRecord userRec = FetchedDataCache.getSingleInstance().getUserRecord();
    return folderRecord.isSuperRoot(userRec);
  }

  public static void clear() {
    fldFetchRequestsIssuedHT.clear();
    fldViewInvalidatedHT.clear();
    openChatFolders.clear();
  }

  public static void clearFetchedIDs() {
    fldFetchRequestsIssuedHT.clear();
  }

}