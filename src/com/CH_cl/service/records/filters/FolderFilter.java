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

package com.CH_cl.service.records.filters;

import com.CH_co.trace.Trace;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;

import java.util.Arrays;
import java.util.HashSet;

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
 * <b>$Revision: 1.18 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class FolderFilter extends AbstractRecordFilter implements RecordFilter {

  public static final FolderFilter MAIN_VIEW = new FolderFilter(
      new short[] {
            FolderRecord.INTERNAL_ANY_SHARED_FOLDER,
            FolderRecord.CATEGORY_MAIL_FOLDER,
            FolderRecord.CATEGORY_FILE_FOLDER,
            FolderRecord.CATEGORY_CHAT_FOLDER,
            FolderRecord.CATEGORY_GROUP_FOLDER,
            FolderRecord.LOCAL_FILES_FOLDER,
            FolderRecord.FILE_FOLDER,
            //FolderRecord.KEY_FOLDER,
            FolderRecord.MESSAGE_FOLDER,
            FolderRecord.POSTING_FOLDER,
            FolderRecord.CHATTING_FOLDER,
            FolderRecord.ADDRESS_FOLDER,
            FolderRecord.GROUP_FOLDER,
            FolderRecord.RECYCLE_FOLDER,
            }
          );
  public static final FolderFilter ATTACHABLE_CONTENT = new FolderFilter(
      new short[] {
            FolderRecord.CATEGORY_MAIL_FOLDER,
            FolderRecord.CATEGORY_FILE_FOLDER,
            FolderRecord.CATEGORY_CHAT_FOLDER,
            FolderRecord.LOCAL_FILES_FOLDER,
            FolderRecord.FILE_FOLDER,
            FolderRecord.MESSAGE_FOLDER,
            FolderRecord.POSTING_FOLDER,
            FolderRecord.CHATTING_FOLDER,
            FolderRecord.ADDRESS_FOLDER,
            }
          );
  public static final FolderFilter MOVE_FOLDER = new FolderFilter(
      new short[] {
            FolderRecord.CATEGORY_MAIL_FOLDER,
            FolderRecord.CATEGORY_FILE_FOLDER,
            FolderRecord.CATEGORY_CHAT_FOLDER,
            FolderRecord.CATEGORY_GROUP_FOLDER,
            FolderRecord.FILE_FOLDER,
            //FolderRecord.KEY_FOLDER,
            FolderRecord.MESSAGE_FOLDER,
            FolderRecord.POSTING_FOLDER,
            FolderRecord.CHATTING_FOLDER,
            FolderRecord.ADDRESS_FOLDER,
            FolderRecord.GROUP_FOLDER,
            FolderRecord.RECYCLE_FOLDER,
            }
          );
  public static final FolderFilter MOVE_MSG = new FolderFilter(
      new short[] {
            FolderRecord.CATEGORY_MAIL_FOLDER,
            FolderRecord.CATEGORY_CHAT_FOLDER,
            FolderRecord.MESSAGE_FOLDER,
            FolderRecord.POSTING_FOLDER,
            FolderRecord.CHATTING_FOLDER,
            FolderRecord.ADDRESS_FOLDER,
            FolderRecord.WHITELIST_FOLDER,
            FolderRecord.RECYCLE_FOLDER,
            }
          );
  public static final FolderFilter NON_FILE_FOLDERS = new FolderFilter(
      new short[] {
            FolderRecord.CATEGORY_MAIL_FOLDER,
            FolderRecord.CATEGORY_CHAT_FOLDER,
            FolderRecord.CATEGORY_GROUP_FOLDER,
            FolderRecord.KEY_FOLDER,
            FolderRecord.MESSAGE_FOLDER,
            FolderRecord.POSTING_FOLDER,
            FolderRecord.CHATTING_FOLDER,
            FolderRecord.ADDRESS_FOLDER,
            FolderRecord.WHITELIST_FOLDER,
            FolderRecord.GROUP_FOLDER,
            }
          );
  public static final FolderFilter NON_MSG_FOLDERS = new FolderFilter(
      new short[] {
            FolderRecord.CATEGORY_FILE_FOLDER,
            FolderRecord.CATEGORY_GROUP_FOLDER,
            FolderRecord.LOCAL_FILES_FOLDER,
            FolderRecord.FILE_FOLDER,
            FolderRecord.KEY_FOLDER,
            FolderRecord.CONTACT_FOLDER,
            FolderRecord.GROUP_FOLDER,
            }
          );
  public static final FolderFilter NON_LOCAL_FOLDERS = new FolderFilter(
      new short[] {
            FolderRecord.CATEGORY_MAIL_FOLDER,
            FolderRecord.CATEGORY_FILE_FOLDER,
            FolderRecord.CATEGORY_CHAT_FOLDER,
            FolderRecord.CATEGORY_GROUP_FOLDER,
            FolderRecord.FILE_FOLDER,
            FolderRecord.KEY_FOLDER,
            FolderRecord.MESSAGE_FOLDER,
            FolderRecord.POSTING_FOLDER,
            FolderRecord.CHATTING_FOLDER,
            FolderRecord.ADDRESS_FOLDER,
            FolderRecord.WHITELIST_FOLDER,
            FolderRecord.CONTACT_FOLDER,
            FolderRecord.GROUP_FOLDER,
            FolderRecord.RECYCLE_FOLDER,
            }
          );


  private short[] keepFolderTypes;
  private HashSet keepFolderIDsHS;
  private Long keepOwnerId;
  private Long keepParentId;
  private HashSet keepViewParentIDsHS;     // This is actual view parent id from FolderPair, not necessairly FolderShareRecord.viewParentId.
  private Boolean keepViewRootFolders;  // Roots in the view are not necessairly the same as root folders in data tree.
  private Boolean mustBeChatting;
  private Boolean mustBePosting;


  /** Creates new FolderFilter */
  public FolderFilter(short keepFolderType) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderFilter.class, "FolderFilter(short keepFolderType)");
    if (trace != null) trace.args(keepFolderType);
    this.keepFolderTypes = new short[] { keepFolderType };
    if (trace != null) trace.exit(FolderFilter.class);
  }

  public FolderFilter(boolean mustBePosting, boolean mustBeChatting) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderFilter.class, "FolderFilter(boolean mustBePosting, boolean mustBeChatting)");
    if (trace != null) trace.args(mustBePosting);
    if (trace != null) trace.args(mustBeChatting);
    this.mustBePosting = Boolean.valueOf(mustBePosting);
    this.mustBeChatting = Boolean.valueOf(mustBeChatting);
    if (trace != null) trace.exit(FolderFilter.class);
  }

  /** Creates new FolderFilter */
  public FolderFilter(short keepFolderType, Long keepOwnerId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderFilter.class, "FolderFilter(short keepFolderType, Long keepOwnerId)");
    if (trace != null) trace.args(keepFolderType);
    if (trace != null) trace.args(keepOwnerId);
    this.keepFolderTypes = new short[] { keepFolderType };
    this.keepOwnerId = keepOwnerId;
    if (trace != null) trace.exit(FolderFilter.class);
  }

  /** Creates new FolderFilter */
  public FolderFilter(short keepFolderType, Long[] keepFolderIDs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderFilter.class, "FolderFilter(short keepFolderType, Long[] keepFolderIDs)");
    if (trace != null) trace.args(keepFolderType);
    if (trace != null) trace.args(keepFolderIDs);
    this.keepFolderTypes = new short[] { keepFolderType };
    if (keepFolderIDs != null) {
      this.keepFolderIDsHS = new HashSet(keepFolderIDs.length);
      this.keepFolderIDsHS.addAll(Arrays.asList(keepFolderIDs));
    }
    if (trace != null) trace.exit(FolderFilter.class);
  }

  /** Creates new FolderFilter */
  public FolderFilter(Long keepOwnerId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderFilter.class, "FolderFilter(Long keepOwnerId)");
    if (trace != null) trace.args(keepOwnerId);
    this.keepOwnerId = keepOwnerId;
    if (trace != null) trace.exit(FolderFilter.class);
  }

  /** Creates new FolderFilter */
  public FolderFilter(Long keepOwnerId, Long keepParentId, Long keepViewParentId, Boolean keepViewRootFolders) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderFilter.class, "FolderFilter(Long keepOwnerId, Long keepParentId, Long keepViewParentId, boolean keepViewRootFolders)");
    if (trace != null) trace.args(keepOwnerId, keepParentId, keepViewParentId);
    this.keepOwnerId = keepOwnerId;
    this.keepParentId = keepParentId;
    if (keepViewParentId != null) {
      this.keepViewParentIDsHS = new HashSet(1);
      this.keepViewParentIDsHS.add(keepViewParentId);
    }
    this.keepViewRootFolders = keepViewRootFolders;
    if (trace != null) trace.exit(FolderFilter.class);
  }

  /** Creates new FolderFilter */
  public FolderFilter(Long[] keepViewParentIDs) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderFilter.class, "FolderFilter(Long[] keepViewParentIDs)");
    if (trace != null) trace.args(keepViewParentIDs);
    if (keepViewParentIDs != null) {
      this.keepViewParentIDsHS = new HashSet();
      this.keepViewParentIDsHS.addAll(Arrays.asList(keepViewParentIDs));
    }
    if (trace != null) trace.exit(FolderFilter.class);
  }

  /** Creates new FolderFilter */
  public FolderFilter(short[] keepFolderTypes) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderFilter.class, "FolderFilter(short[] keepFolderTypes)");
    if (trace != null) trace.args(keepFolderTypes);
    this.keepFolderTypes = keepFolderTypes;
    if (trace != null) trace.exit(FolderFilter.class);
  }


  public boolean keep(Record record) {
    boolean typeOk = keepFolderTypes == null;
    boolean folderIdOk = keepFolderIDsHS == null;
    boolean ownerOk = keepOwnerId == null;
    boolean parentOk = keepParentId == null;
    boolean viewParentOk = keepViewParentIDsHS == null;
    boolean rootViewOk = keepViewRootFolders == null;
    boolean chattingOk = mustBeChatting == null;
    boolean postingOk = mustBePosting == null;

    FolderRecord fRec = null;
    FolderPair fPair = null;
    if (record instanceof FolderPair)  {
      fPair = (FolderPair) record;
      fRec = fPair.getFolderRecord();
    } else if (record instanceof FolderRecord) {
      fRec = (FolderRecord) record;
    }

    if (fRec != null) {
      if (!typeOk) typeOk = keep(fRec.folderType.shortValue(), fRec.numOfShares.shortValue());
      if (!folderIdOk) folderIdOk = keepFolderIDsHS.contains(fRec.folderId);
      if (!ownerOk) ownerOk = keepOwnerId.equals(fRec.ownerUserId);
      if (!parentOk) parentOk = keepParentId.equals(fRec.parentFolderId);
      if (!chattingOk) chattingOk = mustBeChatting.booleanValue() == fRec.isChatting();
      if (!postingOk) postingOk = mustBePosting.booleanValue() == (fRec.folderType.shortValue() == FolderRecord.POSTING_FOLDER && !fRec.isChatting());
    }
    if (fPair != null) {
      if (!viewParentOk) viewParentOk = keepViewParentIDsHS.contains(fPair.getFileViewParentId());
      if (!viewParentOk) {
        Long guiViewParentId = fPair.getFolderShareRecord().guiViewParentId;
        if (guiViewParentId != null)
          viewParentOk = keepViewParentIDsHS.contains(guiViewParentId);
      }
//      // analize view parents as Category folders
//      if (!viewParentOk) {
//        if (fPair.isViewRoot()) {
//          long parentId = 0;
//          if (fRec.isFileType() || fRec.isLocalFileType()) {
//            parentId = FolderRecord.CATEGORY_FILE_ID;
//          } else if (fRec.isChatting()) {
//            parentId = FolderRecord.CATEGORY_CHAT_ID;
//          } else if (fRec.isMsgType()) {
//            parentId = FolderRecord.CATEGORY_MAIL_ID;
//          }
//          if (parentId != 0) {
//            viewParentOk = keepViewParentIDsHT.get(new Long(parentId)) != null;
//          }
//        }
//      }
      if (!rootViewOk) rootViewOk = keepViewRootFolders.booleanValue() == fPair.isViewRoot();
    }

    return typeOk && folderIdOk && ownerOk && parentOk && viewParentOk && rootViewOk && chattingOk && postingOk;
  }

  public boolean keep(short folderType, short numOfShares) {
    boolean typeOk = false;
    if (keepFolderTypes != null) {
      for (int i=0; i<keepFolderTypes.length; i++) {
        if (keepFolderTypes[i] == folderType ||
            (numOfShares > 1 && keepFolderTypes[i] == FolderRecord.INTERNAL_ANY_SHARED_FOLDER)) {
          typeOk = true;
          break;
        }
      }
    }
    return typeOk == (keepFolderTypes != null && keepFolderTypes.length > 0);
  }

  public FolderFilter cloneAndKeepOwner(Long userId) {
    FolderFilter newFilter = new FolderFilter(keepFolderTypes);
    newFilter.keepOwnerId = userId;
    return newFilter;
  }
}