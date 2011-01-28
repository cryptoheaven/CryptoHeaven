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

package com.CH_co.service.records;

import com.CH_co.trace.Trace;

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
 * <b>$Revision: 1.15 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class FolderPair extends FileRecord implements MemberContactRecordI {

  private FolderRecord folderRecord = null;
  private FolderShareRecord folderShare = null;


  /** Creates new FolderPair */
  public FolderPair(FolderShareRecord folderShare, FolderRecord folderRecord) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderPair.class, "FolderPair()");

    this.folderShare = folderShare;
    this.folderRecord = folderRecord;

    if (trace != null) trace.exit(FolderPair.class);
  }


  public FolderShareRecord getFolderShareRecord() {
    return folderShare;
  }
  public FolderRecord getFolderRecord() {
    return folderRecord;
  }

  public Long getId() {
    return folderRecord.getId();
  }

  public int getIcon() {
    return folderRecord.getIcon();
  }

  public Long getParentId() {
    return folderRecord.parentFolderId;
  }
  public Long getFileViewParentId() {
    return folderShare != null && folderShare.getViewParentId() != null ? folderShare.getViewParentId() : folderRecord.parentFolderId;
  }
  public boolean isViewRoot() {
    return folderRecord.folderId.equals(getFileViewParentId());
  }

  public String getMyName() {
    return folderShare.getFolderName();
  }

  public void merge(Record updated) {
    if (updated instanceof FolderPair) {
      FolderPair folderPair = (FolderPair) updated;
      if (folderPair.getFolderRecord() != null) folderRecord.merge(folderPair.getFolderRecord());
      if (folderPair.getFolderShareRecord() != null) folderShare.merge(folderPair.getFolderShareRecord());
    }
    else
      super.mergeError(updated);
  }

  public static Long[] getShareIDs(FolderPair[] folderPairs) {
    Long[] shareIDs = null;
    if (folderPairs != null) {
      shareIDs = new Long[folderPairs.length];
      for (int i=0; i<folderPairs.length; i++)
        shareIDs[i] = folderPairs[i].getFolderShareRecord().shareId;
    }
    return shareIDs;
  }

  public static FolderRecord[] getFolderRecords(FolderPair[] folderPairs) {
    FolderRecord[] folderRecords = null;
    if (folderPairs != null) {
      folderRecords = new FolderRecord[folderPairs.length];
      for (int i=0; i<folderPairs.length; i++)
        folderRecords[i] = folderPairs[i].getFolderRecord();
    }
    return folderRecords;
  }

  public static FolderShareRecord[] getFolderShareRecords(FolderPair[] folderPairs) {
    FolderShareRecord[] shareRecords = null;
    if (folderPairs != null) {
      shareRecords = new FolderShareRecord[folderPairs.length];
      for (int i=0; i<folderPairs.length; i++)
        shareRecords[i] = folderPairs[i].getFolderShareRecord();
    }
    return shareRecords;
  }

  public String toString() {
    return "[FolderPair"
      + ": folderRecord=" + folderRecord
      + ", folderShare=" + folderShare
      + "]";
  }

  public void setId(Long id) {
    if (folderRecord != null)
      folderRecord.folderId = id;
  }

  public short getMemberType() {
    if (folderRecord != null && 
        folderRecord.folderType != null && 
        folderRecord.folderType.shortValue() == FolderRecord.GROUP_FOLDER)
      return Record.RECORD_TYPE_GROUP;
    else
      return Record.RECORD_TYPE_FOLDER;
  }

  public Long getMemberId() {
    return folderRecord.folderId;
  }

}