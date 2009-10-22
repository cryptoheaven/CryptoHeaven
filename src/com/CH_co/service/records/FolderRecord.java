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

package com.CH_co.service.records;

import java.sql.Timestamp;
import java.util.*;
import javax.swing.Icon;

import com.CH_co.util.*;
import com.CH_co.trace.Trace;

/** 
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class FolderRecord extends Record {

  public static final short INTERNAL_ANY_SHARED_FOLDER = 0; // For internal use only, not a DB type
  public static final short FILE_FOLDER = 1;
  public static final short MESSAGE_FOLDER = 2;
  public static final short POSTING_FOLDER = 3;
  public static final short CONTACT_FOLDER = 4;
  public static final short KEY_FOLDER = 5;
  public static final short LOCAL_FILES_FOLDER = 6; // used for starting JFileChooser for browsing local file system
  public static final short ADDRESS_FOLDER = 7;
  public static final short GROUP_FOLDER = 8;
  public static final short WHITELIST_FOLDER = 9;
  public static final short CHATTING_FOLDER = 10;
  public static final short RECYCLE_FOLDER = 11;

  public static final short CATEGORY_MAIL_FOLDER = 20;
  public static final short CATEGORY_FILE_FOLDER = 21;
  public static final short CATEGORY_CHAT_FOLDER = 22;
  public static final short CATEGORY_GROUP_FOLDER = 23;

  public static final String FILE_FOLDER_STR = "File Folder";
  public static final String MESSAGE_FOLDER_STR = "Message Folder";
  public static final String POSTING_FOLDER_STR = "Posting Folder";
  public static final String CHATTING_FOLDER_STR = "Chatting Folder";
  public static final String CONTACT_FOLDER_STR = "Contact Folder";
  public static final String KEY_FOLDER_STR = "Key Folder";
  public static final String LOCAL_FILES_FOLDER_STR = "My Local Computer";
  public static final String ADDRESS_FOLDER_STR = "Address Book";
  public static final String GROUP_FOLDER_STR = "Group Folder";
  public static final String WHITELIST_FOLDER_STR = "WhiteList Folder";
  public static final String RECYCLE_FOLDER_STR = "Recycle Bin";

  public static final short ATTRIBUTES_BLANK = 0;

  public static final long FOLDER_LOCAL_ID = -500;

  public static final long CATEGORY_MAIL_ID = -400;
  public static final long CATEGORY_FILE_ID = -300;
  public static final long CATEGORY_CHAT_ID = -200;
  public static final long CATEGORY_GROUP_ID = -100;

  public Long folderId;
  public Long parentFolderId;
  public Long ownerUserId;
  public Short folderType;
  public Short numToKeep;
  public Integer keepAsOldAs; // expressed in seconds
  public Short numOfShares;
  public Timestamp dateCreated;
  public Timestamp dateUpdated;


  // Number of updates in this folder.
  private int numOfUpdates;
  // Cache folder rendering notes
  private String cachedOwnerNote;
  private String cachedChatNote;
  private String cachedDisplayText;
  private String cachedToolTip;

  // Cached folder object count
  public Long objectCount;

  /** Creates new FolderRecord */
  public FolderRecord() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderRecord.class, "FolderRecord()");
    if (trace != null) trace.exit(FolderRecord.class);
  }

  public Long getId() {
    return folderId;
  }

  public Icon getIcon() {
    return getIcon(false, null);
  }

  public Icon getIcon(boolean selected, UserRecord uRec) {
    Icon icon = null;
    short numShares = numOfShares.shortValue();
    switch (folderType.shortValue()) {
      case LOCAL_FILES_FOLDER :
        icon = Images.get(ImageNums.MY_COMPUTER16);
        break;
      case ADDRESS_FOLDER :
      case WHITELIST_FOLDER :
        if (numShares > 1) {
          if (selected)
            //icon = Images.get(ImageNums.FLD_ADDR_OPEN_SHARED16);
            icon = Images.get(ImageNums.FLD_ADDR_OPEN_SHARED16, true);
          else
            //icon = Images.get(ImageNums.FLD_ADDR_CLOSED_SHARED16);
            icon = Images.get(ImageNums.FLD_ADDR_CLOSED_SHARED16, true);
        } else {
          if (selected)
            icon = Images.get(ImageNums.FLD_ADDR_OPEN16);
          else
            icon = Images.get(ImageNums.FLD_ADDR_CLOSED16);
        }
        break;
      case FILE_FOLDER :
        if (folderId.longValue() == CATEGORY_FILE_ID) {
          icon = Images.get(ImageNums.FLD_FILES16);
        } else if (numShares > 1) {
          if (selected)
            //icon = Images.get(ImageNums.FLD_OPEN_SHARED16);
            icon = Images.get(ImageNums.FLD_OPEN_SHARED16, true);
          else
            //icon = Images.get(ImageNums.FLD_CLOSED_SHARED16);
            icon = Images.get(ImageNums.FLD_CLOSED_SHARED16, true);
        } else {
          if (selected)
            icon = Images.get(ImageNums.FLD_OPEN16);
          else
            icon = Images.get(ImageNums.FLD_CLOSED16);
        }
        break;
      case MESSAGE_FOLDER :
        if (folderId.longValue() == CATEGORY_MAIL_ID) {
          icon = Images.get(ImageNums.FLD_MAIL18_12);
        } else if (numShares > 1) {
          if (uRec == null) {
            if (selected) icon = Images.get(ImageNums.FLD_MAIL_OPEN_SHARED16, true);
            else icon = Images.get(ImageNums.FLD_MAIL_CLOSED_SHARED16, true);
          } else if (folderId.equals(uRec.draftFolderId)) {
            if (selected) icon = Images.get(ImageNums.FLD_MAIL_DRAFT_OPEN_SHARED16, true);
            else icon = Images.get(ImageNums.FLD_MAIL_DRAFT_CLOSED_SHARED16, true);
          } else if (folderId.equals(uRec.msgFolderId)) {
            if (selected) icon = Images.get(ImageNums.FLD_MAIL_INBOX_OPEN_SHARED16, true);
            else icon = Images.get(ImageNums.FLD_MAIL_INBOX_CLOSED_SHARED16, true);
          } else if (folderId.equals(uRec.junkFolderId)) {
            if (selected) icon = Images.get(ImageNums.FLD_MAIL_JUNK_OPEN_SHARED16, true);
            else icon = Images.get(ImageNums.FLD_MAIL_JUNK_CLOSED_SHARED16, true);
          } else if (folderId.equals(uRec.sentFolderId)) {
            if (selected) icon = Images.get(ImageNums.FLD_MAIL_SENT_OPEN_SHARED16, true);
            else icon = Images.get(ImageNums.FLD_MAIL_SENT_CLOSED_SHARED16, true);
          } else { // catch all
            if (selected) icon = Images.get(ImageNums.FLD_MAIL_OPEN_SHARED16, true);
            else icon = Images.get(ImageNums.FLD_MAIL_CLOSED_SHARED16, true);
          }
        } else {
          if (uRec == null) {
            if (selected) icon = Images.get(ImageNums.FLD_MAIL_OPEN16);
            else icon = Images.get(ImageNums.FLD_MAIL_CLOSED16);
          } else if (folderId.equals(uRec.draftFolderId)) {
            if (selected) icon = Images.get(ImageNums.FLD_MAIL_DRAFT_OPEN16);
            else icon = Images.get(ImageNums.FLD_MAIL_DRAFT_CLOSED16);
          } else if (folderId.equals(uRec.msgFolderId)) {
            if (selected) icon = Images.get(ImageNums.FLD_MAIL_INBOX_OPEN16);
            else icon = Images.get(ImageNums.FLD_MAIL_INBOX_CLOSED16);
          } else if (folderId.equals(uRec.junkFolderId)) {
            if (selected) icon = Images.get(ImageNums.FLD_MAIL_JUNK_OPEN16);
            else icon = Images.get(ImageNums.FLD_MAIL_JUNK_CLOSED16);
          } else if (folderId.equals(uRec.sentFolderId)) {
            if (selected) icon = Images.get(ImageNums.FLD_MAIL_SENT_OPEN16);
            else icon = Images.get(ImageNums.FLD_MAIL_SENT_CLOSED16);
          } else { // catch all
            if (selected) icon = Images.get(ImageNums.FLD_MAIL_OPEN16);
            else icon = Images.get(ImageNums.FLD_MAIL_CLOSED16);
          }
        }
        break;
      case POSTING_FOLDER :
      case CHATTING_FOLDER :
        if (folderId.longValue() == CATEGORY_CHAT_ID) {
          icon = Images.get(ImageNums.CHAT16);
        } else if (numShares > 1) {
          if (isChatting()) {
            if (selected)
              icon = Images.get(ImageNums.FLD_CHAT_OPEN16);
            else
              icon = Images.get(ImageNums.FLD_CHAT_CLOSED16);
          } else {
            if (selected)
              icon = Images.get(ImageNums.FLD_MAIL_POST_OPEN_SHARED16, true);
            else
              icon = Images.get(ImageNums.FLD_MAIL_POST_CLOSED_SHARED16, true);
          }
        } else { // includes chatting archives too which are no longer shared...
          if (selected)
            icon = Images.get(ImageNums.FLD_MAIL_POST_OPEN16);
          else
            icon = Images.get(ImageNums.FLD_MAIL_POST_CLOSED16);
        }
        break;
      case CONTACT_FOLDER :
        if (selected)
          icon = Images.get(ImageNums.FLD_CNT_OPEN16);
        else
          icon = Images.get(ImageNums.FLD_CNT_CLOSED16);
        break;
      case KEY_FOLDER :
        if (selected)
          icon = Images.get(ImageNums.FLD_KEY_OPEN16);
        else
          icon = Images.get(ImageNums.FLD_KEY_CLOSED16);
        break;
      case GROUP_FOLDER :
        if (folderId.longValue() == CATEGORY_GROUP_ID) {
          icon = Images.get(ImageNums.FLD_GROUPS16);
        } else {
          icon = Images.get(ImageNums.PEOPLE16);
        }
        break;
      case RECYCLE_FOLDER :
        if (numShares > 1) {
          icon = Images.get(ImageNums.FLD_RECYCLE_EMPTY_SHARED16, true);
        } else {
          icon = Images.get(ImageNums.FLD_RECYCLE_EMPTY16);
        }
        break;
      case CATEGORY_MAIL_FOLDER :
        icon = Images.get(ImageNums.FLD_MAIL18_12);
        break;
      case CATEGORY_FILE_FOLDER :
        icon = Images.get(ImageNums.FLD_FILES16);
        break;
      case CATEGORY_CHAT_FOLDER :
        icon = Images.get(ImageNums.FLD_CHAT16);
        break;
      case CATEGORY_GROUP_FOLDER :
        icon = Images.get(ImageNums.FLD_GROUPS16);
        break;
    }
    return icon;
  }

  /** @return true if the folder is a root folder -- has itself as its parent. */
  public boolean isRoot() {
    return folderId.equals(parentFolderId);
  }

  /** @return true if the folder is a super-root -- has its ID in the UserRecord. */
  public boolean isSuperRoot(UserRecord userRecord) {
    return isSuperRoot(folderId, userRecord);
  }
  /** @return true if the folder is a super-root -- has its ID in the UserRecord. */
  public static boolean isSuperRoot(Long folderId, UserRecord userRecord) {
    boolean isSuperRoot =
        userRecord.fileFolderId.equals(folderId) ||
        (userRecord.addrFolderId != null && userRecord.addrFolderId.equals(folderId)) ||
        (userRecord.whiteFolderId != null && userRecord.whiteFolderId.equals(folderId)) ||
        (userRecord.draftFolderId != null && userRecord.draftFolderId.equals(folderId)) ||
        userRecord.msgFolderId.equals(folderId) ||
        userRecord.sentFolderId.equals(folderId) ||
        (userRecord.junkFolderId != null && userRecord.junkFolderId.equals(folderId)) ||
        userRecord.contactFolderId.equals(folderId) ||
        userRecord.keyFolderId.equals(folderId) ||
        (userRecord.recycleFolderId != null && userRecord.recycleFolderId.equals(folderId));
    return isSuperRoot;
  }

  /** @return true if 'this' folder is a child of a given parent in a data tree, not view tree. */
  public boolean isChildToParent(Long otherParentFolderId) {
    boolean isChild = false;
    // if it is a root, it CAN NOT be a child.
    if (!isRoot()) {
      // if this is the wanted child
      if (parentFolderId.equals(otherParentFolderId)) {
        isChild = true;
      }
    }
    return isChild;
  }

  public boolean isSharableType() {
    short fType = folderType.shortValue();
    boolean isSharableType =
        fType == FolderRecord.FILE_FOLDER ||
        fType == FolderRecord.MESSAGE_FOLDER ||
        fType == FolderRecord.POSTING_FOLDER ||
        fType == FolderRecord.CHATTING_FOLDER ||
        fType == FolderRecord.ADDRESS_FOLDER ||
        fType == FolderRecord.WHITELIST_FOLDER ||
        fType == FolderRecord.GROUP_FOLDER ||
        fType == FolderRecord.RECYCLE_FOLDER;
    return isSharableType;
  }

  public boolean isChatting() {
    return (folderType.shortValue() == CHATTING_FOLDER ||
              ( folderType.shortValue() == POSTING_FOLDER &&
                //numOfShares.shortValue() > 1 && // make chat archives also a chatting folder type
                (numToKeep.shortValue() > 0 || keepAsOldAs.intValue() > 0)
              )
           );
  }

  public boolean isChattingLive() {
    return numOfShares.shortValue() > 1 && isChatting();
  }

  public boolean isChattingForOldClient() {
    return (folderType.shortValue() == POSTING_FOLDER &&
            numOfShares.shortValue() > 1 &&
            (numToKeep.shortValue() > 0 || keepAsOldAs.intValue() > 0)
           );
  }

  public boolean isDynamicName() {
    return isChattingLive();
  }

  public String getFolderType() {
    String typeName = null;

    if (isChatting())
      typeName = CHATTING_FOLDER_STR;
    else
      typeName = getFolderType(folderType.shortValue());

    return typeName;
  }

  /**
   * @return string representation of folder type (ignore chatting folders, return posting instead)
   * To include chatting folders in description, use the non-static version of GetFolderType.
   */
  public static String getFolderType(short type) {
    String typeName = null;

    switch (type) {
      case ADDRESS_FOLDER:
        typeName = ADDRESS_FOLDER_STR;
        break;
      case WHITELIST_FOLDER:
        typeName = WHITELIST_FOLDER_STR;
        break;
      case FILE_FOLDER:
        typeName = FILE_FOLDER_STR;
        break;
      case MESSAGE_FOLDER:
        typeName = MESSAGE_FOLDER_STR;
        break;
      case POSTING_FOLDER:
        // posting or old chatting but default to POSTING
        typeName = POSTING_FOLDER_STR;
        break;
      case CHATTING_FOLDER:
        typeName = CHATTING_FOLDER_STR;
        break;
      case CONTACT_FOLDER:
        typeName = CONTACT_FOLDER_STR;
        break;
      case KEY_FOLDER:
        typeName = KEY_FOLDER_STR;
        break;
      case GROUP_FOLDER:
        typeName = GROUP_FOLDER_STR;
        break;
      case RECYCLE_FOLDER:
        typeName = RECYCLE_FOLDER_STR;
        break;
    }

    return typeName;
  }

  public boolean isAddressType() {
    return isAddressType(folderType.shortValue());
  }
  public static boolean isAddressType(short type) {
    return  type == FolderRecord.ADDRESS_FOLDER ||
            type == FolderRecord.WHITELIST_FOLDER;
  }
  public boolean isCategoryType() {
    short type = folderType.shortValue();
    return  type == CATEGORY_MAIL_FOLDER ||
            type == CATEGORY_FILE_FOLDER ||
            type == CATEGORY_CHAT_FOLDER ||
            type == CATEGORY_GROUP_FOLDER;
  }
  public boolean isFileType() {
    return folderType.shortValue() == FolderRecord.FILE_FOLDER;
  }
  public boolean isGroupType() {
    return folderType.shortValue() == FolderRecord.GROUP_FOLDER;
  }
  public boolean isLocalFileType() {
    return folderType.shortValue() == FolderRecord.LOCAL_FILES_FOLDER;
  }
  public boolean isMsgType() {
    return isMsgType(folderType.shortValue());
  }
  public static boolean isMsgType(short type) {
    boolean rc = false;
    switch (type) {
      case FolderRecord.MESSAGE_FOLDER :
      case FolderRecord.POSTING_FOLDER :
      case FolderRecord.CHATTING_FOLDER :
      case FolderRecord.ADDRESS_FOLDER :
      case FolderRecord.WHITELIST_FOLDER :
        rc = true;
        break;
      default :
        rc = false;
    }
    return rc;
  }
  public boolean isMailType() {
    return isMailType(folderType.shortValue());
  }
  public static boolean isMailType(short type) {
    return  type == FolderRecord.MESSAGE_FOLDER ||
            type == FolderRecord.POSTING_FOLDER ||
            type == FolderRecord.CHATTING_FOLDER;
  }
  public boolean isRecycleType() {
    return folderType.shortValue() == FolderRecord.RECYCLE_FOLDER;
  }

  /**
   * Local update marking so that renderer may display updated folders differently
   * and reset when user accesses the folder to check the updates.
   */
  public void setUpdated(int num, boolean suppressSound) {
    int oldNum = numOfUpdates;
    numOfUpdates = num;
    if (num > oldNum && !suppressSound) {
      Sounds.playAsynchronous(Sounds.UPDATE_CLIP);
    }
    invalidateCachedValues();
  }
  public void resetUpdates() {
    numOfUpdates = 0;
    invalidateCachedValues();
  }
  public int getUpdateCount() {
    return numOfUpdates;
  }


  public String getCachedOwnerNote() {
    return cachedOwnerNote;
  }
  public String getCachedChatNote() {
    return cachedChatNote;
  }
  public String getCachedDisplayText() {
    return cachedDisplayText;
  }
  public String getCachedToolTip() {
    return cachedToolTip;
  }
  public void setCachedOwnerNote(String note) {
    cachedOwnerNote = note;
  }
  public void setCachedChatNote(String note) {
    cachedChatNote = note;
  }
  public void setCachedDisplayText(String text) {
    cachedDisplayText = text;
  }
  public void setCachedToolTip(String tip) {
    cachedToolTip = tip;
  }
  public void invalidateCachedValues() {
    cachedOwnerNote = null;
    cachedChatNote = null;
    cachedDisplayText = null;
    cachedToolTip = null;
  }


  public int compareFolderType(FolderRecord compareToFolderRecord) {
    int rc = 0;
    short compareToType = compareToFolderRecord.folderType.shortValue();
    short type = folderType.shortValue();
    while (true) { // loop to facilitate break-out
      if (type == compareToType) {
        // non-posting/chatting
        if (folderType.shortValue() != POSTING_FOLDER) {
          rc = 0;
          break;
        } else {
          // posting/chatting
          if (isChatting() == compareToFolderRecord.isChatting()) {
            rc = 0;
            break;
          } else {
            rc = compareToFolderRecord.isChatting() ? -1 : 1; // chatting goes before posting
            break;
          }
        }
      }
      short[] sortOrder = new short[] {
        CATEGORY_MAIL_FOLDER,
        CATEGORY_FILE_FOLDER,
        CATEGORY_CHAT_FOLDER,
        CATEGORY_GROUP_FOLDER,
        MESSAGE_FOLDER,
        LOCAL_FILES_FOLDER,
        FILE_FOLDER,
        POSTING_FOLDER,
        CHATTING_FOLDER,
        ADDRESS_FOLDER,
        WHITELIST_FOLDER,
        GROUP_FOLDER,
        CONTACT_FOLDER,
        KEY_FOLDER,
        RECYCLE_FOLDER };
      int myPos = ArrayUtils.find(sortOrder, folderType.shortValue());
      if (myPos == -1) myPos = Integer.MAX_VALUE;
      int yourPos = ArrayUtils.find(sortOrder, compareToType);
      if (yourPos == -1) yourPos = Integer.MAX_VALUE;
      rc = yourPos > myPos ? 1 : -1;
      // always break, never loop again
      break;
    }
    return rc;
  }


  public static Long[] getParentFolderIDs(FolderRecord[] folderRecords) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderRecord.class, "FolderRecord[] folderRecords");
    if (trace != null) trace.args(folderRecords);

    Vector parentIDsV = new Vector();
    if (folderRecords != null) {
      for (int i=0; i<folderRecords.length; i++) {
        if (parentIDsV.contains(folderRecords[i].parentFolderId) == false)
          parentIDsV.addElement(folderRecords[i].parentFolderId);
      }
    }
    Long[] parentIDs = (Long[]) ArrayUtils.toArray(parentIDsV, Long.class);

    if (trace != null) trace.exit(FolderRecord.class, parentIDs);
    return parentIDs;
  }


  public void merge(Record updated) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderRecord.class, "merge(Record)");
    if (trace != null) trace.args(updated);
    if (trace != null) trace.data(10, "this=", this);

    if (updated instanceof FolderRecord) {
      FolderRecord record = (FolderRecord) updated;
      if (record.folderId       != null) folderId       = record.folderId;
      if (record.parentFolderId != null) parentFolderId = record.parentFolderId;
      if (record.ownerUserId    != null) ownerUserId    = record.ownerUserId;
      if (record.folderType     != null) folderType     = record.folderType;
      if (record.numToKeep      != null) numToKeep      = record.numToKeep;
      if (record.keepAsOldAs    != null) keepAsOldAs    = record.keepAsOldAs;
      if (record.numOfShares    != null) numOfShares    = record.numOfShares;
      if (record.dateCreated    != null) dateCreated    = record.dateCreated;
      if (record.dateUpdated    != null) dateUpdated    = record.dateUpdated;
    }
    else
      super.mergeError(updated);

    // Clear cached rendering notes
    invalidateCachedValues();

    if (trace != null) trace.exit(FolderRecord.class);
  }

  public String toString() {
    return "[FolderRecord"
      + ": folderId="         + folderId
      + ", parentFolderId="   + parentFolderId
      + ", ownerUserId="      + ownerUserId
      + ", folderType="       + folderType
      + ", numToKeep="        + numToKeep
      + ", keepAsOldAs="      + keepAsOldAs
      + ", numOfShares="      + numOfShares
      + ", dateCreated="      + dateCreated
      + ", dateUpdated="      + dateUpdated
      + ", private-local data >> "
      + ", numOfUpdates="     + numOfUpdates
      + "]";
  }

  public void setId(Long id) {
    folderId = id;
  }

}