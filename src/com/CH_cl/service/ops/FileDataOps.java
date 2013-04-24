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

package com.CH_cl.service.ops;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_co.cryptx.BA;
import com.CH_co.monitor.Stats;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.Obj_IDs_Co;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;

import java.io.File;

/**
 * <b>Copyright</b> &copy; 2001-2013
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
 * <b>$Revision: 1.0 $</b>
 * @author  Marcin
 * @version
 */
public class FileDataOps {

  public static FileDataRecord getOrFetchFileLOB(ServerInterfaceLayer SIL, FileLinkRecord link, FileDataRecord data) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileDataOps.class, "getOrFetchFileLOB(ServerInterfaceLayer SIL, FileLinkRecord link, FileDataRecord data)");
    if (trace != null) trace.args(SIL, link, data);
    FileDataRecord rec = getOrFetchFile(SIL, CommandCodes.FILE_Q_GET_FILES_DATA, link.fileLinkId, data.fileId);
    if (trace != null) trace.exit(FileDataOps.class, rec);
    return rec;
  }
  public static FileDataRecord getOrFetchFileDataAttr(ServerInterfaceLayer SIL, Long fileLinkId, Long fileId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileDataOps.class, "getOrFetchFileDataAttr(ServerInterfaceLayer SIL, Long fileLinkId, Long fileId)");
    if (trace != null) trace.args(SIL, fileLinkId, fileId);
    FileDataRecord rec = getOrFetchFile(SIL, CommandCodes.FILE_Q_GET_FILES_DATA_ATTRIBUTES, fileLinkId, fileId);
    if (trace != null) trace.exit(FileDataOps.class, rec);
    return rec;
  }
  private static FileDataRecord getOrFetchFile(ServerInterfaceLayer SIL, int actionCode, Long fileLinkId, Long fileId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileDataOps.class, "getOrFetchFile(ServerInterfaceLayer SIL, int actionCode, Long fileLinkId, Long fileId)");
    if (trace != null) trace.args(SIL);
    if (trace != null) trace.args(actionCode);
    if (trace != null) trace.args(fileLinkId, fileId);

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    FileDataRecord fData = cache.getFileDataRecord(fileId);

    boolean doFetching = false;
    if (actionCode == CommandCodes.FILE_Q_GET_FILES_DATA_ATTRIBUTES) {
      // If data attributes are incomplete, then fetch them
      if (fData == null)
        doFetching = true;
      else {
        if (fData.getEncSize() == null || fData.getEncSize().longValue() == -1 ||
                BA.isEmptyOrZero(fData.getEncOrigDataDigest()) ||
                BA.isEmptyOrZero(fData.getEncSignedOrigDigest()) ||
                BA.isEmptyOrZero(fData.getEncEncDataDigest()))
          doFetching = true;
      }
    } else if (actionCode == CommandCodes.FILE_Q_GET_FILES_DATA) {
      File encFile = fData.getEncDataFile();
      File plnFile = fData.getPlainDataFile();
      doFetching = (encFile == null || !encFile.exists()) && (plnFile == null || !plnFile.exists());
    }

    if (doFetching) {

      Long[] fileLinkIDs = new Long[] { fileLinkId };
      Long[] fromShareIDs = null;
      Long[] fromMsgLinkIDs = null;

      FileLinkRecord fLink = cache.getFileLinkRecord(fileLinkId);
      if (fLink != null) {
        if (fLink.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER) {
          FolderShareRecord fShare = cache.getFolderShareRecordMy(fLink.ownerObjId, true);
          if (fShare != null)
            fromShareIDs = new Long[] { fShare.shareId };
        } else if (fLink.ownerObjType.shortValue() == Record.RECORD_TYPE_MESSAGE) {
          MsgLinkRecord[] mLinks = cache.getMsgLinkRecordsForMsg(fLink.ownerObjId);
          if (mLinks != null && mLinks.length > 0) {
            // gather first link that belongs to a folder and if one doesn't exist then use any link without share ID
            for (int i=0; i<mLinks.length; i++) {
              if (mLinks[i].ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER) {
                if (fromMsgLinkIDs == null) {
                  fromMsgLinkIDs = new Long[] { mLinks[i].msgLinkId };
                  FolderShareRecord[] fShares = cache.getFolderSharesMyForFolders(new Long[] { mLinks[i].ownerObjId }, true);
                  fromShareIDs = RecordUtils.getIDs(fShares);
                  break;
                }
              }
            }
            // if still no fromMsgLinkIDs found for FOLDER type owner, then use anyone without specifying shares and server will find it
            if (fromMsgLinkIDs == null) {
              fromMsgLinkIDs = new Long[] { mLinks[0].msgLinkId };
            }
          }
        }
      }
      Obj_IDs_Co request = new Obj_IDs_Co();
      request.IDs = new Long[3][];
      request.IDs[0] = fileLinkIDs;
      request.IDs[1] = fromShareIDs;
      request.IDs[2] = fromMsgLinkIDs;

      MessageAction msgAction = new MessageAction(actionCode, request);

      if (actionCode == CommandCodes.FILE_Q_GET_FILES_DATA && fData.getEncSize().longValue() > getMaxFileDownSizeForMainConnection()) {
        Thread th = new DownloadUtilities.DownloadFileRunner(msgAction, null, new FileLinkRecord[] { fLink }, new String[] { fLink.getFileName() }, SIL, false, false);
        th.start();
        // wait for completion...
        try { th.join(); } catch (InterruptedException e) { }
      } else {
        SIL.submitAndWait(msgAction);
      }

      // re-check the cache after request completed
      fData = cache.getFileDataRecord(fileId);
    }

    if (trace != null) trace.exit(FileDataOps.class, fData);
    return fData;
  }

  private static long getMaxFileDownSizeForMainConnection() {
    long maxTransferProportional = Stats.getMaxTransferRateIn()*1; // 1 seconds worth of download
    long maxSize = Math.max(maxTransferProportional, ServerInterfaceLayer.DEFAULT_MAX_FILE_SIZE_FOR_MAIN_CONNECTION);
    return maxSize;
  }
}