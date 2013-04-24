/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_gui.file;

import java.util.*;

import com.CH_gui.frame.MainFrame;

import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.cache.FetchedDataCache;

import com.CH_co.service.records.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.fld.*;
import com.CH_co.service.msg.dataSets.file.*;
import com.CH_co.service.msg.dataSets.obj.*;

import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import com.CH_gui.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:
 * All common methods for folders or/and folder records should be placed here
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.17 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class FileUtilities extends Object {


  /** Creates new FileUtilities */
  public FileUtilities() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileUtilities.class, "FileUtilities()");
    if (trace != null) trace.exit(FileUtilities.class);
  }

  /* Create and send a request to rename a FolderShareRecord and all other people shares with <code> newName, newDesc </code> */
  public static void renameFolderAndShares(String newName, String newDesc, String newShareName, String newShareDesc, final FolderShareRecord record) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileUtilities.class, "renameFolderAndShares(String newName, String newDesc, String newShareName, String newShareDesc, FolderShareRecord record)");
    if (trace != null) trace.args(newName, newDesc, newShareName, newShareDesc, record);

    final ServerInterfaceLayer serverInterfaceLayer = MainFrame.getServerInterfaceLayer();

    final FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    final UserRecord myUser = cache.getUserRecord();
    final Long myUserId = myUser.userId;
    final FolderShareRecord[] shares = cache.getFolderShareRecordsForFolder(record.folderId);

    for (int i=0; i<shares.length; i++) {
      shares[i] = (FolderShareRecord) shares[i].clone();
      if (shares[i].shareId.equals(record.shareId)) {
        shares[i].setFolderName(newName);
        shares[i].setFolderDesc(newDesc);
      } else {
        shares[i].setFolderName(newShareName);
        shares[i].setFolderDesc(newShareDesc);
      }
    }

    // user ids of people who are involved but we don't have public keys cached
    Vector userIDsOfNeededKeysV = new Vector();
    for (int i=0; i<shares.length; i++) {
      if (shares[i].isOwnedByUser()) {
        Long uId = shares[i].ownerUserId;
        KeyRecord kR = cache.getKeyRecordForUser(uId);
        if (kR == null || kR.plainPublicKey == null)
          if (!userIDsOfNeededKeysV.contains(uId))
            userIDsOfNeededKeysV.addElement(uId);
      }
    }

    Runnable afterKeyFetchRunner = new Runnable() {
      public void run() {
        Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "renameFolderAndShares.afterKeyFetchRunner.run()");

        try {
          Set groupIDsSet = null;
          for (int i=0; i<shares.length; i++) {
            FolderShareRecord share = shares[i];
            // Keep the same folder key for all shares, users must have same key to decrypt content successfuly.
            // Don't generate new key for existing folder because the existing data (files, messages) would be lost forever (DELETE equivalent).
            if (share.isOwnedByUser()) {
              Long ownerUserId = share.ownerUserId;
              share.setSymmetricKey(record.getSymmetricKey());
              if (ownerUserId.equals(myUserId))
                share.seal(myUser.getSymKeyFldShares());
              else
                share.seal(cache.getKeyRecordForUser(ownerUserId));
            } else {
              if (groupIDsSet == null) groupIDsSet = cache.getFolderGroupIDsMySet();
              FolderShareRecord groupShare = cache.getFolderShareRecordMy(share.ownerUserId, groupIDsSet);
              // we should have the key of the group, but check just in case
              if (groupShare != null) {
                share.seal(groupShare.getSymmetricKey());
              } else {
                MessageDialog.showErrorDialog(null, "Could not locate group's encryption key to rename the folder.  Operation terminated.", "Fetch Error");
                throw new RuntimeException("Could not locate group's encryption key to rename the folder.  Operation terminated.");
              }
            }
          }
        } catch (Throwable t) {
          if (trace != null) trace.exception(getClass(), 100, t);
          MessageDialog.showErrorDialog(GeneralDialog.getDefaultParent(), "Could not fetch user's Public Key.  Operation terminated.", "Fetch Error");
          throw new RuntimeException("Could not fetch user's Public Key.  Operation terminated.");
        }

        Fld_AltStrs_Rq request = new Fld_AltStrs_Rq(shares);
        MessageAction msgAction = new MessageAction(CommandCodes.FLD_Q_ALTER_STRS, request);
        serverInterfaceLayer.submitAndReturn(msgAction);

        if (trace != null) trace.exit(FileUtilities.class);
      }
    };

    // We are lacking public KEYs, fetch them now.
    if (userIDsOfNeededKeysV.size() > 0) {
      Long[] userIDs = new Long[userIDsOfNeededKeysV.size()];
      userIDsOfNeededKeysV.toArray(userIDs);
      MainFrame.getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_USERS, new Obj_IDList_Co(userIDs)), 120000, afterKeyFetchRunner, afterKeyFetchRunner);
    } else {
      afterKeyFetchRunner.run();
    }

    if (trace != null) trace.exit(FileUtilities.class);
  }

  /* Create and send a request to rename a FolderShareRecord with <code> newName </code> */
  // DEPRECIATED
  private static void renameFolder(String newName, FolderShareRecord record) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileUtilities.class, "renameFolder(String newName, FolderShareRecord record)");
    if (trace != null) trace.args(newName, record);

    record.setFolderName(newName);
    record.seal(FetchedDataCache.getSingleInstance().getUserRecord().getSymKeyFldShares());

    ServerInterfaceLayer serverInterfaceLayer = MainFrame.getServerInterfaceLayer();

    Fld_AltStrs_Rq request = new Fld_AltStrs_Rq(record);
    MessageAction msgAction = new MessageAction(CommandCodes.FLD_Q_ALTER_STRS, request);
    serverInterfaceLayer.submitAndReturn(msgAction);

    if (trace != null) trace.exit(FileUtilities.class);
  }

  public static void renameFile(String newName, FileLinkRecord record) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileUtilities.class, "renameFile(String newName, FileLinkRecord record)");
    if (trace != null) trace.args(newName, record);

    record.setFileName(newName);
    record.setFileType(FileTypes.getFileType(newName));
    record.seal();

    ServerInterfaceLayer serverInterfaceLayer = MainFrame.getServerInterfaceLayer();
    FetchedDataCache cache = serverInterfaceLayer.getFetchedDataCache();

    Long shareId = cache.getFolderShareRecordMy(record.getParentId(), true).shareId;

    File_Rename_Rq request = new File_Rename_Rq(shareId, record);
    MessageAction msgAction = new MessageAction(CommandCodes.FILE_Q_RENAME, request);
    serverInterfaceLayer.submitAndReturn(msgAction);

    if (trace != null) trace.exit(FileUtilities.class);
  }

}