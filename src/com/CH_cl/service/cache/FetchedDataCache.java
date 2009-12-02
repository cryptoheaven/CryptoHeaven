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

package com.CH_cl.service.cache;

import java.security.*;
import java.util.*;
import javax.swing.event.EventListenerList;

import com.CH_co.util.*;
import com.CH_co.trace.Trace;
import com.CH_co.cryptx.*;

import com.CH_cl.service.cache.event.*;
import com.CH_cl.service.records.*;
import com.CH_cl.service.records.filters.*;
import com.CH_cl.util.GlobalSubProperties;

import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.service.msg.dataSets.obj.*;

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
 * <b>$Revision: 1.55 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class FetchedDataCache extends Object {

  // in production set DEBUG to false
  public static final boolean DEBUG__SUPPRESS_EVENTS_STATS = false;
  public static final boolean DEBUG__SUPPRESS_EVENTS_MSGS = false;
  public static final boolean DEBUG__SUPPRESS_EVENTS_FILES = false;
  public static final boolean DEBUG__SUPPRESS_EVENTS_FOLDERS = false;
  public static final boolean DEBUG__SUPPRESS_EVENTS_KEYS = false;
  public static final boolean DEBUG__SUPPRESS_EVENTS_USERS = false;
  public static final boolean DEBUG__SUPPRESS_EVENTS_CONTACTS = false;

  // login user record
  private Long myUserId;

  // login user settings record
  private UserSettingsRecord myUserSettingsRecord;

  // login user password recovery record
  private PassRecoveryRecord myPassRecoveryRecord;

  // login user password
  private BAEncodedPassword encodedPassword;

  // new user private key
  private RSAPrivateKey newUserPrivateKey;


  SortedMap userRecordMap;
  SortedMap folderRecordMap;
  SortedMap folderShareRecordMap;
  MultiHashtable folderShareRecordMap_byFldId;
  MultiHashtable folderShareRecordMap_byOwnerId;
  SortedMap fileLinkRecordMap;
  SortedMap fileDataRecordMap;
  SortedMap keyRecordMap;
  SortedMap contactRecordMap;
  SortedMap msgLinkRecordMap;
  MultiHashtable msgLinkRecordMap_byMsgId; // key is the msgId
  SortedMap msgDataRecordMap;
  SortedMap emailRecordMap;
  MultiHashtable addrHashRecordMap_byMsgId; // key is the msgId
  MultiHashtable addrHashRecordMap_byHash; // key is the hash
  SortedMap[] statRecordMaps;
  public Vector bodyKeys;
  public Hashtable requestedAddrHashHT;

  public static final int STAT_TYPE_FILE = 0;
  public static final int STAT_TYPE_FOLDER = 1;
  public static final int STAT_TYPE_MESSAGE = 2;

  EventListenerList myListenerList = new EventListenerList();

  private static FetchedDataCache singleInstance;
  private static final Object singleInstanceMonitor = new Object();

  /**
   * @returns a single instance of the cache.
   */
  public static FetchedDataCache getSingleInstance() {
    synchronized (singleInstanceMonitor) {
      if (singleInstance == null) {
        singleInstance = new FetchedDataCache();
      }
      return singleInstance;
    }
  }

  /** Creates new FetchedDataCache */
  private FetchedDataCache() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "FetchedDataCache()");
    init();
    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * Starts a session for this instance and initializes all variables to empty.
   */
  private synchronized void init() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "init()");
    myUserId = null;
    myUserSettingsRecord = null;
    myPassRecoveryRecord = null;
    userRecordMap = new TreeMap();
    folderRecordMap = new TreeMap();
    folderShareRecordMap = new TreeMap();
    folderShareRecordMap_byFldId = new MultiHashtable(true);
    folderShareRecordMap_byOwnerId = new MultiHashtable(true);
    fileLinkRecordMap = new TreeMap();
    fileDataRecordMap = new TreeMap();
    keyRecordMap = new TreeMap();
    contactRecordMap = new TreeMap();
    msgLinkRecordMap = new TreeMap();
    msgLinkRecordMap_byMsgId = new MultiHashtable(true);
    msgDataRecordMap = new TreeMap();
    emailRecordMap = new TreeMap();
    addrHashRecordMap_byMsgId = new MultiHashtable(true);
    addrHashRecordMap_byHash = new MultiHashtable(true);
    statRecordMaps = new SortedMap[3];
    for (int i=0; i<3; i++)
      statRecordMaps[i] = new TreeMap();
    bodyKeys = new Vector();
    requestedAddrHashHT = new Hashtable();
    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * Clears the cach to remove all of its data.
   */
  public void clear() {
    synchronized (this) {
      removeFileLinkRecords(getFileLinkRecords());
      removeMsgLinkRecords(getMsgLinkRecords());
      removeMsgDataRecords(getMsgDataRecords());
      removeContactRecords(getContactRecords());
      removeStatRecords(getStatRecords());
      removeEmailRecords(getEmailRecords());
      removeFolderRecords(getFolderRecords());
      removeFolderShareRecords(getFolderShareRecords());
      removeKeyRecords(getKeyRecords());
      removeUserRecords(getUserRecords());

      myUserId = null;
      myUserSettingsRecord = null;
      myPassRecoveryRecord = null;
      encodedPassword = null;

      fileLinkRecordMap.clear();
      fileDataRecordMap.clear();
      msgLinkRecordMap.clear();
      msgLinkRecordMap_byMsgId.clear();
      msgDataRecordMap.clear();
      contactRecordMap.clear();
      for (int i=0; i<3; i++)
        statRecordMaps[i].clear();
      emailRecordMap.clear();
      folderRecordMap.clear();
      folderShareRecordMap.clear();
      folderShareRecordMap_byFldId.clear();
      folderShareRecordMap_byOwnerId.clear();
      keyRecordMap.clear();
      userRecordMap.clear();
      bodyKeys.clear();
      requestedAddrHashHT.clear();
      addrHashRecordMap_byMsgId.clear();
      addrHashRecordMap_byHash.clear();
    }
    FolderRecUtil.clear();
  }

  /**
   *  Sets the encoded password for the duration of this connection.
   */
  public synchronized void setEncodedPassword(BAEncodedPassword encPassword) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "setEncodedPassword(BAEncodedPassword)");
    encodedPassword = encPassword;
    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   *  Gets the encoded password for the duration of this connection.
   */
  public synchronized BAEncodedPassword getEncodedPassword() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getEncodedPassword()");
    if (encodedPassword == null)
      throw new IllegalStateException("Encoded password is not available at this time.");
    if (trace != null) trace.exit(FetchedDataCache.class);
    return encodedPassword;
  }

  /**
   *  Sets the private key generated for the new user account.
   */
  public synchronized void setNewUserPrivateKey(RSAPrivateKey rsaPrivateKey) {
    newUserPrivateKey = rsaPrivateKey;
  }
  /**
   *  Sets the private key generated for the new user account.
   */
  public synchronized RSAPrivateKey getNewUserPrivateKey() {
    return newUserPrivateKey;
  }

  /**
   * @return the userId of the current user.
   */
  public synchronized Long getMyUserId() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getMyUserId()");
    Long myUId = myUserId;
    if (trace != null) trace.exit(FetchedDataCache.class, myUId);
    return myUId;
  }

  /**
   * @return the UserSettingsRecord of the current user.
   */
  public synchronized UserSettingsRecord getMyUserSettingsRecord() {
    return myUserSettingsRecord;
  }

  /**
   * @return the PassRecoveryRecord of the current user.
   */
  public synchronized PassRecoveryRecord getMyPassRecoveryRecord() {
    return myPassRecoveryRecord;
  }

  /**
   * @return number of FolderRecords in the cache
   */
  public synchronized int countFolderRecords() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "countFolderRecords()");
    int numOfFolders = folderRecordMap.size();
    if (trace != null) trace.exit(FetchedDataCache.class, numOfFolders);
    return numOfFolders;
  }

  /**
   * Add email address record hash to keep track of already requested ones.
   */
  public synchronized void addRequestedAddrHash(byte[] hash) {
    // Store the hash as string so when comparing different instances of the same data will match.
    String strHash = ArrayUtils.toString(hash);
    requestedAddrHashHT.put(strHash, strHash);
  }

  /**
   * Add email address record hashes in batch mode.
   */
  public synchronized void addRequestedAddrHashes(Vector hashesV) {
    for (int i=0; i<hashesV.size(); i++)
      addRequestedAddrHash((byte[]) hashesV.elementAt(i));
  }

  /**
   * @return true if hash exists in the requested cashe.
   */
  public synchronized boolean wasRequestedAddrHash(byte[] hash) {
    return requestedAddrHashHT.get(ArrayUtils.toString(hash)) != null;
  }

  /*********************************
   ***   UserRecord operations   ***
   *********************************/

  /**
   * Adds new records or record updates into the cach and fires appropriate event.
   */
  public void addUserRecords(UserRecord[] userRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addUserRecords(UserRecord[])");
    if (trace != null) trace.args(userRecords);

    if (userRecords != null && userRecords.length > 0) {
      // Un-seal my user record among other user records -- only when my User Record has not been un-sealed before.
      // At first login, there won't be any myUserRecord yet, when a keyRecord comes, we will unseal it then.
      UserRecord myUserRecord = getUserRecord();
      if (myUserRecord != null && myUserRecord.getSymKeyFldShares() == null && userRecords != null) {
        for (int i=0; i<userRecords.length; i++) {
          UserRecord uRec = userRecords[i];
          // only unwrap my user record
          if (uRec.userId.equals(myUserRecord.userId) && uRec.pubKeyId != null) {
            KeyRecord kRec = getKeyRecord(uRec.pubKeyId);
            if (kRec != null && kRec.getPrivateKey() != null) {
              uRec.unSeal(kRec);
            }
          }
        }
      }

      synchronized (this) {
        userRecords = (UserRecord[]) RecordUtils.merge(userRecordMap, userRecords);
      }

      // invalidate cached values for folders because rendering might have changed...
      FolderRecord[] fRecs = getFolderRecords();
      for (int i=0; i<fRecs.length; i++) {
        fRecs[i].invalidateCachedValues();
      }

      fireUserRecordUpdated(userRecords, RecordEvent.SET);
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * Removes records from the cach and fires appropriate event.
   */
  public void removeUserRecords(UserRecord[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeUserRecords(UserRecord[])");
    if (trace != null) trace.args(records);

    if (records != null && records.length > 0) {
      synchronized (this) {
        records = (UserRecord[]) RecordUtils.remove(userRecordMap, records);
      }
      // fire removal of users
      fireUserRecordUpdated(records, RecordEvent.REMOVE);
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * Sets the logged-in user record and adds it into the cache.  Cannot change the userId during the same session.
   */
  public void setUserRecord(UserRecord userRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "setUserRecord(UserRecord)");
    if (trace != null) trace.args(userRecord);

    synchronized (this) {
      if (myUserId == null)
        myUserId = userRecord.getId();
      else if (!userRecord.getId().equals(myUserId))
        throw new IllegalStateException("UserRecord already initialized with different id.");
    }

    addUserRecords(new UserRecord[] { userRecord });
    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * Sets the logged-in user settings record.  Cannot change the userId during the same session.
   */
  public void setUserSettingsRecord(UserSettingsRecord userSettingsRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "setUserSettingsRecord(UserSettingsRecord)");
    if (trace != null) trace.args(userSettingsRecord);

    synchronized (this) {
      if (myUserId == null)
        throw new IllegalStateException("UserRecord should be initialized first.");
      else if (!userSettingsRecord.userId.equals(myUserId))
        throw new IllegalStateException("UserRecord already initialized with different id.");
    }

    KeyRecord kRec = getKeyRecord(userSettingsRecord.pubKeyId);
    if (kRec != null && kRec.getPrivateKey() != null) {
      StringBuffer errorBuffer = new StringBuffer();
      userSettingsRecord.unSeal(kRec, errorBuffer);
      if (errorBuffer.length() > 0)
        MessageDialog.showErrorDialog(null, errorBuffer.toString(), "Invalid Settings");
    }

    synchronized (this) {
      if (myUserSettingsRecord != null)
        myUserSettingsRecord.merge(userSettingsRecord);
      else
        myUserSettingsRecord = userSettingsRecord;

      fireUserSettingsRecordUpdated(new UserSettingsRecord[] { myUserSettingsRecord }, RecordEvent.SET);
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * Sets the password recovery record.
   */
  public synchronized void setPassRecoveryRecord(PassRecoveryRecord passRecoveryRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "setPassRecoveryRecord(PassRecoveryRecord)");
    if (trace != null) trace.args(passRecoveryRecord);

    if (myPassRecoveryRecord != null && passRecoveryRecord != null)
      myPassRecoveryRecord.merge(passRecoveryRecord);
    else
      myPassRecoveryRecord = passRecoveryRecord;

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * @return stored User Record
   */
  public synchronized UserRecord getUserRecord() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getUserRecord()");
    UserRecord uRec = null;
    if (myUserId != null)
      uRec = getUserRecord(myUserId);
    if (trace != null) trace.exit(FetchedDataCache.class, uRec);
    return uRec;
  }

  public synchronized UserRecord getUserRecord(Long userId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getUserRecord(Long userId)");
    UserRecord uRec = (UserRecord) userRecordMap.get(userId);
    if (trace != null) trace.exit(FetchedDataCache.class, uRec);
    return uRec;
  }

  /**
   * @return all user records stored in the cache
   */
  public synchronized UserRecord[] getUserRecords() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getUserRecords()");

    Vector usersV = new Vector();
    usersV.addAll(userRecordMap.values());
    UserRecord[] users = (UserRecord[]) ArrayUtils.toArray(usersV, UserRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, users);
    return users;
  }

  /**
   * @return all users matching specified user IDs
   */
  public synchronized UserRecord[] getUserRecords(Long[] userIDs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getUserRecords(Long[] userIDs)");
    if (trace != null) trace.args(userIDs);

    Vector usersV = new Vector();
    if (userIDs != null) {
      for (int i=0; i<userIDs.length; i++) {
        UserRecord uRec = (UserRecord) userRecordMap.get(userIDs[i]);
        if (uRec != null)
          usersV.addElement(uRec);
      }
    }
    UserRecord[] users = (UserRecord[]) ArrayUtils.toArray(usersV, UserRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, users);
    return users;
  }

  /***********************************
   ***   FolderRecord operations   ***
   ***********************************/

  /**
   * Adds new records or record updates into the cach and fires appropriate event.
   */
  public void addFolderRecords(FolderRecord[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addFolderRecords(FolderRecord[])");
    if (trace != null) trace.args(records);

    if (records != null && records.length > 0) {
      // convert old chatting into new explicit type
      for (int i=0; i<records.length; i++) {
        if (records[i].folderType.shortValue() == FolderRecord.POSTING_FOLDER && records[i].isChatting())
          records[i].folderType = new Short(FolderRecord.CHATTING_FOLDER);
      }
      synchronized (this) {
        records = (FolderRecord[]) RecordUtils.merge(folderRecordMap, records);
      }
      fireFolderRecordUpdated(records, RecordEvent.SET);
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * Removes records from the cach and fires appropriate event.
   */
  public void removeFolderRecords(FolderRecord[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeFolderRecords(FolderRecord[])");
    if (trace != null) trace.args(records);

    if (records != null && records.length > 0) {
      // gather all folders that will be removed.
      FolderRecord[] allDescendingFolders = getFoldersAllDescending(records);
      FolderRecord[] allToRemove = (FolderRecord[]) ArrayUtils.concatinate(records, allDescendingFolders);
      synchronized (this) {
        records = (FolderRecord[]) RecordUtils.remove(folderRecordMap, allToRemove);
      }
      // We have removed all specified folders AND all their descendants
      fireFolderRecordUpdated(records, RecordEvent.REMOVE);

      // remove all shares that belong to those folders
      FolderShareRecord[] removingShares = (FolderShareRecord[]) getFolderShareRecordsForFolders(allToRemove);
      removeFolderShareRecords(removingShares);
      //removeFoldersAndChildrenFolderRecords(records);
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }


  /**
   * @return all FolderRecords from cache.
   */
  public synchronized FolderRecord[] getFolderRecords() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFolderRecords()");
    FolderRecord[] allFolders = (FolderRecord[]) ArrayUtils.toArray(folderRecordMap.values(), FolderRecord.class);
    if (trace != null) trace.exit(FetchedDataCache.class, allFolders);
    return allFolders;
  }

  /**
   * @return all FolderRecords from cache with specified IDs
   */
  public synchronized FolderRecord[] getFolderRecords(Long[] folderIDs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFolderRecords(Long[] folderIDs)");
    if (trace != null) trace.args(folderIDs);

    Vector fRecsV = new Vector();
    if (folderIDs != null) {
      for (int i=0; i<folderIDs.length; i++) {
        if (folderIDs[i] != null) {
          FolderRecord fRec = (FolderRecord) folderRecordMap.get(folderIDs[i]);
          if (fRec != null)
            fRecsV.addElement(fRec);
        }
      }
    }
    FolderRecord[] folders = (FolderRecord[]) ArrayUtils.toArray(fRecsV, FolderRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, folders);
    return folders;
  }

  /**
   * @return all FolderRecords from cache that pass through specified filter
   */
  public synchronized FolderRecord[] getFolderRecords(RecordFilter filter) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFolderRecords(RecordFilter filter)");
    if (trace != null) trace.args(filter);

    Vector fRecsV = new Vector();
    Collection col = folderRecordMap.values();
    Iterator iter = col.iterator();
    while (iter.hasNext()) {
      FolderRecord folderRecord = (FolderRecord) iter.next();
      // if this is someone else's folder
      if (filter.keep(folderRecord)) {
        fRecsV.addElement(folderRecord);
      }
    }
    FolderRecord[] folders = (FolderRecord[]) ArrayUtils.toArray(fRecsV, FolderRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, folders);
    return folders;
  }

  /**
   * @return all FolderRecords from cache which current user is NOT the owner of.
   */
  public synchronized FolderRecord[] getFolderRecordsNotMine() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFolderRecordsNotMine()");

    Vector fRecsV = new Vector();
    Collection col = folderRecordMap.values();
    Iterator iter = col.iterator();
    while (iter.hasNext()) {
      FolderRecord folderRecord = (FolderRecord) iter.next();
      // if this is someone else's folder
      if (!folderRecord.ownerUserId.equals(myUserId)) {
        fRecsV.addElement(folderRecord);
      }
    }
    FolderRecord[] folders = (FolderRecord[]) ArrayUtils.toArray(fRecsV, FolderRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, folders);
    return folders;
  }


  /**
   * @return all FolderRecords from cache which match the criteria.
   */
  public synchronized FolderRecord[] getFolderRecordsForUser(Long userId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFolderRecordsForUser(Long userId)");

    Vector fRecsV = new Vector();
    Collection col = folderRecordMap.values();
    Iterator iter = col.iterator();
    while (iter.hasNext()) {
      FolderRecord folderRecord = (FolderRecord) iter.next();
      if (folderRecord.ownerUserId.equals(userId)) {
        fRecsV.addElement(folderRecord);
      }
    }
    FolderRecord[] folders = (FolderRecord[]) ArrayUtils.toArray(fRecsV, FolderRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, folders);
    return folders;
  }

  /**
   * @return all FolderRecords from cache which match the criteria.
   */
  public synchronized FolderRecord[] getFolderRecordsForUsers(Long[] userIDs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFolderRecordsForUsers(Long[] userIDs)");

    Vector fRecsV = new Vector();
    Collection col = folderRecordMap.values();
    Iterator iter = col.iterator();
    while (iter.hasNext()) {
      FolderRecord folderRecord = (FolderRecord) iter.next();
      for (int i=0; i<userIDs.length; i++) {
        if (folderRecord.ownerUserId.equals(userIDs[i])) {
          if (!fRecsV.contains(folderRecord)) {
            fRecsV.addElement(folderRecord);
            break;
          }
        }
      }
    }
    FolderRecord[] folders = (FolderRecord[]) ArrayUtils.toArray(fRecsV, FolderRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, folders);
    return folders;
  }


  /**
   * @return all chatting FolderRecords from cache
   */
  public synchronized FolderRecord[] getFolderRecordsChatting() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFolderRecordsChatting()");

    Vector fRecsV = new Vector();
    Collection col = folderRecordMap.values();
    Iterator iter = col.iterator();
    while (iter.hasNext()) {
      FolderRecord folderRecord = (FolderRecord) iter.next();
      // if this is someone else's folder
      if (folderRecord.isChatting()) {
        fRecsV.addElement(folderRecord);
      }
    }
    FolderRecord[] folders = (FolderRecord[]) ArrayUtils.toArray(fRecsV, FolderRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, folders);
    return folders;
  }

  /**
   * @return a FolderRecord from cache with a given id.
   */
  public synchronized FolderRecord getFolderRecord(Long folderId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFolderRecord(Long folderId)");
    if (trace != null) trace.args(folderId);

    FolderRecord folderRecord = null;
    if (folderId != null)
      folderRecord = (FolderRecord) folderRecordMap.get(folderId);

    if (trace != null) trace.exit(FetchedDataCache.class, folderRecord);
    return folderRecord;
  }

  /**
   * Finds all folder records that carry the specified parentFolderId.
   * @return all children of the parent specified.
   */
  public synchronized FolderRecord[] getFoldersChildren(Long parentFolderId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFoldersChildren(Long parentFolderId)");
    if (trace != null) trace.args(parentFolderId);

    Vector childrenV = new Vector();
    Collection col = folderRecordMap.values();
    Iterator iter = col.iterator();
    while (iter.hasNext()) {
      FolderRecord folderRecord = (FolderRecord) iter.next();
      if (folderRecord.isChildToParent(parentFolderId)) {
        childrenV.addElement(folderRecord);
      }
    }
    FolderRecord[] folders = (FolderRecord[]) ArrayUtils.toArray(childrenV, FolderRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, folders);
    return folders;
  }

  /**
   * @return all children of the parents specified.
   */
  public synchronized FolderRecord[] getFoldersChildren(FolderRecord[] parentRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFoldersChildren(FolderRecord[] parentRecords)");
    if (trace != null) trace.args(parentRecords);

    // load hashtable with specified parents
    Hashtable ht = new Hashtable(parentRecords.length);
    for (int i=0; i<parentRecords.length; i++) {
      ht.put(parentRecords[i].folderId, parentRecords[i]);
    }
    Vector childrenV = new Vector();
    Collection col = folderRecordMap.values();
    Iterator iter = col.iterator();
    while (iter.hasNext()) {
      FolderRecord folderRecord = (FolderRecord) iter.next();
      // if this is a child at all
      if (!folderRecord.parentFolderId.equals(folderRecord.folderId)) {
        // if this is one of the wanted children (quick hashtable access)
        if (ht.get(folderRecord.parentFolderId) != null)
          childrenV.addElement(folderRecord);
      }
    }
    FolderRecord[] folders = (FolderRecord[]) ArrayUtils.toArray(childrenV, FolderRecord.class);
    ht.clear();

    if (trace != null) trace.exit(FetchedDataCache.class, folders);
    return folders;
  }

  /**
   * @param parentFolders
   * @return children in the FolderRecord hierarchy, ignores view hierarchy
   */
  public synchronized FolderRecord[] getFoldersAllDescending(FolderRecord[] parentFolders) {
    Vector allDescendantsV = new Vector();
    addFoldersAllChildren(allDescendantsV, parentFolders);
    FolderRecord[] result = (FolderRecord[]) ArrayUtils.toArray(allDescendantsV, FolderRecord.class);
    return result;
  }
  private synchronized void addFoldersAllChildren(Vector allDescendants, FolderRecord[] folders) {
    if (folders != null && folders.length >= 0) {
      FolderRecord[] childFolders = getFoldersChildren(folders);
      if (childFolders != null && childFolders.length > 0) {
        Vector realChildrenV = new Vector();
        for (int i=0; i<childFolders.length; i++) {
          if (!allDescendants.contains(childFolders[i])) {
            allDescendants.addElement(childFolders[i]);
            realChildrenV.addElement(childFolders[i]);
          }
        }
        FolderRecord[] realChildren = (FolderRecord[]) ArrayUtils.toArray(realChildrenV, FolderRecord.class);
        addFoldersAllChildren(allDescendants, realChildren);
      }
    }
  }

  /**
   * @return table of all Group Folders to which specified user belongs (recursively too)
   */
  public synchronized Long[] getFolderGroupIDsMy() {
    return getFolderGroupIDs(getMyUserId());
  }
  public synchronized Long[] getFolderGroupIDs(Long userId) {
    Hashtable groupIDsHT = getFolderGroupIDsHT(userId);
    Set keys = groupIDsHT.keySet();
    Long[] groupIDs = (Long[]) ArrayUtils.toArray(keys, Long.class);
    return groupIDs;
  }
  public synchronized Hashtable getFolderGroupIDsMyHT() {
    return myUserId != null ? getFolderGroupIDsHT(myUserId) : null;
  }
  public synchronized Hashtable getFolderGroupIDsHT(Long userId) {
    Hashtable groupIDsHT = new Hashtable();
    Vector sharesForUserV = folderShareRecordMap_byOwnerId.getAll(userId);
    if (sharesForUserV != null) {
      for (int i=0; i<sharesForUserV.size(); i++) {
        FolderShareRecord sRec = (FolderShareRecord) sharesForUserV.elementAt(i);
        if (sRec.isOwnedBy(userId, (Long[]) null)) { // check if owner is USER type
          FolderRecord fRec = getFolderRecord(sRec.folderId);
          if (fRec != null && fRec.isGroupType())
            groupIDsHT.put(fRec.folderId, fRec);
        }
      }
    }
    if (groupIDsHT.size() > 0)
      addFolderGroupIDs(groupIDsHT);
    return groupIDsHT;
  }
  private synchronized void addFolderGroupIDs(Hashtable groupIDsHT) {
    boolean anyAdded = false;
    Collection col = folderShareRecordMap.values();
    Iterator iter = col.iterator();
    while (iter.hasNext()) {
      FolderShareRecord sRec = (FolderShareRecord) iter.next();
      if (sRec.isOwnedByGroup() && groupIDsHT.get(sRec.ownerUserId) != null) {
        FolderRecord fRec = getFolderRecord(sRec.folderId);
        if (fRec != null && fRec.isGroupType()) {
          if (groupIDsHT.get(fRec.folderId) == null) {
            groupIDsHT.put(fRec.folderId, fRec);
            anyAdded = true;
          }
        }
      }
    }
    if (anyAdded)
      addFolderGroupIDs(groupIDsHT);
  }

  /**
   * Clears all Folder Records from the cache no events are fired.
   */
  private synchronized void clearFolderRecords() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "clearFolderRecords()");
    folderRecordMap.clear();
    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  public synchronized void clearFolderPairRecords() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "clearFolderPairRecords()");
    folderRecordMap.clear();
    folderShareRecordMap.clear();
    folderShareRecordMap_byFldId.clear();
    folderShareRecordMap_byOwnerId.clear();
    if (trace != null) trace.exit(FetchedDataCache.class);
  }



  /****************************************
   ***   FolderShareRecord operations   ***
   ****************************************/

  /**
   * Adds new records or record updates into the cach, unseals them and fires appropriate event.
   */
  public void addFolderShareRecords(FolderShareRecord[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addFolderShareRecords(FolderShareRecord[])");
    if (trace != null) trace.args(records);

    if (records != null && records.length > 0) {
      // Merge so that cached valued become updated, this is required for the
      // unsealing process to have complete and upto-date shares in the cache.
      Long myUId = null;
      Hashtable groupIDsHT = null;
      synchronized (this) {
        // Merge so that cached valued become updated, this is required for the
        // unsealing process to have complete and upto-date shares in the cache.
        records = (FolderShareRecord[]) RecordUtils.merge(folderShareRecordMap, records);
        for (int i=0; i<records.length; i++) folderShareRecordMap_byFldId.put(records[i].folderId, records[i]);
        for (int i=0; i<records.length; i++) folderShareRecordMap_byOwnerId.put(records[i].ownerUserId, records[i]);
        // carry on with unsealing preparations
        myUId = getMyUserId();
        groupIDsHT = getFolderGroupIDsHT(myUId);
        Vector toUnsealV = new Vector(Arrays.asList(records));
        Vector exceptionV = new Vector();
        while (toUnsealV.size() > 0) {
          for (int i=0; i<toUnsealV.size(); i++) {
            FolderShareRecord sRec = (FolderShareRecord) toUnsealV.elementAt(i);
            if (sRec.isOwnedBy(myUId, groupIDsHT)) { // group changes
              // local folder
              if (sRec.shareId.longValue() == FolderShareRecord.SHARE_LOCAL_ID) {
                sRec.setFolderName(FolderShareRecord.SHARE_LOCAL_NAME);
                sRec.setFolderDesc(FolderShareRecord.SHARE_LOCAL_DESC);
              } else if (sRec.shareId.longValue() == FolderShareRecord.CATEGORY_FILE_ID) {
                sRec.setFolderName(FolderShareRecord.CATEGORY_FILE_NAME);
                sRec.setFolderDesc(FolderShareRecord.CATEGORY_FILE_DESC);
              } else if (sRec.shareId.longValue() == FolderShareRecord.CATEGORY_MAIL_ID) {
                sRec.setFolderName(FolderShareRecord.CATEGORY_MAIL_NAME);
                sRec.setFolderDesc(FolderShareRecord.CATEGORY_MAIL_DESC);
              } else if (sRec.shareId.longValue() == FolderShareRecord.CATEGORY_CHAT_ID) {
                sRec.setFolderName(FolderShareRecord.CATEGORY_CHAT_NAME);
                sRec.setFolderDesc(FolderShareRecord.CATEGORY_CHAT_DESC);
              } else if (sRec.shareId.longValue() == FolderShareRecord.CATEGORY_GROUP_ID) {
                sRec.setFolderName(FolderShareRecord.CATEGORY_GROUP_NAME);
                sRec.setFolderDesc(FolderShareRecord.CATEGORY_GROUP_DESC);
              }
              try {
                attemptUnsealShare(sRec, exceptionV, groupIDsHT);
              } catch (Throwable t) {
                if (trace != null) trace.data(100, "Exception occured while attempting to unseal FolderShare", sRec);
                if (trace != null) trace.exception(FetchedDataCache.class, 101, t);
              }
            }
          } // end for
          int origSize = toUnsealV.size();
          toUnsealV.clear();
          if (exceptionV.size() > 0 && exceptionV.size() < origSize) {
            // another iteration
            toUnsealV.addAll(exceptionV);
          }
          exceptionV.clear();
        } // end while
      } // end synchronized - don't want other threads to take records if we are not done unsealing them...

      for (int i=0; i<records.length; i++) {
        // Clear folder cached data if applicable.
        FolderShareRecord sRec = records[i];
        if (sRec.isOwnedBy(myUId, groupIDsHT)) { // group changes
          FolderRecord fRec = getFolderRecord(sRec.folderId);
          if (fRec != null) {
            fRec.invalidateCachedValues();
          }
        }
      }

      fireFolderShareRecordUpdated(records, RecordEvent.SET);

    } // end if records != null

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  private void attemptUnsealShare(FolderShareRecord sRec, Vector exceptionV, Hashtable groupIDsHT) {
    // if anything to unseal
    if (sRec.getEncFolderName() != null) {
      // un-seal with user's global-folder symmetric key
      if (sRec.isOwnedByUser() && sRec.getPubKeyId() == null) {
        sRec.unSeal(getUserRecord().getSymKeyFldShares());
      }
      // un-seal with symmetric key of the group
      else if (sRec.isOwnedByGroup()) {
        FolderShareRecord myGroupShare = getFolderShareRecordMy(sRec.ownerUserId, groupIDsHT);
        BASymmetricKey symmetricKey = myGroupShare != null ? myGroupShare.getSymmetricKey() : null;
        if (symmetricKey != null)
          sRec.unSeal(symmetricKey);
        else
          exceptionV.addElement(sRec);
      }
      // un-seal new folder share with private key
      else if (sRec.getPubKeyId() != null) {
        KeyRecord keyRec = getKeyRecord(sRec.getPubKeyId());
        if (keyRec != null)
          sRec.unSeal(keyRec.getPrivateKey());
        else
          exceptionV.addElement(sRec);
      }
    } // end if anything to unseal
  }

  /**
   * Removes records from the cach and fires appropriate event.
   */
  public void removeFolderShareRecords(FolderShareRecord[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeFolderShareRecords(FolderShareRecord[])");
    if (trace != null) trace.args(records);

    if (records != null && records.length > 0) {
      synchronized (this) {
        records = (FolderShareRecord[]) RecordUtils.remove(folderShareRecordMap, records);
        for (int i=0; i<records.length; i++) folderShareRecordMap_byFldId.remove(records[i].folderId, records[i]);
        for (int i=0; i<records.length; i++) folderShareRecordMap_byOwnerId.remove(records[i].ownerUserId, records[i]);
      }

      // removing shares sometimes causes folder tree description changes or table heading description changes
      for (int i=0; i<records.length; i++) {
        // Clear folder cached data if applicable.
        FolderShareRecord sRec = records[i];
        FolderRecord fRec = getFolderRecord(sRec.folderId);
        if (fRec != null) {
          fRec.invalidateCachedValues();
        }
      }

      fireFolderShareRecordUpdated(records, RecordEvent.REMOVE);

      // Convert our removed shares to folder records, so we can remove them from the listeners.
      Long userId = getMyUserId();
      Vector fRecsToRemoveV = new Vector();
      for (int i=0; i<records.length; i++) {
        FolderShareRecord share = (FolderShareRecord) records[i];
        if (share.isOwnedBy(userId, (Long[]) null)) {
          FolderRecord fRec = getFolderRecord(share.folderId);
          if (fRec != null && !fRecsToRemoveV.contains(fRec))
            fRecsToRemoveV.addElement(fRec);
        }
      }
      if (fRecsToRemoveV.size() > 0) {
        FolderRecord[] fRecsToRemove = (FolderRecord[]) ArrayUtils.toArray(fRecsToRemoveV, FolderRecord.class);
        removeFolderRecords(fRecsToRemove);
      }
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * @return a FolderShareRecord from cache with a given id.
   */
  public synchronized FolderShareRecord getFolderShareRecord(Long shareId) {
    return (FolderShareRecord) folderShareRecordMap.get(shareId);
  }


  /**
   * @return all FolderShareRecords from cache.
   */
  public synchronized FolderShareRecord[] getFolderShareRecords() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFolderShareRecords()");

    Vector sharesV = new Vector();
    sharesV.addAll(folderShareRecordMap.values());
    FolderShareRecord[] shareRecords = (FolderShareRecord[]) ArrayUtils.toArray(sharesV, FolderShareRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, shareRecords);
    return shareRecords;
  }

  /**
   * @return specified FolderShareRecords from cache.
   */
  public synchronized FolderShareRecord[] getFolderShareRecords(Long[] shareIDs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFolderShareRecords(Long[] shareIDs)");

    Vector sharesV = new Vector();
    if (shareIDs != null) {
      for (int i=0; i<shareIDs.length; i++) {
        if (shareIDs[i] != null) {
          FolderShareRecord share = (FolderShareRecord) folderShareRecordMap.get(shareIDs[i]);
          if (share != null)
            sharesV.addElement(share);
        }
      }
    }
    FolderShareRecord[] shareRecords = (FolderShareRecord[]) ArrayUtils.toArray(sharesV, FolderShareRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, shareRecords);
    return shareRecords;
  }

  /**
   * @return a FolderShareRecord from cache for a given folderId and current user.
   */
  private synchronized FolderShareRecord getFolderShareRecordMy(Long folderId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFolderShareRecordMy(Long folderId)");
    if (trace != null) trace.args(folderId);

    FolderShareRecord shareRec = null;
    Vector sharesV = folderShareRecordMap_byFldId.getAll(folderId);
    if (sharesV != null) {
      for (int i=0; i<sharesV.size(); i++) {
        FolderShareRecord shareRecord = (FolderShareRecord) sharesV.elementAt(i);
        if (shareRecord.isOwnedBy(myUserId, (Long[]) null)) {
          shareRec = shareRecord;
          break;
        }
      }
    }

    if (trace != null) trace.exit(FetchedDataCache.class, shareRec);
    return shareRec;
  }
  public synchronized FolderShareRecord getFolderShareRecordMy(Long folderId, boolean includeGroupOwned) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFolderShareRecordMy(Long folderId, boolean includeGroupOwned)");
    if (trace != null) trace.args(folderId);
    if (trace != null) trace.args(includeGroupOwned);

    // first try to find my own share of this folder...
    FolderShareRecord shareRec = getFolderShareRecordMy(folderId);
    if (shareRec == null && includeGroupOwned) {
      Hashtable groupIDsHT = getFolderGroupIDsHT(myUserId);
      shareRec = getFolderShareRecordGroupOwnded(folderId, groupIDsHT);
    }

    if (trace != null) trace.exit(FetchedDataCache.class, shareRec);
    return shareRec;
  }
  public synchronized FolderShareRecord getFolderShareRecordMy(Long folderId, Hashtable groupIDsHT) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFolderShareRecordMy(Long folderId, Hashtable groupIDsHT)");
    if (trace != null) trace.args(folderId);
    if (trace != null) trace.args(groupIDsHT);

    FolderShareRecord shareRec = getFolderShareRecordMy(folderId);
    if (shareRec == null && groupIDsHT != null) {
      shareRec = getFolderShareRecordGroupOwnded(folderId, groupIDsHT);
    }

    if (trace != null) trace.exit(FetchedDataCache.class, shareRec);
    return shareRec;
  }
  private synchronized FolderShareRecord getFolderShareRecordGroupOwnded(Long folderId, Hashtable groupIDsHT) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFolderShareRecordGroupOwnded(Long folderId, Hashtable groupIDsHT)");
    if (trace != null) trace.args(folderId);
    if (trace != null) trace.args(groupIDsHT); // if present - include folder shares through group memberships

    FolderShareRecord shareRec = null;
    if (groupIDsHT != null && groupIDsHT.size() > 0) {
      Collection col = folderShareRecordMap.values();
      Iterator iter = col.iterator();
      while (iter.hasNext()) {
        FolderShareRecord shareRecord = (FolderShareRecord) iter.next();
        if (shareRecord.folderId.equals(folderId) && shareRecord.isOwnedBy(null, groupIDsHT)) {
          shareRec = shareRecord;
          break;
        }
      }
    }

    if (trace != null) trace.exit(FetchedDataCache.class, shareRec);
    return shareRec;
  }

  /**
   * @return a FolderShareRecords from cache for a given folderId and current user.
   */
  public synchronized FolderShareRecord[] getFolderShareRecordsMy(Long folderId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFolderShareRecordsMy(Long folderId)");
    if (trace != null) trace.args(folderId);

    Hashtable groupIDsHT = getFolderGroupIDsHT(myUserId);
    FolderShareRecord[] shareRecs = getFolderShareRecordsMy(folderId, groupIDsHT);

    if (trace != null) trace.exit(FetchedDataCache.class, shareRecs);
    return shareRecs;
  }
  public synchronized FolderShareRecord[] getFolderShareRecordsMy(Long folderId, Hashtable groupIDsHT) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFolderShareRecordMy(Long folderId, Hashtable groupIDsHT)");
    if (trace != null) trace.args(folderId);
    if (trace != null) trace.args(groupIDsHT); // if present - include folder shares through group memberships

    Vector shareRecsV = new Vector();
    Collection col = folderShareRecordMap.values();
    Iterator iter = col.iterator();
    while (iter.hasNext()) {
      FolderShareRecord shareRecord = (FolderShareRecord) iter.next();
      if (shareRecord.folderId.equals(folderId) &&
          shareRecord.isOwnedBy(myUserId, groupIDsHT)) { // group changes
        shareRecsV.addElement(shareRecord);
      }
    }
    FolderShareRecord[] shareRecs = (FolderShareRecord[]) ArrayUtils.toArray(shareRecsV, FolderShareRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, shareRecs);
    return shareRecs;
  }

  /**
   * @return all shares (only 1 per folder) that belong to current user.
   */
  public synchronized FolderShareRecord[] getFolderSharesMy(boolean includeGroupOwned) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFolderSharesMy(boolean includeGroupOwned)");
    if (trace != null) trace.args(includeGroupOwned);

    Hashtable folderIDsHT = new Hashtable();
    if (includeGroupOwned) {
      Hashtable groupIDsHT = null;
      groupIDsHT = getFolderGroupIDsHT(myUserId);
      Collection col = folderShareRecordMap.values();
      Iterator iter = col.iterator();
      while (iter.hasNext()) {
        FolderShareRecord shareRecord = (FolderShareRecord) iter.next();
        if (shareRecord.isOwnedBy(myUserId, groupIDsHT)) {
          // if user owned (not by group) then replace already existing group one, if not stored yet - store it
          if (shareRecord.isOwnedByUser() || folderIDsHT.get(shareRecord.folderId) == null)
            folderIDsHT.put(shareRecord.folderId, shareRecord);
        }
      }
    } else {
      // do not include group owned, use different Map for shortcut
      Vector sharesV = folderShareRecordMap_byOwnerId.getAll(myUserId);
      if (sharesV != null) {
        for (int i=0; i<sharesV.size(); i++) {
          FolderShareRecord shareRecord = (FolderShareRecord) sharesV.elementAt(i);
          if (shareRecord.isOwnedBy(myUserId, (Long[]) null))
            folderIDsHT.put(shareRecord.folderId, shareRecord);
        }
      }
    }
    FolderShareRecord[] shareRecs = (FolderShareRecord[]) ArrayUtils.toArray(folderIDsHT.values(), FolderShareRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, shareRecs);
    return shareRecs;
  }

  /**
   * @return all of my folder shares that belong to specified folders.
   */
  public synchronized FolderShareRecord[] getFolderSharesMyForFolders(Long[] folderIDs, boolean includeGroupOwned) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFolderSharesMyForFolders(Long[] folderIDs, boolean includeGroupOwned)");
    if (trace != null) trace.args(folderIDs);
    if (trace != null) trace.args(includeGroupOwned);

    FolderShareRecord[] shareRecords = null;
    if (folderIDs != null && folderIDs.length > 0) {
      // load group memberships
      Hashtable groupIDsHT = null;
      if (includeGroupOwned) groupIDsHT = getFolderGroupIDsHT(myUserId);
      shareRecords = getFolderSharesMyForFolders(folderIDs, groupIDsHT);
    }

    if (trace != null) trace.exit(FetchedDataCache.class, shareRecords);
    return shareRecords;
  }

  /**
   * @return all of my folder shares that belong to specified folders.
   */
  public synchronized FolderShareRecord[] getFolderSharesMyForFolders(Long[] folderIDs, Hashtable groupIDsHT) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFolderSharesMyForFolders(Long[] folderIDs, Hashtable groupIDsHT)");
    if (trace != null) trace.args(folderIDs);
    if (trace != null) trace.args(groupIDsHT);

    FolderShareRecord[] shareRecords = null;
    if (folderIDs != null && folderIDs.length > 0) {
      // load a Hashtable with wanted folder IDs
      Hashtable ht = new Hashtable(folderIDs.length);
      for (int i=0; i<folderIDs.length; i++)
        ht.put(folderIDs[i], folderIDs[i]);
      // go through all shares and see if we want them
      Hashtable folderIDsHT = new Hashtable();
      Collection col = folderShareRecordMap.values();
      Iterator iter = col.iterator();
      while (iter.hasNext()) {
        FolderShareRecord shareRecord = (FolderShareRecord) iter.next();
        if (ht.get(shareRecord.folderId) != null) {
          if (shareRecord.isOwnedBy(myUserId, groupIDsHT)) { // group changes
            if (shareRecord.isOwnedByUser() || folderIDsHT.get(shareRecord.folderId) == null) {
              folderIDsHT.put(shareRecord.folderId, shareRecord);
            }
          }
        }
      }
      shareRecords = (FolderShareRecord[]) ArrayUtils.toArray(folderIDsHT.values(), FolderShareRecord.class);
    }

    if (trace != null) trace.exit(FetchedDataCache.class, shareRecords);
    return shareRecords;
  }


  public synchronized FolderShareRecord[] getFolderShareRecordsMyRootsForMsgs(MsgLinkRecord[] msgLinks, boolean includeGroupOwned) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFolderShareRecordsMyRootsForMsgs(MsgLinkRecord[] msgLinks, boolean includeGroupOwned)");
    if (trace != null) trace.args(msgLinks);

    MsgLinkRecord[] childMsgs = msgLinks;
    FolderShareRecord[] parentShares = null;
    Hashtable groupIDsHT = null;
    while (true) {
      MsgLinkRecord[] parentLinks = null;
      FolderShareRecord[] parentShareRecs = null;
      Long[] ownerIDs = MsgLinkRecord.getOwnerObjIDs(childMsgs, Record.RECORD_TYPE_MESSAGE);
      if (trace != null) trace.data(10, "find all msg links for msg IDs", ownerIDs);
      if (ownerIDs != null && ownerIDs.length > 0) {
        parentLinks = getMsgLinkRecordsForMsgs(ownerIDs);
      }
      ownerIDs = MsgLinkRecord.getOwnerObjIDs(childMsgs, Record.RECORD_TYPE_FOLDER);
      if (trace != null) trace.data(20, "find share records for folder IDs", ownerIDs);
      if (ownerIDs != null && ownerIDs.length > 0) {
        if (includeGroupOwned && groupIDsHT == null) groupIDsHT = getFolderGroupIDsHT(myUserId);
        parentShareRecs = getFolderSharesMyForFolders(ownerIDs, groupIDsHT);
        if (parentShareRecs != null && parentShareRecs.length > 0) {
          parentShares = (FolderShareRecord[]) ArrayUtils.concatinate(parentShares, parentShareRecs);
          parentShares = (FolderShareRecord[]) ArrayUtils.removeDuplicates(parentShares);
        }
      }
      // recursively make the fetched parents to be children so we fetch their parents until we hit the roots.
      if (parentLinks != null && parentLinks.length > 0) {
        childMsgs = parentLinks;
      } else {
        break;
      }
    }

    if (trace != null) trace.exit(FetchedDataCache.class, parentShares);
    return parentShares;
  }


  /**
   * @return FolderShareRecords from cache for a given folderId.
   */
  public synchronized FolderShareRecord[] getFolderShareRecordsForFolder(Long folderId) {

    Vector sharesV = folderShareRecordMap_byFldId.getAll(folderId);
    FolderShareRecord[] shareRecords = (FolderShareRecord[]) ArrayUtils.toArray(sharesV, FolderShareRecord.class);

    return shareRecords;
  }


  /**
   * @return FolderShareRecords from cache for a given userId EXCLUDING shares accessed through group memberships
   */
  public synchronized FolderShareRecord[] getFolderShareRecordsForUsers(Long[] userIDs) {
    Vector sharesV = new Vector();
    userIDs = (Long[]) ArrayUtils.removeDuplicates(userIDs, Long.class);
    for (int i=0; i<userIDs.length; i++) {
      Vector sharesForOwnerV = folderShareRecordMap_byOwnerId.getAll(userIDs[i]);
      if (sharesForOwnerV != null) {
        for (int k=0; k<sharesForOwnerV.size(); k++) {
          FolderShareRecord shareRecord = (FolderShareRecord) sharesForOwnerV.elementAt(k);
          if (shareRecord.isOwnedByUser())
            sharesV.addElement(shareRecord);
        }
      }
    }
    FolderShareRecord[] shareRecords = (FolderShareRecord[]) ArrayUtils.toArray(sharesV, FolderShareRecord.class);
    return shareRecords;
  }


  /**
   * @return all children of the parents specified.
   */
  public synchronized FolderShareRecord[] getFolderShareRecordsForFolders(FolderRecord[] folderRecords) {
    Vector sharesV = new Vector();
    folderRecords = (FolderRecord[]) ArrayUtils.removeDuplicates(folderRecords, FolderRecord.class);
    for (int i=0; i<folderRecords.length; i++) {
      Vector sharesForFolderV = folderShareRecordMap_byFldId.getAll(folderRecords[i].folderId);
      if (sharesForFolderV != null) {
        for (int k=0; k<sharesForFolderV.size(); k++)
          sharesV.addElement(sharesForFolderV.elementAt(k));
      }
    }
    FolderShareRecord[] shareRecords = (FolderShareRecord[]) ArrayUtils.toArray(sharesV, FolderShareRecord.class);
    return shareRecords;
  }

  /**
   * Finds all folder pairs that are descendants in the VIEW tree to specified parents.
   * @return all descendant children of the view parents specified.
   */
  public synchronized FolderPair[] getFolderPairsViewAllDescending(FolderPair[] parentFolderPairs, boolean includeParents) {
    Vector allDescendants = new Vector();
    Hashtable groupIDsHT = getFolderGroupIDsHT(myUserId);
    addFolderPairsViewAllDescending(allDescendants, parentFolderPairs, groupIDsHT);
    // include or exclude parents, sometimes a folder is its own parent in the view, so do this anyway
    for (int i=0; i<parentFolderPairs.length; i++) {
      if (allDescendants.contains(parentFolderPairs[i]) != includeParents) {
        if (includeParents)
          allDescendants.addElement(parentFolderPairs[i]);
        else
          allDescendants.removeElement(parentFolderPairs[i]);
      }
    }
    // pack the result into an array
    FolderPair[] result = (FolderPair[]) ArrayUtils.toArray(allDescendants, FolderPair.class);
    return result;
  }
  private synchronized void addFolderPairsViewAllDescending(Vector allDescendants, FolderPair[] folderPairs, Hashtable groupIDsHT) {
    if (folderPairs != null && folderPairs.length >= 0) {
      FolderPair[] childPairs = getFolderPairsViewChildren(folderPairs, groupIDsHT);
      if (childPairs != null && childPairs.length > 0) {
        Vector realChildrenV = new Vector();
        for (int i=0; i<childPairs.length; i++) {
          if (!allDescendants.contains(childPairs[i])) {
            allDescendants.addElement(childPairs[i]);
            realChildrenV.addElement(childPairs[i]);
          }
        }
        FolderPair[] realChildren = (FolderPair[]) ArrayUtils.toArray(realChildrenV, FolderPair.class);
        addFolderPairsViewAllDescending(allDescendants, realChildren, groupIDsHT);
      }
    }
  }

  /**
   * Finds all folder pairs that are children in the VIEW tree to specified parent.
   * @return all children of the view parent specified.
   */
  public synchronized FolderPair[] getFolderPairsViewChildren(Long parentFolderId, boolean includeGroupOwned) {
    // exceptional case is when looking for children of Category folder, in that case allow root folders to match
    if (parentFolderId.longValue() < 0)
      return getFolderPairs(new FolderFilter(null, null, parentFolderId, null), includeGroupOwned);
    else
      return getFolderPairs(new FolderFilter(null, null, parentFolderId, Boolean.FALSE), includeGroupOwned);
  }
  public synchronized FolderPair[] getFolderPairsViewChildren(FolderPair[] parentFolderPairs, boolean includeGroupOwned) {
    return getFolderPairs(new FolderFilter(RecordUtils.getIDs(parentFolderPairs)), includeGroupOwned);
  }
  public synchronized FolderPair[] getFolderPairsViewChildren(FolderPair[] parentFolderPairs, Hashtable groupIDsHT) {
    return getFolderPairs(new FolderFilter(RecordUtils.getIDs(parentFolderPairs)), groupIDsHT);
  }
  /**
   * @return all of My accessible Posting Folder Shares (for ie: message recipients) or other types
   */
  public synchronized FolderPair[] getFolderPairsMyOfType(short folderType, boolean includeGroupOwned) {
    return getFolderPairs(new FolderFilter(folderType), includeGroupOwned);
  }
  /**
   * @param filter is typically FolderFilter instance type.
   * @return all of My Folder Shares that pass through the specified filter.
   */
  public synchronized FolderPair[] getFolderPairs(RecordFilter filter, boolean includeGroupOwned) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFolderPairs(RecordFilter filter, boolean includeGroupOwned)");
    if (trace != null) trace.args(filter);
    if (trace != null) trace.args(includeGroupOwned);
    Hashtable groupIDsHT = null;
    if (includeGroupOwned) {
      if (myUserId != null)
        groupIDsHT = getFolderGroupIDsHT(myUserId);
    }
    FolderPair[] folderPairs = getFolderPairs(filter, groupIDsHT);
    if (trace != null) trace.exit(FetchedDataCache.class, folderPairs);
    return folderPairs;
  }
  public synchronized FolderPair[] getFolderPairs(RecordFilter filter, Hashtable groupIDsHT) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFolderPairs(RecordFilter filter, Hashtable groupIDsHT)");
    if (trace != null) trace.args(filter);
    if (trace != null) trace.args(groupIDsHT);
    Hashtable folderPairsHT = new Hashtable();
    Collection col = folderShareRecordMap.values();
    Iterator iter = col.iterator();
    while (iter.hasNext()) {
      FolderShareRecord shareRecord = (FolderShareRecord) iter.next();
      // if this is one of the wanted shares
      if (shareRecord.isOwnedBy(myUserId, groupIDsHT)) { // group changes required
        FolderRecord folderRecord = getFolderRecord(shareRecord.folderId);
        if (folderRecord != null) {
          FolderPair fPair = new FolderPair(shareRecord, folderRecord);
          if (filter.keep(fPair)) {
            if (shareRecord.isOwnedByUser() || folderPairsHT.get(shareRecord.folderId) == null) {
              folderPairsHT.put(shareRecord.folderId, fPair);
            }
          }
        }
      }
    }
    FolderPair[] folderPairs = (FolderPair[]) ArrayUtils.toArray(folderPairsHT.values(), FolderPair.class);
    if (trace != null) trace.exit(FetchedDataCache.class, folderPairs);
    return folderPairs;
  }


  /**
   * Clears all Folder Share Records from the cache no events are fired.
   */
  private synchronized void clearFolderShareRecords() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "clearFolderShareRecords()");
    folderShareRecordMap.clear();
    folderShareRecordMap_byFldId.clear();
    folderShareRecordMap_byOwnerId.clear();
    if (trace != null) trace.exit(FetchedDataCache.class);
  }


  /*************************************
   ***   FileLinkRecord operations   ***
   *************************************/

  /**
   * Adds new records or record updates into the cach and fires appropriate event.
   */
  public void addFileLinkRecords(FileLinkRecord[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addFileLinkRecords(FileLinkRecord[])");
    if (trace != null) trace.args(records);

    if (records != null && records.length > 0) {
      synchronized (this) {
        Hashtable groupIDsHT = null;
        // un-seal the records
        for (int i=0; i<records.length; i++) {
          FileLinkRecord fLink = records[i];
          BASymmetricKey unsealingKey = null;
          short ownerObjType = fLink.ownerObjType.shortValue();

          // find the symmetric key from the owner object's record.
          switch (ownerObjType) {
            case Record.RECORD_TYPE_FOLDER:
              if (groupIDsHT == null) groupIDsHT = getFolderGroupIDsHT(myUserId);
              FolderShareRecord shareRecord = getFolderShareRecordMy(fLink.ownerObjId, groupIDsHT);
              // Share Record may be null if this file was moved from a shared folder to a private on
              // not accessible to us, but some other user.
              if (shareRecord != null) {
                unsealingKey = shareRecord.getSymmetricKey();
              }
              break;
            case Record.RECORD_TYPE_MESSAGE:
              // any password protected message has the key
              MsgDataRecord msgDataRecord = getMsgDataRecord(fLink.ownerObjId);
              if (msgDataRecord.bodyPassHash != null) {
                unsealingKey = msgDataRecord.getSymmetricBodyKey();
              } else {
                // any message link has a symmetric key for the message data and attached files
                MsgLinkRecord[] msgLinkRecords = getMsgLinkRecordsForMsg(fLink.ownerObjId);
                if (msgLinkRecords != null && msgLinkRecords.length > 0)
                  unsealingKey = msgLinkRecords[0].getSymmetricKey();
              }
              break;
            default:
              throw new IllegalArgumentException("Not supported: ownerObjType=" + ownerObjType);
          }
          if (unsealingKey != null) {
            fLink.unSeal(unsealingKey);
          }
        } // end for

        records = (FileLinkRecord[]) RecordUtils.merge(fileLinkRecordMap, records);
      } // end synchronized
      fireFileLinkRecordUpdated(records, RecordEvent.SET);

      // recalculate flags in the involved folders
      statUpdatesInFoldersForVisualNotification(records);
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * Removes records from the cach and fires appropriate event.
   */
  public void removeFileLinkRecords(FileLinkRecord[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeFileLinkRecords(FileLinkRecord[])");
    if (trace != null) trace.args(records);

    if (records != null && records.length > 0) {
      synchronized (this) {
        records = (FileLinkRecord[]) RecordUtils.remove(fileLinkRecordMap, records);
      }
      fireFileLinkRecordUpdated(records, RecordEvent.REMOVE);

      // remove corresponding Stat Records
      removeStatRecords(getStatRecords(RecordUtils.getIDs(records), STAT_TYPE_FILE), false);

      // recalculate flags in the involved folders
      statUpdatesInFoldersForVisualNotification(records);
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * Removes records from the cache and fires appropriate event.
   */
  public synchronized void removeFileLinkRecords(Long[] fileLinkIDs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeFileLinkRecords(Long[] fileLinkIDs)");
    if (trace != null) trace.args(fileLinkIDs);

    FileLinkRecord[] fileLinks = getFileLinkRecords(fileLinkIDs);
    if (fileLinks != null && fileLinks.length > 0) {
      removeFileLinkRecords(fileLinks);
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * @return a FileLinkRecord from cache with a given id.
   */
  public synchronized FileLinkRecord getFileLinkRecord(Long fileLinkId) {
    return (FileLinkRecord) fileLinkRecordMap.get(fileLinkId);
  }

  /**
   * @return all FileLinkRecords from cache
   */
  public synchronized FileLinkRecord[] getFileLinkRecords() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFileLinkRecords()");

    Vector fileLinksV = new Vector();
    fileLinksV.addAll(fileLinkRecordMap.values());
    FileLinkRecord[] fileLinks = (FileLinkRecord[]) ArrayUtils.toArray(fileLinksV, FileLinkRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, fileLinks);
    return fileLinks;
  }

  /**
   * @return all File Link Records for specified file link ids.
   * The records found in the cache are returned, the IDs which do not have
   * corresponding records in the cache are ignored.
   */
  public synchronized FileLinkRecord[] getFileLinkRecords(Long[] fileLinkIDs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFileLinkRecords(Long[] fileLinkIDs)");
    if (trace != null) trace.args(fileLinkIDs);

    Vector fileLinksV = new Vector();
    if (fileLinkIDs != null) {
      for (int i=0; i<fileLinkIDs.length; i++) {
        FileLinkRecord fileLink = (FileLinkRecord) fileLinkRecordMap.get(fileLinkIDs[i]);
        if (fileLink != null)
          fileLinksV.addElement(fileLink);
      }
    }
    FileLinkRecord[] fileLinks = (FileLinkRecord[]) ArrayUtils.toArray(fileLinksV, FileLinkRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, fileLinks);
    return fileLinks;
  }


  /**
   * @return a collection of FileLinkRecords for specified shareId.
   */
  public synchronized FileLinkRecord[] getFileLinkRecords(Long shareId) {
    // find the corresponding folderId
    Long folderId = getFolderShareRecord(shareId).folderId;
    return getFileLinkRecordsOwnerAndType(folderId, new Short(Record.RECORD_TYPE_FOLDER));
  }

  /**
   * Filters the map to collect FileLinkRecords for a given ownerId and ownerType.
   * @return a collection of FileLinkRecords for specified ownerId and ownerType.
   */
  public synchronized FileLinkRecord[] getFileLinkRecordsOwnerAndType(Long ownerId, Short ownerType) {
    Vector fileLinksV = new Vector();
    // Collect all file links for this folder
    Iterator iter = fileLinkRecordMap.values().iterator();
    while (iter.hasNext()) {
      FileLinkRecord fLink = (FileLinkRecord) iter.next();
      if (fLink.ownerObjId.equals(ownerId) && fLink.ownerObjType.equals(ownerType))
        fileLinksV.addElement(fLink);
    }
    FileLinkRecord[] fileLinks = (FileLinkRecord[]) ArrayUtils.toArray(fileLinksV, FileLinkRecord.class);
    return fileLinks;
  }


  /**
   * Filters the map to collect FileLinkRecords for a given ownerIDs and ownerType.
   * @return a collection of FileLinkRecords for specified ownerId and ownerType.
   */
  public synchronized FileLinkRecord[] getFileLinkRecordsOwnersAndType(Long[] ownerIDs, Short ownerType) {
    Vector fileLinksV = new Vector();
    // load a Hashtable with wanted ownerIDs
    Hashtable ht = new Hashtable();
    for (int i=0; i<ownerIDs.length; i++) {
      ht.put(ownerIDs[i], ownerIDs[i]);
    }
    // Collect all file links for this folder
    Iterator iter = fileLinkRecordMap.values().iterator();
    while (iter.hasNext()) {
      FileLinkRecord fLink = (FileLinkRecord) iter.next();
      if (fLink.ownerObjType.equals(ownerType) && ht.get(fLink.ownerObjId) != null)
        fileLinksV.addElement(fLink);
    }
    FileLinkRecord[] fileLinks = (FileLinkRecord[]) ArrayUtils.toArray(fileLinksV, FileLinkRecord.class);
    return fileLinks;
  }


  /*************************************
   ***   FileDataRecord operations   ***
   *************************************/

  /**
   * Adds new records or record updates into the cach and fires appropriate event.
   */
  public void addFileDataRecords(FileDataRecord[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addFileDataRecords(FileDataRecord[])");
    if (trace != null) trace.args(records);

    if (records != null && records.length > 0) {
      synchronized (this) {
        records = (FileDataRecord[]) RecordUtils.merge(fileDataRecordMap, records);
      }
      //fireFileDataRecordUpdated(records, RecordEvent.SET);
      // temporary enc files should be expired now, plain files are already created.
      for (int i=0; i<records.length; i++) {
        records[i].cleanupEncFile();
      }
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  public synchronized FileDataRecord getFileDataRecord(Long fileId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getFileDataRecord(Long fileId)");
    if (trace != null) trace.args(fileId);

    FileDataRecord fRec = null;
    if (fileId != null)
      fRec = (FileDataRecord) fileDataRecordMap.get(fileId);

    if (trace != null) trace.exit(FetchedDataCache.class, fRec);
    return fRec;
  }


  /********************************
   ***   KeyRecord operations   ***
   ********************************/

  /**

   * Adds new records or record updates into the cach and fires appropriate event.
   */
  public void addKeyRecords(KeyRecord[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addKeyRecords(KeyRecord[])");
    if (trace != null) trace.args(records);

    if (records != null && records.length > 0) {
      StringBuffer errorBuffer = new StringBuffer();
      synchronized (this) {
        // Unwrap key records.
        for (int i=0; i<records.length; i++) {
          if (records[i].ownerUserId.equals(myUserId)) {

            // in case this is my key and doesn't have the encrypted private key, try to fetch it from properties
            BASymCipherBlock ba = records[i].getEncPrivateKey();
            if (ba == null) {

              String propertyKey = "Enc"+RSAPrivateKey.OBJECT_NAME+"_"+records[i].keyId;
              GlobalSubProperties keyProperties = new GlobalSubProperties(GlobalSubProperties.PROPERTY_EXTENSION_KEYS);
              String property = keyProperties.getProperty(propertyKey);

              if (property != null && property.length() > 0) {
                ba = new BASymCipherBlock(ArrayUtils.toByteArray(property));
                records[i].setEncPrivateKey(ba);
              }
            }

            records[i].unSeal(getEncodedPassword());

            // Now that we have the key, if my userRecord is not yet decrypted, decrypt it now.
            UserRecord myUserRecord = getUserRecord();
            if (myUserRecord != null &&
                records[i].keyId.equals(myUserRecord.pubKeyId) &&
                records[i].getPrivateKey() != null &&
                myUserRecord.getSymKeyFldShares() == null)
            {
              myUserRecord.unSeal(records[i]);
            }

            // Also decrypt the user settings if not yet decrypted
            if (myUserSettingsRecord != null &&
                myUserSettingsRecord.getXmlText() == null &&
                myUserSettingsRecord.pubKeyId.equals(records[i].keyId))
            {
              myUserSettingsRecord.unSeal(records[i], errorBuffer);
              fireUserSettingsRecordUpdated(new UserSettingsRecord[] { myUserSettingsRecord }, RecordEvent.SET);
            }
          }
        }

        records = (KeyRecord[]) RecordUtils.merge(keyRecordMap, records);
      }
      if (errorBuffer.length() > 0)
        MessageDialog.showErrorDialog(null, errorBuffer.toString(), "Invalid Settings");
      fireKeyRecordUpdated(records, RecordEvent.SET);
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * Removes records from the cach and fires appropriate event.
   */
  public void removeKeyRecords(KeyRecord[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeKeyRecords(KeyRecord[])");
    if (trace != null) trace.args(records);

    if (records != null && records.length > 0) {
      synchronized (this) {
        records = (KeyRecord[]) RecordUtils.remove(keyRecordMap, records);
      }
      fireKeyRecordUpdated(records, RecordEvent.REMOVE);
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * @return a KeyRecord from cache with a given id.
   */
  public synchronized KeyRecord getKeyRecord(Long keyId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getKeyRecord(Long)");
    if (trace != null) trace.args(keyId);

    KeyRecord kRec = null;
    if (keyId != null)
      kRec = (KeyRecord) keyRecordMap.get(keyId);

    if (trace != null) trace.exit(FetchedDataCache.class, kRec);
    return kRec;
  }

  /**
   * @return ALL KeyRecords stored in the cache.
   */
  public synchronized KeyRecord[] getKeyRecords() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getKeyRecords()");

    //Iterator iter = keyRecordMap.values().iterator();
    Vector keysV = new Vector();
    keysV.addAll(keyRecordMap.values());
    KeyRecord[] keys = (KeyRecord[]) ArrayUtils.toArray(keysV, KeyRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, keys);
    return keys;
  }

  /**
   * @return the current KeyRecord (last key record created by the user -- user.currentKeyId keyId).
   */
  public synchronized KeyRecord getKeyRecordMyCurrent() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getCurrentKeyRecord()");

    KeyRecord keyRecord = getKeyRecord(getUserRecord().currentKeyId);

    if (trace != null) trace.exit(FetchedDataCache.class, keyRecord);
    return keyRecord;
  }

  /**
   * @return most recent KeyRecord for a given user
   */
  public synchronized KeyRecord getKeyRecordForUser(Long userId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getKeyRecordForUser(Long userId)");
    if (trace != null) trace.args(userId);

    KeyRecord keyRecord = null;
    Iterator iter = keyRecordMap.values().iterator();
    while (iter.hasNext()) {
      KeyRecord kRec = (KeyRecord) iter.next();
      if (kRec.ownerUserId.equals(userId)) {
        if (keyRecord == null || kRec.keyId.longValue() > keyRecord.keyId.longValue())
          keyRecord = kRec;
      }
    }

    if (trace != null) trace.exit(FetchedDataCache.class, keyRecord);
    return keyRecord;
  }




  /************************************
   ***   ContactRecord operations   ***
   ************************************/

  /**
   * Adds new records or record updates into the cach and fires appropriate event.
   */
  public void addContactRecords(ContactRecord[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addContactRecords(ContactRecord[])");
    if (trace != null) trace.args(records);

    if (records != null && records.length > 0) {
      // xxxx to-do: this should be after unwrap
      // invalidate cached values for folders because rendering might have changed...
      FolderRecord[] fRecs = getFolderRecords();
      for (int i=0; i<fRecs.length; i++) {
        fRecs[i].invalidateCachedValues();
      }

      synchronized (this) {
        unWrapContactRecords(records);
        records = (ContactRecord[]) RecordUtils.merge(contactRecordMap, records);
      }

      fireContactRecordUpdated(records, RecordEvent.SET);

      // After notification is done, make previous status equal current status,
      // so that late unnecessary notification are not triggered when managing contacts.
      final ContactRecord[] recs = records;
      Thread th = new Thread() {
        public void run() {
          try { Thread.sleep(3000); } catch (Throwable t) { }
          for (int i=0; recs!=null && i<recs.length; i++) {
            if (recs[i] != null)
              recs[i].previousStatus = recs[i].status;
          }
        }
      };
      th.setDaemon(true);
      th.start();
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * Removes records from the cache and fires appropriate event.
   */
  public void removeContactRecords(ContactRecord[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeContactRecords(ContactRecord[])");
    if (trace != null) trace.args(records);

    if (records != null && records.length > 0) {
      synchronized (this) {
        records = (ContactRecord[]) RecordUtils.remove(contactRecordMap, records);
      }
      fireContactRecordUpdated(records, RecordEvent.REMOVE);
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  private void unWrapContactRecords(ContactRecord[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "unWrapContactRecords(ContactRecord[] records)");
    if (trace != null) trace.args(records);

    UserRecord myUserRecord = null;
    Hashtable groupIDsHT = null;
    // Unwrap contact records.
    for (int i=0; i<records.length; i++) {
      ContactRecord cRec = records[i];

      // If status change to ONLINE, play sound.
      if (cRec.status != null && ContactRecord.isOnlineStatus(cRec.status)) {
        ContactRecord oldRec = getContactRecord(cRec.contactId);
        if (oldRec != null &&
                !ContactRecord.isOnlineStatus(oldRec.status) &&
                myUserId.equals(oldRec.ownerUserId) &&
                (oldRec.permits.intValue() & ContactRecord.SETTING_DISABLE_AUDIBLE_ONLINE_NOTIFY) == 0
                )
        {
          Sounds.playAsynchronous(Sounds.ONLINE);
        }
      }

      if (groupIDsHT == null) groupIDsHT = getFolderGroupIDsHT(myUserId);
      FolderShareRecord shareRecord = getFolderShareRecordMy(cRec.folderId, groupIDsHT);
      // unSeal only OWNER part
      if (cRec.ownerUserId != null && cRec.ownerUserId.equals(myUserId)) {
        // OWNER part
        if (cRec.getEncOwnerNote() != null) {
          if (cRec.getEncOwnerNote().size() > 0) {
            if (shareRecord != null)
              cRec.unSeal(shareRecord.getSymmetricKey());
          } else {
            // If owner's note is blank, use default handle or make from user id.
            UserRecord uRec = getUserRecord(cRec.contactWithId);
            if (uRec != null)
              cRec.setOwnerNote(uRec.handle);
            else
              cRec.setOwnerNote("User (" + cRec.contactWithId + ")");
          }
        }
      }
      // unSeal only OTHER part
      else if (cRec.contactWithId != null && cRec.contactWithId.equals(myUserId)) {
        // OTHER part
        if (cRec.getEncOtherNote() != null) {
          if (cRec.getEncOtherNote().size() > 0) {
            Long keyId = cRec.getOtherKeyId();
            if (keyId != null) {
              KeyRecord otherKeyRec = getKeyRecord(keyId);
              if (otherKeyRec != null) {
                cRec.unSeal(otherKeyRec);
              }
            } else {
              // if no keyId then this record must have already been recrypted
              if (myUserRecord == null) myUserRecord = getUserRecord();
              BASymmetricKey ba = myUserRecord.getSymKeyCntNotes();
              if (ba != null) cRec.unSealRecrypted(ba);
            }
          } else {
            // If other note is blank, use default handle or make from user id.
            UserRecord uRec = getUserRecord(cRec.ownerUserId);
            if (uRec != null)
              cRec.setOtherNote(uRec.handle);
            else
              cRec.setOtherNote("User (" + cRec.contactWithId + ")");
          }
        }
      } // end unSeal OTHER part
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  } // end unWrapContactRecords()

  /**
   * Clears all Contact Records from the cache no events are fired.
   */
  public synchronized void clearContactRecords() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "clearContactRecords()");

    contactRecordMap.clear();

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * @return a ContactRecord from cache with a given id.
   */
  public synchronized ContactRecord getContactRecord(Long contactId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getContactRecord(Long)");
    if (trace != null) trace.args(contactId);

    ContactRecord cRec = (ContactRecord) contactRecordMap.get(contactId);

    if (trace != null) trace.exit(FetchedDataCache.class, cRec);
    return cRec;
  }

  /**
   * @return all Contact Records.
   */
  public synchronized ContactRecord[] getContactRecords() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getContactRecords()");

    Vector contactsV = new Vector();
    contactsV.addAll(contactRecordMap.values());
    ContactRecord[] contacts = (ContactRecord[]) ArrayUtils.toArray(contactsV, ContactRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, contacts);
    return contacts;
  }

  /**
   * @return all My Active Contact Records.
   */
  public synchronized ContactRecord[] getContactRecordsMyActive() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getContactRecordsMyActive()");

    Iterator iter = contactRecordMap.values().iterator();
    Vector contactsV = new Vector();
    while (iter.hasNext()) {
      ContactRecord cRec = (ContactRecord) iter.next();
      if (cRec.ownerUserId.equals(myUserId) && cRec.isOfActiveType()) {
        contactsV.addElement(cRec);
      }
    }
    ContactRecord[] contacts = (ContactRecord[]) ArrayUtils.toArray(contactsV, ContactRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, contacts);
    return contacts;
  }

  /**
   * @return contact matching search criteria.
   */
  public synchronized ContactRecord getContactRecordOwnerWith(Long ownerUserId, Long withUserId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getContactRecordOwnerWith(Long ownerUserId, Long withUserId)");

    ContactRecord contactRecord = null;
    Iterator iter = contactRecordMap.values().iterator();
    while (iter.hasNext()) {
      ContactRecord cRec = (ContactRecord) iter.next();
      if (cRec.ownerUserId.equals(ownerUserId) && cRec.contactWithId.equals(withUserId)) {
        contactRecord = cRec;
        break;
      }
    }

    if (trace != null) trace.exit(FetchedDataCache.class, contactRecord);
    return contactRecord;
  }


  /**
   * @return records matching search criteria.
   */
  public synchronized ContactRecord[] getContactRecordsForUsers(Long[] userIDs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getContactRecordsForUsers(Long[] userIDs)");

    Vector recordsV = new Vector();
    Iterator iter = contactRecordMap.values().iterator();
    while (iter.hasNext()) {
      ContactRecord cRec = (ContactRecord) iter.next();
      for (int i=0; i<userIDs.length; i++) {
        Long uId = userIDs[i];
        if (cRec.ownerUserId.equals(uId) || cRec.contactWithId.equals(uId)) {
          if (!recordsV.contains(cRec)) {
            recordsV.addElement(cRec);
            break;
          }
        }
      }
    }
    ContactRecord[] records = (ContactRecord[]) ArrayUtils.toArray(recordsV, ContactRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, records);
    return records;
  }


  /************************************
   ***   MsgLinkRecord operations   ***
   ************************************/

  /**
   * Adds new records or record updates into the cach and fires appropriate event.
   */
  public void addMsgLinkRecords(MsgLinkRecord[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addMsgLinkRecords(MsgLinkRecord[] records)");
    if (trace != null) trace.args(records);

    if (records != null && records.length > 0) {
      synchronized (this) {
        // unseal the Msg Links
        unWrapMsgLinkRecords(records);
        records = (MsgLinkRecord[]) RecordUtils.merge(msgLinkRecordMap, records);
        for (int i=0; i<records.length; i++) msgLinkRecordMap_byMsgId.put(records[i].msgId, records[i]);
      }
      fireMsgLinkRecordUpdated(records, RecordEvent.SET);

      // recalculate flags in the involved folders
      statUpdatesInFoldersForVisualNotification(records);
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }
  /**
   * Called ONLY when new records are added to cache in SET mode.
   */
  private void unWrapMsgLinkRecords(MsgLinkRecord[] linkRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "unWrapMsgLinkRecords(MsgLinkRecord[] linkRecords)");
    if (trace != null) trace.args(linkRecords);
    Hashtable groupIDsHT = null;
    for (int i=0; i<linkRecords.length; i++) {
      MsgLinkRecord link = linkRecords[i];
      try {
        if (link.getRecPubKeyId() != null) {
          KeyRecord kRec = getKeyRecord(link.getRecPubKeyId());
          if (kRec != null && kRec.getPrivateKey() != null)
            link.unSeal(kRec);
        } else if (link.ownerObjType != null && link.ownerObjId != null) {
          // When a message BODY is received, it does not have msg link and brief's fields -- ignore unSealing
          if (link.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER) {
            if (groupIDsHT == null) groupIDsHT = getFolderGroupIDsHT(myUserId);
            FolderShareRecord sRec = getFolderShareRecordMy(link.ownerObjId, groupIDsHT);
            if (sRec != null)
              link.unSeal(sRec.getSymmetricKey());
          } else if (link.ownerObjType.shortValue() == Record.RECORD_TYPE_MESSAGE) {
            // any password protected message has the key
            MsgDataRecord msgDataRecord = getMsgDataRecord(link.ownerObjId);
            if (msgDataRecord.bodyPassHash != null) {
              link.unSeal(msgDataRecord.getSymmetricBodyKey());
            } else {
              // any message link has a symmetric key for the message data and attached files
              MsgLinkRecord[] lRecs = getMsgLinkRecordsForMsg(link.ownerObjId);
              if (lRecs != null && lRecs.length > 0)
                link.unSeal(lRecs[0].getSymmetricKey());
            }
          }
        }
      } catch (Throwable t) {
        if (trace != null) trace.data(100, "Exception occured while processing link", link);
        if (trace != null) trace.exception(FetchedDataCache.class, 101, t);
      }
    } // end for

    if (trace != null) trace.exit(FetchedDataCache.class);
  }


  /**
   * Visually notify users that certain folders have modified content, update the
   * updates' count.
   * @param records can be instances of StatRecord, FileLinkRecords, MsgLinkRecords
   */
  public void statUpdatesInFoldersForVisualNotification(Record[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "statUpdatesInFoldersForVisualNotification(Record[] records)");
    if (trace != null) trace.args(records);

    // Many cache queries are done in possibly not synchronized block so catch
    // any exceptions due to possible inconsistant cache states.
    try {
      // gather all folders that the stats are for, but first fetch the links...
      Long[] objLinkIDs = null;
      FileLinkRecord[] fileLinks = null;
      MsgLinkRecord[] msgLinks = null;
      FolderRecord[] folderRecs = null;

      if (records instanceof StatRecord[]) {
        objLinkIDs = StatRecord.getLinkIDs((StatRecord[]) records);
        fileLinks = getFileLinkRecords(objLinkIDs);
        msgLinks = getMsgLinkRecords(objLinkIDs);
        if (trace != null) trace.data(10, objLinkIDs);
        if (trace != null) trace.data(11, fileLinks);
        if (trace != null) trace.data(12, msgLinks);
      }
      else if (records instanceof FileLinkRecord[]) {
        fileLinks = (FileLinkRecord[]) records;
        if (trace != null) trace.data(13, fileLinks);
      }
      else if (records instanceof MsgLinkRecord[]) {
        msgLinks = (MsgLinkRecord[]) records;
        if (trace != null) trace.data(14, msgLinks);
      }
      else if (records instanceof FolderRecord[]) {
        folderRecs = (FolderRecord[]) records;
        if (trace != null) trace.data(15, folderRecs);
      }

      Long[] folderIDs = null;
      if (fileLinks != null && fileLinks.length > 0) {
        folderIDs = FileLinkRecord.getOwnerObjIDs(fileLinks, Record.RECORD_TYPE_FOLDER);
        if (trace != null) trace.data(16, folderIDs);
      }

      if (msgLinks != null && msgLinks.length > 0) {
        folderIDs = (Long[]) ArrayUtils.concatinate(folderIDs, MsgLinkRecord.getOwnerObjIDs(msgLinks, Record.RECORD_TYPE_FOLDER));
        if (trace != null) trace.data(17, folderIDs);
      }

      if (folderRecs != null && folderRecs.length > 0) {
        folderIDs = (Long[]) ArrayUtils.concatinate(folderIDs, RecordUtils.getIDs(folderRecs));
        if (trace != null) trace.data(18, folderIDs);
      }

      FolderRecord[] updatedFolders = getFolderRecords(folderIDs);
      if (trace != null) trace.data(19, updatedFolders);

      // Go through all folders and count their red flags from the cache.
      if (updatedFolders != null) {
        for (int i=0; i<updatedFolders.length; i++) {
          FolderRecord fRec = updatedFolders[i];
          int statType = -1;
          if (fRec != null) {
            Long[] statIDs = null;
            Record[] links = null;
            if (fRec.isFileType()) {
              links = getFileLinkRecordsOwnerAndType(fRec.folderId, new Short(Record.RECORD_TYPE_FOLDER));
              statType = STAT_TYPE_FILE;
              if (trace != null) trace.data(30, links);
            }
            else if (fRec.isMsgType()) {
              links = getMsgLinkRecordsOwnerAndType(fRec.folderId, new Short(Record.RECORD_TYPE_FOLDER));
              statType = STAT_TYPE_MESSAGE;
              if (trace != null) trace.data(31, links);
            }
            if (links != null && links.length > 0)
              statIDs = RecordUtils.getIDs(links);

            int redFlagCount = 0;
            if (statIDs != null && statIDs.length > 0) {
              // Gather Stats for each folder
              for (int k=0; k<statIDs.length; k++) {
                StatRecord stat = getStatRecord(statIDs[k], statType);
                if (stat != null && stat.mark.equals(StatRecord.FLAG_NEW) && stat.firstDelivered == null)
                  redFlagCount ++;
              }
            }
            boolean suppressSound = fRec.isChatting() && !FolderRecUtil.isOpenChatFolder(fRec.folderId);
            suppressSound |= fRec.folderId.equals(getUserRecord().junkFolderId) || fRec.folderId.equals(getUserRecord().recycleFolderId);
            fRec.setUpdated(redFlagCount, suppressSound);
          }
        } // end for

        // cause the folder listeners to be notified
        if (updatedFolders.length > 0)
          addFolderRecords(updatedFolders);
      }
    } catch (Throwable t) {
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }


  /**
   * We need data Records in the cache before the message table can display contents.
   * For that reason, the event will be fired when we are done with both, links and datas.
   * Adds new records or record updates into the cach and fires appropriate event.
   */
  public void addMsgLinkAndDataRecords(MsgLinkRecord linkRecord, MsgDataRecord dataRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addMsgLinkAndDataRecords(MsgLinkRecord linkRecord, MsgDataRecord dataRecord)");
    addMsgLinkAndDataRecords(new MsgLinkRecord[] { linkRecord }, new MsgDataRecord[] { dataRecord });
    if (trace != null) trace.exit(FetchedDataCache.class);
  }
  public void addMsgLinkAndDataRecords(MsgLinkRecord[] linkRecords, MsgDataRecord[] dataRecords) {
    addMsgLinkAndDataRecords(linkRecords, dataRecords, false);
  }
  public void addMsgLinkAndDataRecords(MsgLinkRecord[] linkRecords, MsgDataRecord[] dataRecords, boolean suppressEventFireing) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addMsgLinkAndDataRecords(MsgLinkRecord[] linkRecords, MsgDataRecord[] dataRecords, boolean suppressEventFireing)");
    if (trace != null) trace.args(linkRecords, dataRecords);
    if (trace != null) trace.args(suppressEventFireing);

    synchronized (this) {
      // unseal the Msg Links and Msg Datas
      if (linkRecords != null && linkRecords.length > 0) {
        unWrapMsgLinkRecords(linkRecords);
        linkRecords = (MsgLinkRecord[]) RecordUtils.merge(msgLinkRecordMap, linkRecords);
        for (int i=0; i<linkRecords.length; i++) msgLinkRecordMap_byMsgId.put(linkRecords[i].msgId, linkRecords[i]);
      }
      if (dataRecords != null && dataRecords.length > 0) {
        unWrapMsgDataRecords(dataRecords);
        dataRecords = (MsgDataRecord[]) RecordUtils.merge(msgDataRecordMap, dataRecords);
      } else if (linkRecords != null && linkRecords.length > 0) {
        // since there are no Msg Datas specified, maybe we should try unWrapping any datas pointed by the link from the cache...
        // this would cover symmetric recrypt case of shared inboxes
        Long[] msgIDs = MsgLinkRecord.getMsgIDs(linkRecords);
        // re-assign the variable, we'll use it in fireing the events
        dataRecords = getMsgDataRecords(msgIDs);
        if (dataRecords != null && dataRecords.length > 0)
          unWrapMsgDataRecords(dataRecords);
      }
    }
    if (!suppressEventFireing) {
      if (linkRecords == null || linkRecords.length == 0) {
        if (dataRecords != null && dataRecords.length > 0) {
          MsgLinkRecord[] relatedLinks = getMsgLinkRecordsForMsgs(RecordUtils.getIDs(dataRecords));
          fireMsgLinkRecordUpdated(relatedLinks, RecordEvent.SET);
        }
      } else {
        fireMsgLinkRecordUpdated(linkRecords, RecordEvent.SET);
      }
      if (dataRecords != null && dataRecords.length > 0) {
        fireMsgDataRecordUpdated(dataRecords, RecordEvent.SET);
      }
    }

    // recalculate flags in the involved folders
    if (linkRecords != null && linkRecords.length > 0) {
      statUpdatesInFoldersForVisualNotification(linkRecords);
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }


  /**
   * Removes records from the cache and fires appropriate event.
   */
  public void removeMsgLinkRecords(MsgLinkRecord[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeMsgLinkRecords(MsgLinkRecord[] records)");
    if (trace != null) trace.args(records);

    if (records != null && records.length > 0) {
      synchronized (this) {
        records = (MsgLinkRecord[]) RecordUtils.remove(msgLinkRecordMap, records);
        if (records != null) {
          // for removed links remove them from secondary hashtable
          for (int i=0; i<records.length; i++) msgLinkRecordMap_byMsgId.remove(records[i].msgId, records[i]);
          // for removed links to Address Records, remove the corresponding AddrHashRecords
          Long[] msgIDs = MsgLinkRecord.getMsgIDs(records);
          removeAddrHashRecords(msgIDs);
        }
      }
      fireMsgLinkRecordUpdated(records, RecordEvent.REMOVE);

      // remove corresponding Stat Records
      removeStatRecords(getStatRecords(RecordUtils.getIDs(records), STAT_TYPE_MESSAGE), false);

      // recalculate flags in the involved folders
      statUpdatesInFoldersForVisualNotification(records);
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }


  /**
   * Removes records from the cache and fires appropriate event.
   */
  public synchronized void removeMsgLinkRecords(Long[] msgLinkIDs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeMsgLinkRecords(Long[] msgLinkIDs)");
    if (trace != null) trace.args(msgLinkIDs);

    MsgLinkRecord[] msgLinks = getMsgLinkRecords(msgLinkIDs);
    if (msgLinks != null && msgLinks.length > 0) {
      removeMsgLinkRecords(msgLinks);
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * @return the requested message link object.
   */
  public synchronized MsgLinkRecord getMsgLinkRecord(Long msgLinkId) {
    return (MsgLinkRecord) msgLinkRecordMap.get(msgLinkId);
  }

  /**
   * @return all Message Link Records from cache.
   */
  public synchronized MsgLinkRecord[] getMsgLinkRecords() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getMsgLinkRecords()");

    Vector msgLinksV = new Vector();
    msgLinksV.addAll(msgLinkRecordMap.values());
    MsgLinkRecord[] msgLinks = (MsgLinkRecord[]) ArrayUtils.toArray(msgLinksV, MsgLinkRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, msgLinks);
    return msgLinks;
  }

  /**
   * @return all Message Link Records for specified message link ids.
   * The records found in the cache are returned, the IDs which do not have
   * corresponding records in the cache are ignored.
   */
  public synchronized MsgLinkRecord[] getMsgLinkRecords(Long[] msgLinkIDs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getMsgLinkRecords(Long[] msgLinkIDs)");
    if (trace != null) trace.args(msgLinkIDs);

    Vector msgLinksV = new Vector();
    if (msgLinkIDs != null) {
      for (int i=0; i<msgLinkIDs.length; i++) {
        MsgLinkRecord msgLink = (MsgLinkRecord) msgLinkRecordMap.get(msgLinkIDs[i]);
        if (msgLink != null)
          msgLinksV.addElement(msgLink);
      }
    }
    MsgLinkRecord[] msgLinks = (MsgLinkRecord[]) ArrayUtils.toArray(msgLinksV, MsgLinkRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, msgLinks);
    return msgLinks;
  }

/**
   * @return all Message Link Records created between specified times.
   * The records found in the cache are returned.
   */
  public synchronized MsgLinkRecord[] getMsgLinkRecords(Date dateCreatedFrom, Date dateCreatedTo, Long ownerObjId, Short ownerObjType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getMsgLinkRecords(Date dateCreatedFrom, Date dateCreatedTo, Long ownerObjId, Short ownerObjType)");
    if (trace != null) trace.args(dateCreatedFrom, dateCreatedTo, ownerObjId, ownerObjType);

    Vector msgLinksV = new Vector();
    Iterator iter = msgLinkRecordMap.values().iterator();
    Date dateFrom = null;
    Date dateTo = null;
    if (dateCreatedFrom.before(dateCreatedTo)) {
      dateFrom = dateCreatedFrom;
      dateTo = dateCreatedTo;
    } else {
      dateFrom = dateCreatedTo;
      dateTo = dateCreatedFrom;
    }
    while (iter.hasNext()) {
      MsgLinkRecord msgLink = (MsgLinkRecord) iter.next();
      if ((ownerObjId == null || ownerObjId.equals(msgLink.ownerObjId)) && (ownerObjType == null || ownerObjType.equals(msgLink.ownerObjType))) {
        if (msgLink.dateCreated.compareTo(dateFrom) >= 0 && msgLink.dateCreated.compareTo(dateTo) <= 0) {
          msgLinksV.addElement(msgLink);
        }
      }
    }
    MsgLinkRecord[] msgLinks = (MsgLinkRecord[]) ArrayUtils.toArray(msgLinksV, MsgLinkRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, msgLinks);
    return msgLinks;
  }

  /**
   * @return all Message Link Records for a given folder id.
   */
  public synchronized MsgLinkRecord[] getMsgLinkRecordsForFolder(Long folderId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getMsgLinkRecordsForFolder(Long folderId)");
    if (trace != null) trace.args(folderId);

    Iterator iter = msgLinkRecordMap.values().iterator();
    Vector msgLinksV = new Vector();
    while (iter.hasNext()) {
      MsgLinkRecord linkRecord = (MsgLinkRecord) iter.next();
      if (linkRecord != null &&
          linkRecord.ownerObjType != null &&
          linkRecord.ownerObjId != null &&
          linkRecord.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER &&
          linkRecord.ownerObjId.equals(folderId))
        msgLinksV.addElement(linkRecord);
    }
    MsgLinkRecord[] msgLinks = (MsgLinkRecord[]) ArrayUtils.toArray(msgLinksV, MsgLinkRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, msgLinks);
    return msgLinks;
  }

  /**
   * @return all Message Link Records for a given folder IDs.
   */
  public synchronized MsgLinkRecord[] getMsgLinkRecordsForFolders(Long[] folderIDs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getMsgLinkRecordsForFolder(Long[] folderIDs)");
    if (trace != null) trace.args(folderIDs);

    Vector msgLinksV = new Vector();
    Hashtable folderIDsHT = new Hashtable();
    for (int i=0; i<folderIDs.length; i++)
      folderIDsHT.put(folderIDs[i], folderIDs[i]);
    Iterator iter = msgLinkRecordMap.values().iterator();
    while (iter.hasNext()) {
      MsgLinkRecord linkRecord = (MsgLinkRecord) iter.next();
      if (linkRecord != null &&
          linkRecord.ownerObjType != null &&
          linkRecord.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER &&
          linkRecord.ownerObjId != null &&
          folderIDsHT.get(linkRecord.ownerObjId) != null
        )
        msgLinksV.addElement(linkRecord);
    }
    MsgLinkRecord[] msgLinks = (MsgLinkRecord[]) ArrayUtils.toArray(msgLinksV, MsgLinkRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, msgLinks);
    return msgLinks;
  }

  /**
   * @return all Message Link Records for a given msg id.
   */
  public synchronized MsgLinkRecord[] getMsgLinkRecordsForMsg(Long msgId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getMsgLinkRecordsForMsg(Long msgId)");
    if (trace != null) trace.args(msgId);

    Vector msgLinksV = msgLinkRecordMap_byMsgId.getAll(msgId);
    MsgLinkRecord[] msgLinks = (MsgLinkRecord[]) ArrayUtils.toArray(msgLinksV, MsgLinkRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, msgLinks);
    return msgLinks;
  }


  /**
   * @return all of my folder shares that belong to specified folders.
   */
  public synchronized MsgLinkRecord[] getMsgLinkRecordsForMsgs(Long[] msgIDs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getMsgLinkRecordsForMsgs(Long[] msgIDs)");
    if (trace != null) trace.args(msgIDs);

    MsgLinkRecord[] records = null;
    if (msgIDs != null) {
      Vector linksV = new Vector();
      for (int i=0; i<msgIDs.length; i++) {
        Vector v = msgLinkRecordMap_byMsgId.getAll(msgIDs[i]);
        if (v != null) linksV.addAll(v);
      }
      records = (MsgLinkRecord[]) ArrayUtils.toArray(linksV, MsgLinkRecord.class);
    }

    if (trace != null) trace.exit(FetchedDataCache.class, records);
    return records;
  }


  /**
   * @return all Message Link Records that are owned by ownerId and type ownerType.
   */
  public synchronized MsgLinkRecord[] getMsgLinkRecordsOwnerAndType(Long ownerId, Short ownerType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getMsgLinkRecordsOwnerAndType(Long ownerId, Short ownerType)");
    if (trace != null) trace.args(ownerId, ownerType);

    Vector msgLinksV = new Vector();
    if (ownerId != null && ownerType != null) {
      Collection coll = msgLinkRecordMap.values();
      if (coll != null && !coll.isEmpty()) {
        Iterator iter = coll.iterator();
        while (iter.hasNext()) {
          MsgLinkRecord linkRecord = (MsgLinkRecord) iter.next();
          if (ownerId.equals(linkRecord.ownerObjId) && ownerType.equals(linkRecord.ownerObjType)) {
            msgLinksV.addElement(linkRecord);
          }
        }
      }
    }
    MsgLinkRecord[] msgLinks = (MsgLinkRecord[]) ArrayUtils.toArray(msgLinksV, MsgLinkRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, msgLinks);
    return msgLinks;
  }


  /**
   * @return all Message Link Records that are owned by ownerIDs and type ownerType.
   */
  public synchronized MsgLinkRecord[] getMsgLinkRecordsOwnersAndType(Long[] ownerIDs, Short ownerType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getMsgLinkRecordsOwnersAndType(Long[] ownerIDs, Short ownerType)");
    if (trace != null) trace.args(ownerIDs, ownerType);

    Vector msgLinksV = new Vector();
    if (ownerIDs != null && ownerIDs.length > 0 && ownerType != null) {
      // load a Hashtable with wanted ownerIDs
      Hashtable ht = new Hashtable();
      for (int i=0; i<ownerIDs.length; i++) {
        ht.put(ownerIDs[i], ownerIDs[i]);
      }
      Collection coll = msgLinkRecordMap.values();
      if (coll != null && !coll.isEmpty()) {
        Iterator iter = coll.iterator();
        while (iter.hasNext()) {
          MsgLinkRecord linkRecord = (MsgLinkRecord) iter.next();
          if (ownerType.equals(linkRecord.ownerObjType)) {
            Long ownerObjId = linkRecord.ownerObjId;
            if (ownerObjId != null && ht.get(ownerObjId) != null) {
              msgLinksV.addElement(linkRecord);
            }
          }
        }
      }
    }
    MsgLinkRecord[] msgLinks = (MsgLinkRecord[]) ArrayUtils.toArray(msgLinksV, MsgLinkRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, msgLinks);
    return msgLinks;
  }


  /************************************
   ***   MsgDataRecord operations   ***
   ************************************/

  /**
   * Adds new records or record updates into the cach and fires appropriate event.
   */
  public void addMsgDataRecords(MsgDataRecord[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addMsgDataRecords(MsgDataRecord[] records)");
    if (trace != null) trace.args(records);

    if (records != null && records.length > 0) {
      synchronized (this) {
        // unSeal Msg Data records
        unWrapMsgDataRecords(records);
        records = (MsgDataRecord[]) RecordUtils.merge(msgDataRecordMap, records);
      }
      fireMsgDataRecordUpdated(records, RecordEvent.SET);
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }
  private void unWrapMsgDataRecords(MsgDataRecord[] dataRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "unWrapMsgDataRecords(MsgDataRecord[] dataRecords)");
    if (trace != null) trace.args(dataRecords);
    for (int i=0; i<dataRecords.length; i++) {
      MsgDataRecord data = dataRecords[i];
      try {
        MsgLinkRecord[] linkRecords = getMsgLinkRecordsForMsg(data.msgId);
        if (linkRecords != null && linkRecords.length > 0) {
          // Find a symmetric key from links that might have been password protected and not unsealed yet...
          BASymmetricKey symmetricKey = null;
          for (int k=0; k<linkRecords.length; k++)
            if (linkRecords[k].getSymmetricKey() != null)
              symmetricKey = linkRecords[k].getSymmetricKey();
          if (symmetricKey != null) {
            // if this data record contains sendPrivKeyId, then signature needs to be verified
            if (data.getSendPrivKeyId() != null) {
              // for performance don't verify everything, do it when person asks to see it
              //            KeyRecord msgSigningKeyRec = getKeyRecord(data.getSendPrivKeyId());
              //            if (msgSigningKeyRec != null)
              //              data.unSeal(linkRecords[0].getSymmetricKey(), msgSigningKeyRec);
              //            else
              // signing key no longer exists, maybe the user account was deleted..., just unseal the message.
              data.unSealWithoutVerify(symmetricKey, bodyKeys);
            } else {
              // unSeal the subject only, don't verify signatures as the text is not available yet
              data.unSealSubject(symmetricKey);
            }
          }
        }
      } catch (Throwable t) {
        if (trace != null) trace.data(100, "Exception occured while processing data", data);
        if (trace != null) trace.exception(FetchedDataCache.class, 101, t);
      }
    }
    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * Removes records from the cache and fires appropriate event.
   */
  public void removeMsgDataRecords(MsgDataRecord[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeMsgDataRecords(MsgDataRecord[] records)");
    if (trace != null) trace.args(records);

    if (records != null && records.length > 0) {
      synchronized (this) {
        records = (MsgDataRecord[]) RecordUtils.remove(msgDataRecordMap, records);
        removeAddrHashRecords(RecordUtils.getIDs(records));
      }
      fireMsgDataRecordUpdated(records, RecordEvent.REMOVE);
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * @return Message Data Record for a given message ID.
   */
  public synchronized MsgDataRecord getMsgDataRecord(Long msgId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getMsgDataRecord(Long msgId)");
    if (trace != null) trace.args(msgId);
    MsgDataRecord dataRecord = (MsgDataRecord) msgDataRecordMap.get(msgId);
    if (trace != null) trace.exit(FetchedDataCache.class, dataRecord);
    return dataRecord;
  }
  public synchronized MsgDataRecord getMsgDataRecordNoTrace(Long msgId) {
    return (MsgDataRecord) msgDataRecordMap.get(msgId);
  }


  /**
   * @return all Message Data Records from cache.
   */
  public synchronized MsgDataRecord[] getMsgDataRecords() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getMsgDataRecords()");

    Vector msgDatasV = new Vector();
    msgDatasV.addAll(msgDataRecordMap.values());
    MsgDataRecord[] msgDatas = (MsgDataRecord[]) ArrayUtils.toArray(msgDatasV, MsgDataRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, msgDatas);
    return msgDatas;
  }


  /**
   * @return Message Data Record for a given message ID.
   */
  public synchronized MsgDataRecord[] getMsgDataRecords(Long[] msgIDs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getMsgDataRecords(Long[] msgIDs)");
    if (trace != null) trace.args(msgIDs);

    Vector msgDatasV = new Vector();
    if (msgIDs != null) {
      for (int i=0; i<msgIDs.length; i++) {
        MsgDataRecord msgData = (MsgDataRecord) msgDataRecordMap.get(msgIDs[i]);
        if (msgData != null)
          msgDatasV.addElement(msgData);
      }
    }
    MsgDataRecord[] msgDatas = (MsgDataRecord[]) ArrayUtils.toArray(msgDatasV, MsgDataRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, msgDatas);
    return msgDatas;
  }

  /**
   * @return Message Data Record for a given filter.
   */
  public synchronized MsgDataRecord[] getMsgDataRecords(MsgFilter msgFilter) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getMsgDataRecords(MsgFilter msgFilter)");
    if (trace != null) trace.args(msgFilter);

    Vector msgDatasV = new Vector();
    Collection col = msgDataRecordMap.values();
    Iterator iter = col.iterator();
    while (iter.hasNext()) {
      MsgDataRecord mData = (MsgDataRecord) iter.next();
      if (msgFilter.keep(mData))
        msgDatasV.addElement(mData);
    }
    MsgDataRecord[] msgDatas = (MsgDataRecord[]) ArrayUtils.toArray(msgDatasV, MsgDataRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, msgDatas);
    return msgDatas;
  }

  /**
   * @return all Message Data Records for specified Message Links.
   */
  public synchronized MsgDataRecord[] getMsgDataRecordsForLinks(Long[] msgLinkIDs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getMsgDataRecordsForLinks(Long[] msgLinkIDs)");
    if (trace != null) trace.args(msgLinkIDs);

    MsgLinkRecord[] msgLinks = getMsgLinkRecords(msgLinkIDs);
    Long[] msgIDs = MsgLinkRecord.getMsgIDs(msgLinks);
    MsgDataRecord[] msgDatas = getMsgDataRecords(msgIDs);

    if (trace != null) trace.exit(FetchedDataCache.class, msgDatas);
    return msgDatas;
  }

  /************************************
   ***   StatRecord operations      ***
   ************************************/

  /**
   * Adds new records or record updates into the cach and fires appropriate event.
   */
  public void addStatRecords(StatRecord[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addStatRecords(StatRecord[])");
    if (trace != null) trace.args(records);

    if (records != null && records.length > 0) {
      StatRecord[][] stats = new StatRecord[3][];
      for (int i=0; i<stats.length; i++) {
        stats[i] = StatRecord.gatherStatsOfType(records, StatRecord.STAT_TYPES[i]);
        if (stats[i] != null && stats[i].length > 0) {
          synchronized (this) {
            stats[i] = (StatRecord[]) RecordUtils.merge(statRecordMaps[i], stats[i]);
          }
        }
      }
      fireStatRecordUpdated(records, RecordEvent.SET);
      statUpdatesInFoldersForVisualNotification(records);
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * Removes records from the cache and fires appropriate event.
   */
  public void removeStatRecords(StatRecord[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeStatRecords(StatRecord[] records)");
    if (trace != null) trace.args(records);

    removeStatRecords(records, false);

    if (trace != null) trace.exit(FetchedDataCache.class);
  }
  public void removeStatRecords(StatRecord[] records, boolean visualNotification) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeStatRecords(StatRecord[])");
    if (trace != null) trace.args(records);

    if (records != null && records.length > 0) {
      StatRecord[][] stats = new StatRecord[3][];
      for (int i=0; i<stats.length; i++) {
        stats[i] = StatRecord.gatherStatsOfType(records, StatRecord.STAT_TYPES[i]);
        if (stats[i] != null && stats[i].length > 0) {
          synchronized (this) {
            stats[i] = (StatRecord[]) RecordUtils.remove(statRecordMaps[i], stats[i]);
          }
        }
      }

      fireStatRecordUpdated(records, RecordEvent.REMOVE);

      if (visualNotification)
        statUpdatesInFoldersForVisualNotification(records);
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * @return Stat Record for a given Link ID (statId is the linkId for client purposes)
   */
  public synchronized StatRecord getStatRecord(Long statId, int statType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getStatRecord(Long statId, int statType)");
    if (trace != null) trace.args(statId);
    if (trace != null) trace.args(statType);

    StatRecord statRecord = (StatRecord) statRecordMaps[statType].get(statId);

    if (trace != null) trace.exit(FetchedDataCache.class, statRecord);
    return statRecord;
  }


  /**
   * @return all StatRecords from cache for a given type
   */
  public synchronized StatRecord[] getStatRecords(int statType) {
    Vector statsV = new Vector();
    statsV.addAll(statRecordMaps[statType].values());
    StatRecord[] stats = (StatRecord[]) ArrayUtils.toArray(statsV, StatRecord.class);
    return stats;
  }

  /**
   * @return all StatRecords from cache
   */
  public synchronized StatRecord[] getStatRecords() {
    Vector statsV = new Vector();
    for (int i=0; i<3; i++)
      statsV.addAll(statRecordMaps[i].values());
    StatRecord[] stats = (StatRecord[]) ArrayUtils.toArray(statsV, StatRecord.class);
    return stats;
  }


  /**
   * @return all StatRecords from cache with specified IDs
   */
  public synchronized StatRecord[] getStatRecords(Long[] statIDs, int statType) {
    Vector statsV = new Vector();
    if (statIDs != null) {
      for (int i=0; i<statIDs.length; i++) {
        StatRecord rec = (StatRecord) statRecordMaps[statType].get(statIDs[i]);
        if (rec != null)
          statsV.addElement(rec);
      }
    }
    StatRecord[] stats = (StatRecord[]) ArrayUtils.toArray(statsV, StatRecord.class);
    return stats;
  }



  /************************************
   ***   EmailRecord operations     ***
   ************************************/

  /**
   * Adds new records or record updates into the cach and fires appropriate event.
   */
  public void addEmailRecords(EmailRecord[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addEmailRecords(EmailRecord[])");
    if (trace != null) trace.args(records);

    if (records != null && records.length > 0) {
      synchronized (this) {
        records = (EmailRecord[]) RecordUtils.merge(emailRecordMap, records);
      }
      fireEmailRecordUpdated(records, RecordEvent.SET);
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * Removes records from the cache and fires appropriate event.
   */
  public void removeEmailRecords(EmailRecord[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeEmailRecords(EmailRecord[] records)");
    if (trace != null) trace.args(records);

    if (records != null && records.length > 0) {
      synchronized (this) {
        records = (EmailRecord[]) RecordUtils.remove(emailRecordMap, records);
      }
      fireEmailRecordUpdated(records, RecordEvent.REMOVE);
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * Removes records from the cache and fires appropriate event.
   */
  public void removeEmailRecords(Long[] emlIDs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeEmailRecords(Long[] emlIDs)");
    if (trace != null) trace.args(emlIDs);

    EmailRecord[] records = getEmailRecords(emlIDs);
    removeEmailRecords(records);

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * @return EmailRecord for a given ID
   */
  public synchronized EmailRecord getEmailRecord(Long emlId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getEmailRecord(Long emlId)");
    if (trace != null) trace.args(emlId);

    EmailRecord emailRecord = (EmailRecord) emailRecordMap.get(emlId);

    if (trace != null) trace.exit(FetchedDataCache.class, emailRecord);
    return emailRecord;
  }

  /**
   * @return EmailRecord for a given ID
   */
  public synchronized EmailRecord getEmailRecord(String emlAddr) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getEmailRecord(String emlAddr)");
    if (trace != null) trace.args(emlAddr);

    EmailRecord emlRec = null;
    Iterator iter = emailRecordMap.values().iterator();
    while (iter.hasNext()) {
      EmailRecord rec = (EmailRecord) iter.next();
      if (EmailRecord.isAddressEqual(rec.emailAddr, emlAddr)) {
        emlRec = rec;
        break;
      }
    }

    if (trace != null) trace.exit(FetchedDataCache.class, emlRec);
    return emlRec;
  }

  /**
   * @return all EmailRecords from cache
   */
  public synchronized EmailRecord[] getEmailRecords() {
    Vector emailsV = new Vector();
    emailsV.addAll(emailRecordMap.values());
    EmailRecord[] emails = (EmailRecord[]) ArrayUtils.toArray(emailsV, EmailRecord.class);
    return emails;
  }

  /**
   * @return all EmailRecords from cache with specified IDs
   */
  public synchronized EmailRecord[] getEmailRecords(Long userId) {
    Vector emailsV = new Vector();
    if (userId != null) {
      Iterator iter = emailRecordMap.values().iterator();
      while (iter.hasNext()) {
        EmailRecord rec = (EmailRecord) iter.next();
        if (rec.userId.equals(userId))
          emailsV.addElement(rec);
      }
    }
    EmailRecord[] emails = (EmailRecord[]) ArrayUtils.toArray(emailsV, EmailRecord.class);
    return emails;
  }

  /**
   * @return all EmailRecords from cache with specified IDs
   */
  public synchronized EmailRecord[] getEmailRecords(Long[] emailIDs) {
    Vector emailsV = new Vector();
    if (emailIDs != null) {
      for (int i=0; i<emailIDs.length; i++) {
        EmailRecord sRec = (EmailRecord) emailRecordMap.get(emailIDs[i]);
        if (sRec != null)
          emailsV.addElement(sRec);
      }
    }
    EmailRecord[] emails = (EmailRecord[]) ArrayUtils.toArray(emailsV, EmailRecord.class);
    return emails;
  }


  /***************************************
   ***   AddrHashRecord operations     ***
   ***************************************/

  /**
   * Adds new records or record updates into the cach and fires appropriate event.
   */
  public void addAddrHashRecords(AddrHashRecord[] records) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addAddrHashRecords(AddrHashRecord[] records)");
    if (trace != null) trace.args(records);

    if (records != null && records.length > 0) {
      synchronized (this) {
        for (int i=0; records!=null && i<records.length; i++) {
          addrHashRecordMap_byMsgId.put(records[i].msgId, records[i]);
          addrHashRecordMap_byHash.put(records[i].hash.getHexContent(), records[i]); // for some strange reason byte[] doesn't work as key, so use String equivalent instead
        }
      }
      //fireAddrHashRecordUpdated(records, RecordEvent.SET);
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * Removes records from the cache and fires appropriate event. -- this is difficult because of 1 to many relationship between msgId and hash... leave this for now
   */
  public void removeAddrHashRecords(Long[] msgIDs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeAddrHashRecords(Long[] msgIDs)");
    if (trace != null) trace.args(msgIDs);

    if (msgIDs != null && msgIDs.length > 0) {
      synchronized (this) {
        for (int i=0; i<msgIDs.length; i++) {
          // remove the first element to find out the hash, then remove all other for the same key...
          AddrHashRecord addrHashRecord = (AddrHashRecord) addrHashRecordMap_byMsgId.remove(msgIDs[i]);
          if (addrHashRecord != null) {
            addrHashRecordMap_byMsgId.removeAll(msgIDs[i]);
            addrHashRecordMap_byHash.removeAll(addrHashRecord.hash.getHexContent());
          }
        }
      }
      //fireAddrHashRecordUpdated(records, RecordEvent.REMOVE);
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /**
   * @return AddrHashRecord for a given ID
   */
  public synchronized AddrHashRecord[] getAddrHashRecordsForMsgId(Long msgId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getAddrHashRecordsForMsgId(Long msgId)");
    if (trace != null) trace.args(msgId);

    Vector addrHashRecordsV = addrHashRecordMap_byMsgId.getAll(msgId);
    AddrHashRecord[] addrHashRecords = (AddrHashRecord[]) ArrayUtils.toArray(addrHashRecordsV, AddrHashRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, addrHashRecords);
    return addrHashRecords;
  }

  /**
   * @return AddrHashRecord for a given hash
   */
  public synchronized AddrHashRecord[] getAddrHashRecords(byte[] hash) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getAddrHashRecords(byte[] hash)");
    if (trace != null) trace.args(hash);

    AddrHashRecord[] addrHashRecords = null;
    if (hash != null)
      addrHashRecords = getAddrHashRecords(new BADigestBlock(hash));

    if (trace != null) trace.exit(FetchedDataCache.class, addrHashRecords);
    return addrHashRecords;
  }

  /**
   * @return AddrHashRecord for a given hash
   */
  public synchronized AddrHashRecord[] getAddrHashRecords(BADigestBlock hash) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getAddrHashRecords(BADigestBlock hash)");
    if (trace != null) trace.args(hash);

    String hashHex = hash.getHexContent();
    Vector addrHashRecordsV = addrHashRecordMap_byHash.getAll(hashHex);
    AddrHashRecord[] addrHashRecords = (AddrHashRecord[]) ArrayUtils.toArray(addrHashRecordsV, AddrHashRecord.class);

    if (trace != null) trace.exit(FetchedDataCache.class, addrHashRecords);
    return addrHashRecords;
  }

  /**
   * @return AddrHashRecord for a given email string
   */
  public synchronized AddrHashRecord[] getAddrHashRecords(String emailAddr) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getAddrHashRecords(String emailAddr)");
    if (trace != null) trace.args(emailAddr);

    byte[] hash = getAddrHashForEmail(emailAddr);
    AddrHashRecord[] addrHashRecords = null;
    if (hash != null)
      addrHashRecords = getAddrHashRecords(hash);

    if (trace != null) trace.exit(FetchedDataCache.class, addrHashRecords);
    return addrHashRecords;
  }

  /**
   * @return Address Record for a given hash
   */
  public synchronized MsgDataRecord[] getAddrRecords(byte[] hash) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getAddrRecords(byte[] hash)");
    if (trace != null) trace.args(hash);

    AddrHashRecord[] addrHashRecords = getAddrHashRecords(hash);
    MsgDataRecord[] addrRecords = null;
    if (addrHashRecords != null) {
      Long[] msgIDs = AddrHashRecord.getMsgIDs(addrHashRecords);
      addrRecords = getMsgDataRecords(msgIDs);
    }

    if (trace != null) trace.exit(FetchedDataCache.class, addrRecords);
    return addrRecords;
  }

  /**
   * @return Address Record for a given hash looking between specified msgIDs
   */
  public synchronized MsgDataRecord[] getAddrRecords(byte[] hash, Long[] fromMsgIDs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getAddrRecords(byte[] hash, Long[] fromMsgIDs)");
    if (trace != null) trace.args(hash, fromMsgIDs);

    MsgDataRecord[] addrRecords = null;
    if (fromMsgIDs == null) {
      addrRecords = getAddrRecords(hash);
    } else {
      // if hash exists then look for it between specified msgIDs
      if (getAddrHashRecords(hash) != null) {
        BADigestBlock hashBA = new BADigestBlock(hash);
        Vector msgIDsV = new Vector();
        for (int i=0; i<fromMsgIDs.length; i++) {
          Vector addrHashRecordsV = addrHashRecordMap_byMsgId.getAll(fromMsgIDs[i]);
          // all these records must have the same msgId so find only the first match
          if (addrHashRecordsV != null) {
            for (int k=0; k<addrHashRecordsV.size(); k++) {
              AddrHashRecord addrHashRecord = (AddrHashRecord) addrHashRecordsV.elementAt(k);
              if (addrHashRecord.hash.equals(hashBA) && !msgIDsV.contains(addrHashRecord.msgId)) {
                msgIDsV.addElement(addrHashRecord.msgId);
                // break on first match, because we are finding UNIQUE msgIDs
                break;
              }
            }
          }
        }
        if (msgIDsV.size() > 0) {
          addrRecords = getMsgDataRecords((Long[]) ArrayUtils.toArray(msgIDsV, Long.class));
        }
      }
    }

    if (trace != null) trace.exit(FetchedDataCache.class, addrRecords);
    return addrRecords;
  }

  /**
   * @return Address Record for a given email string
   */
  public synchronized MsgDataRecord[] getAddrRecords(String emailAddr) {
    return getAddrRecords(emailAddr, null);
  }

  /**
   * @return Address Record for a given email string
   */
  public synchronized MsgDataRecord[] getAddrRecords(String emailAddr, Long[] fromMsgIDs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "getAddrRecords(String emailAddr, Long[] fromMsgIDs)");
    if (trace != null) trace.args(emailAddr);

    byte[] hash = getAddrHashForEmail(emailAddr);
    MsgDataRecord[] addrRecords = null;
    if (hash != null)
      addrRecords = fromMsgIDs != null ? getAddrRecords(hash, fromMsgIDs) : getAddrRecords(hash);

    if (trace != null) trace.exit(FetchedDataCache.class, addrRecords);
    return addrRecords;
  }

  // normalize email address to a hash value
  public static byte[] getAddrHashForEmail(String emailAddr) {
    byte[] hash = null;
    if (emailAddr != null) {
      String[] addrs = EmailRecord.gatherAddresses(emailAddr);
      if (addrs != null && addrs.length > 0) {
        emailAddr = addrs[addrs.length-1].trim().toLowerCase(Locale.US);
        MessageDigest messageDigest = null;
        try {
          messageDigest = MessageDigest.getInstance("MD5");
          hash = messageDigest.digest(emailAddr.getBytes());
        } catch (Throwable t) {
          throw new IllegalStateException("Could not create MessageDigest.");
        }
      }
    }
    return hash;
  }

  //===========================================================================
  //=====================   L I S T E N E R S  ================================
  //===========================================================================

  /****************************************
   ***   UserRecord Listener handling   ***
   ****************************************/

  public synchronized void addUserRecordListener(UserRecordListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addUserRecordListener(UserRecordListener)");
    if (trace != null) trace.args(l);
    myListenerList.add(UserRecordListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  public synchronized void removeUserRecordListener(UserRecordListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeUserRecordListener(UserRecordListener)");
    if (trace != null) trace.args(l);
    myListenerList.remove(UserRecordListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }


  /**
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   */
  protected void fireUserRecordUpdated(UserRecord[] records, int eventType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "fireUserRecordUpdated(UserRecord[], int eventType)");
    if (trace != null) trace.args(records);
    if (trace != null) trace.args(eventType);

    if (!DEBUG__SUPPRESS_EVENTS_USERS) {
      // Guaranteed to return a non-null array
      Object[] listeners = myListenerList.getListenerList();
      UserRecordEvent e = null;
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length-2; i>=0; i-=2) {
        if (listeners[i] == UserRecordListener.class) {
          // Lazily create the event:
          if (e == null)
            e = new UserRecordEvent(this, records, eventType);
          int oldPriority = Thread.currentThread().getPriority();
          Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
          ((UserRecordListener)listeners[i+1]).userRecordUpdated(e);
          Thread.currentThread().setPriority(oldPriority);
        }
      }
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }


  /************************************************
   ***   UserSettingsRecord Listener handling   ***
   ************************************************/

  public synchronized void addUserSettingsRecordListener(UserSettingsRecordListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addUserSettingsRecordListener(UserSettingsRecordListener)");
    if (trace != null) trace.args(l);
    myListenerList.add(UserSettingsRecordListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  public synchronized void removeUserSettingsRecordListener(UserSettingsRecordListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeUserSettingsRecordListener(UserSettingsRecordListener)");
    if (trace != null) trace.args(l);
    myListenerList.remove(UserSettingsRecordListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }


  /**
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   */
  protected void fireUserSettingsRecordUpdated(UserSettingsRecord[] records, int eventType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "fireUserSettingsRecordUpdated(UserSettingsRecord[], int eventType)");
    if (trace != null) trace.args(records);
    if (trace != null) trace.args(eventType);

    if (!DEBUG__SUPPRESS_EVENTS_USERS) {
      // Guaranteed to return a non-null array
      Object[] listeners = myListenerList.getListenerList();
      UserSettingsRecordEvent e = null;
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length-2; i>=0; i-=2) {
        if (listeners[i] == UserSettingsRecordListener.class) {
          // Lazily create the event:
          if (e == null)
            e = new UserSettingsRecordEvent(this, records, eventType);
          int oldPriority = Thread.currentThread().getPriority();
          Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
          ((UserSettingsRecordListener)listeners[i+1]).userSettingsRecordUpdated(e);
          Thread.currentThread().setPriority(oldPriority);
        }
      }
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }


  /******************************************
   ***   FolderRecord Listener handling   ***
   ******************************************/

  public synchronized void addFolderRecordListener(FolderRecordListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addFolderRecordListener(FolderRecordListener)");
    if (trace != null) trace.args(l);
    myListenerList.add(FolderRecordListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  public synchronized void removeFolderRecordListener(FolderRecordListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeFolderRecordListener(FolderRecordListener)");
    if (trace != null) trace.args(l);
    myListenerList.remove(FolderRecordListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }


  /**
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   */
  public void fireFolderRecordUpdated(FolderRecord[] records, int eventType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "fireFolderRecordUpdated(FolderRecord[], int eventType)");
    if (trace != null) trace.args(records);
    if (trace != null) trace.args(eventType);

    if (!DEBUG__SUPPRESS_EVENTS_FOLDERS) {
      // Guaranteed to return a non-null array
      Object[] listeners = myListenerList.getListenerList();
      FolderRecordEvent e = null;
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length-2; i>=0; i-=2) {
        if (listeners[i] == FolderRecordListener.class) {
          // Lazily create the event:
          if (e == null)
            e = new FolderRecordEvent(this, records, eventType);
          int oldPriority = Thread.currentThread().getPriority();
          Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
          ((FolderRecordListener)listeners[i+1]).folderRecordUpdated(e);
          Thread.currentThread().setPriority(oldPriority);
        }
      }
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }



  /***********************************************
   ***   FolderShareRecord Listener handling   ***
   ***********************************************/

  public synchronized void addFolderShareRecordListener(FolderShareRecordListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addFolderShareRecordListener(FolderShareRecordListener)");
    if (trace != null) trace.args(l);
    myListenerList.add(FolderShareRecordListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  public synchronized void removeFolderShareRecordListener(FolderShareRecordListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeFolderShareRecordListener(FolderShareRecordListener)");
    if (trace != null) trace.args(l);
    myListenerList.remove(FolderShareRecordListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }


  /**
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   */
  protected void fireFolderShareRecordUpdated(FolderShareRecord[] records, int eventType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "fireFolderShareRecordUpdated(FolderShareRecord[], int eventType)");
    if (trace != null) trace.args(records);
    if (trace != null) trace.args(eventType);

    if (!DEBUG__SUPPRESS_EVENTS_FOLDERS) {
      // Guaranteed to return a non-null array
      Object[] listeners = myListenerList.getListenerList();
      FolderShareRecordEvent e = null;
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length-2; i>=0; i-=2) {
        if (listeners[i] == FolderShareRecordListener.class) {
          // Lazily create the event:
          if (e == null)
            e = new FolderShareRecordEvent(this, records, eventType);
          int oldPriority = Thread.currentThread().getPriority();
          Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
          ((FolderShareRecordListener)listeners[i+1]).folderShareRecordUpdated(e);
          Thread.currentThread().setPriority(oldPriority);
        }
      }
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }



  /******************************************
   ***   FileLinkRecord Listener handling   ***
   ******************************************/

  public synchronized void addFileLinkRecordListener(FileLinkRecordListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addFileLinkRecordListener(FileLinkRecordListener)");
    if (trace != null) trace.args(l);
    myListenerList.add(FileLinkRecordListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  public synchronized void removeFileLinkRecordListener(FileLinkRecordListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeFolderRecordListener(FolderRecordListener)");
    if (trace != null) trace.args(l);
    myListenerList.remove(FileLinkRecordListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }


  /**
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   */
  protected void fireFileLinkRecordUpdated(FileLinkRecord[] records, int eventType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "fireFileLinkRecordUpdated(FileLinkRecord[], int eventType)");
    if (trace != null) trace.args(records);
    if (trace != null) trace.args(eventType);

    if (!DEBUG__SUPPRESS_EVENTS_FILES) {
      // Guaranteed to return a non-null array
      Object[] listeners = myListenerList.getListenerList();
      FileLinkRecordEvent e = null;
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length-2; i>=0; i-=2) {
        if (listeners[i] == FileLinkRecordListener.class) {
          // Lazily create the event:
          if (e == null)
            e = new FileLinkRecordEvent(this, records, eventType);
          int oldPriority = Thread.currentThread().getPriority();
          Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
          ((FileLinkRecordListener)listeners[i+1]).fileLinkRecordUpdated(e);
          Thread.currentThread().setPriority(oldPriority);
        }
      }
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }



  /***************************************
   ***   KeyRecord Listener handling   ***
   ***************************************/

  public synchronized void addKeyRecordListener(KeyRecordListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addKeyRecordListener(KeyRecordListener)");
    if (trace != null) trace.args(l);
    myListenerList.add(KeyRecordListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  public synchronized void removeKeyRecordListener(KeyRecordListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeKeyRecordListener(KeyRecordListener)");
    if (trace != null) trace.args(l);
    myListenerList.remove(KeyRecordListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }


  /**
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   */
  protected void fireKeyRecordUpdated(KeyRecord[] records, int eventType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "fireKeyRecordUpdated(KeyRecord[], int eventType)");
    if (trace != null) trace.args(records);
    if (trace != null) trace.args(eventType);

    if (!DEBUG__SUPPRESS_EVENTS_KEYS) {
      // Guaranteed to return a non-null array
      Object[] listeners = myListenerList.getListenerList();
      KeyRecordEvent e = null;
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length-2; i>=0; i-=2) {
        if (listeners[i] == KeyRecordListener.class) {
          // Lazily create the event:
          if (e == null)
            e = new KeyRecordEvent(this, records, eventType);
          int oldPriority = Thread.currentThread().getPriority();
          Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
          ((KeyRecordListener)listeners[i+1]).keyRecordUpdated(e);
          Thread.currentThread().setPriority(oldPriority);
        }
      }
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }



  /*******************************************
   ***   ContactRecord Listener handling   ***
   *******************************************/

  public synchronized void addContactRecordListener(ContactRecordListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addContactRecordListener(ContactRecordListener)");
    if (trace != null) trace.args(l);
    myListenerList.add(ContactRecordListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  public synchronized void removeContactRecordListener(ContactRecordListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeContactRecordListener(ContactRecordListener)");
    if (trace != null) trace.args(l);
    myListenerList.remove(ContactRecordListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }


  /**
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   */
  protected void fireContactRecordUpdated(ContactRecord[] records, int eventType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "fireContactRecordUpdated(ContactRecord[], int eventType)");
    if (trace != null) trace.args(records);
    if (trace != null) trace.args(eventType);

    if (!DEBUG__SUPPRESS_EVENTS_CONTACTS) {
      // Guaranteed to return a non-null array
      Object[] listeners = myListenerList.getListenerList();
      ContactRecordEvent e = null;
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length-2; i>=0; i-=2) {
        if (listeners[i] == ContactRecordListener.class) {
          // Lazily create the event:
          if (e == null)
            e = new ContactRecordEvent(this, records, eventType);
          int oldPriority = Thread.currentThread().getPriority();
          Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
          ((ContactRecordListener)listeners[i+1]).contactRecordUpdated(e);
          Thread.currentThread().setPriority(oldPriority);
        }
      }
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }




  /*******************************************
   ***   MsgLinkRecord Listener handling   ***
   *******************************************/

  public synchronized void addMsgLinkRecordListener(MsgLinkRecordListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addMsgLinkRecordListener(MsgLinkRecordListener l)");
    if (trace != null) trace.args(l);
    myListenerList.add(MsgLinkRecordListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  public synchronized void removeMsgLinkRecordListener(MsgLinkRecordListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeMsgLinkRecordListener(MsgLinkRecordListener l)");
    if (trace != null) trace.args(l);
    myListenerList.remove(MsgLinkRecordListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }


  /**
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   * Mostly called internally when records are added or removed.
   */
  public void fireMsgLinkRecordUpdated(MsgLinkRecord[] records, int eventType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "fireMsgLinkRecordUpdated(MsgLinkRecord[] records, int eventType)");
    if (trace != null) trace.args(records);
    if (trace != null) trace.args(eventType);

    if (!DEBUG__SUPPRESS_EVENTS_MSGS) {
      // Guaranteed to return a non-null array
      Object[] listeners = myListenerList.getListenerList();
      MsgLinkRecordEvent e = null;
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length-2; i>=0; i-=2) {
        if (listeners[i] == MsgLinkRecordListener.class) {
          // Lazily create the event:
          if (e == null)
            e = new MsgLinkRecordEvent(this, records, eventType);
          int oldPriority = Thread.currentThread().getPriority();
          Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
          ((MsgLinkRecordListener)listeners[i+1]).msgLinkRecordUpdated(e);
          Thread.currentThread().setPriority(oldPriority);
        }
      }
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }



  /*******************************************
   ***   MsgDataRecord Listener handling   ***
   *******************************************/

  public synchronized void addMsgDataRecordListener(MsgDataRecordListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addMsgDataRecordListener(MsgDataRecordListener l)");
    if (trace != null) trace.args(l);
    myListenerList.add(MsgDataRecordListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  public synchronized void removeMsgDataRecordListener(MsgDataRecordListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeMsgDataRecordListener(MsgDataRecordListener l)");
    if (trace != null) trace.args(l);
    myListenerList.remove(MsgDataRecordListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }


  /**
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   * Mostly called internally when records are added or removed.
   */
  public void fireMsgDataRecordUpdated(MsgDataRecord[] records, int eventType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "fireMsgDataRecordUpdated(MsgDataRecord[] records, int eventType)");
    if (trace != null) trace.args(records);
    if (trace != null) trace.args(eventType);

    if (!DEBUG__SUPPRESS_EVENTS_MSGS) {
      // Guaranteed to return a non-null array
      Object[] listeners = myListenerList.getListenerList();
      MsgDataRecordEvent e = null;
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length-2; i>=0; i-=2) {
        if (listeners[i] == MsgDataRecordListener.class) {
          // Lazily create the event:
          if (e == null)
            e = new MsgDataRecordEvent(this, records, eventType);
          int oldPriority = Thread.currentThread().getPriority();
          Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
          ((MsgDataRecordListener)listeners[i+1]).msgDataRecordUpdated(e);
          Thread.currentThread().setPriority(oldPriority);
        }
      }
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  /*******************************************
   ***   StatRecord Listener handling      ***
   *******************************************/

  public synchronized void addStatRecordListener(StatRecordListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addStatRecordListener(StatRecordListener)");
    if (trace != null) trace.args(l);
    myListenerList.add(StatRecordListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  public synchronized void removeStatRecordListener(StatRecordListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeStatRecordListener(StatRecordListener)");
    if (trace != null) trace.args(l);
    myListenerList.remove(StatRecordListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }


  /**
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   */
  protected void fireStatRecordUpdated(StatRecord[] records, int eventType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "fireStatRecordUpdated(StatRecord[], int eventType)");
    if (trace != null) trace.args(records);
    if (trace != null) trace.args(eventType);

    if (!DEBUG__SUPPRESS_EVENTS_STATS) {
      // Guaranteed to return a non-null array
      Object[] listeners = myListenerList.getListenerList();
      StatRecordEvent e = null;
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length-2; i>=0; i-=2) {
        if (listeners[i] == StatRecordListener.class) {
          // Lazily create the event:
          if (e == null)
            e = new StatRecordEvent(this, records, eventType);
          int oldPriority = Thread.currentThread().getPriority();
          Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
          ((StatRecordListener)listeners[i+1]).statRecordUpdated(e);
          Thread.currentThread().setPriority(oldPriority);
        }
      }
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }


  /*******************************************
   ***   EmailRecord Listener handling     ***
   *******************************************/

  public synchronized void addEmailRecordListener(EmailRecordListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addEmailRecordListener(EmailRecordListener)");
    if (trace != null) trace.args(l);
    myListenerList.add(EmailRecordListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  public synchronized void removeEmailRecordListener(EmailRecordListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeEmailRecordListener(EmailRecordListener)");
    if (trace != null) trace.args(l);
    myListenerList.remove(EmailRecordListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }


  /**
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   */
  protected void fireEmailRecordUpdated(EmailRecord[] records, int eventType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "fireEmailRecordUpdated(EmailRecord[], int eventType)");
    if (trace != null) trace.args(records);
    if (trace != null) trace.args(eventType);

    if (!DEBUG__SUPPRESS_EVENTS_STATS) {
      // Guaranteed to return a non-null array
      Object[] listeners = myListenerList.getListenerList();
      EmailRecordEvent e = null;
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length-2; i>=0; i-=2) {
        if (listeners[i] == EmailRecordListener.class) {
          // Lazily create the event:
          if (e == null)
            e = new EmailRecordEvent(this, records, eventType);
          int oldPriority = Thread.currentThread().getPriority();
          Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
          ((EmailRecordListener)listeners[i+1]).emailRecordUpdated(e);
          Thread.currentThread().setPriority(oldPriority);
        }
      }
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }


  /*********************************************
   ***   FldRingRing Listener handling       ***
   *********************************************/

  public synchronized void addFolderRingListener(FolderRingListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addFolderRingListener(FolderRingListener))");
    if (trace != null) trace.args(l);
    myListenerList.add(FolderRingListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  public synchronized void removeFolderRingListener(FolderRingListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeFolderRingListener(FolderRingListener)");
    if (trace != null) trace.args(l);
    myListenerList.remove(FolderRingListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }


  /**
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   */
  public void fireFolderRingEvent(Obj_List_Co source) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "fireFolderRingEvent(Obj_List_Co source)");
    if (trace != null) trace.args(source);

    // Guaranteed to return a non-null array
    Object[] listeners = myListenerList.getListenerList();
    EventObject e = null;
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length-2; i>=0; i-=2) {
      if (listeners[i] == FolderRingListener.class) {
        // Lazily create the event:
        if (e == null)
          e = new EventObject(source);
        int oldPriority = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
        ((FolderRingListener)listeners[i+1]).fldRingRingUpdate(e);
        Thread.currentThread().setPriority(oldPriority);
      }
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }


  /*******************************************
   ***   MsgTyping Listener handling       ***
   *******************************************/

  public synchronized void addMsgTypingListener(MsgTypingListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "addMsgTypingListener(MsgTypingListener)");
    if (trace != null) trace.args(l);
    myListenerList.add(MsgTypingListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }

  public synchronized void removeMsgTypingListener(MsgTypingListener l) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "removeMsgTypingListener(MsgTypingListener)");
    if (trace != null) trace.args(l);
    myListenerList.remove(MsgTypingListener.class, l);
    if (trace != null) trace.exit(FetchedDataCache.class);
  }


  /**
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   */
  public void fireMsgTypingEvent(Obj_List_Co source) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FetchedDataCache.class, "fireMsgTypingEvent(Obj_List_Co source)");
    if (trace != null) trace.args(source);

    // Guaranteed to return a non-null array
    Object[] listeners = myListenerList.getListenerList();
    EventObject e = null;
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length-2; i>=0; i-=2) {
      if (listeners[i] == MsgTypingListener.class) {
        // Lazily create the event:
        if (e == null)
          e = new EventObject(source);
        int oldPriority = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
        ((MsgTypingListener)listeners[i+1]).msgTypingUpdate(e);
        Thread.currentThread().setPriority(oldPriority);
      }
    }

    if (trace != null) trace.exit(FetchedDataCache.class);
  }

}