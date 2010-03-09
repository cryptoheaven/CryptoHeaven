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

package com.CH_cl.service.actions.file;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.actions.msg.MsgAGet;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.cache.event.RecordEvent;

import com.CH_co.service.msg.*;
import com.CH_co.service.records.*;
import com.CH_co.service.msg.dataSets.file.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.msg.dataSets.stat.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

import java.sql.Timestamp;
import java.util.*;

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
 * <b>$Revision: 1.7 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class FileAGetFiles extends ClientMessageAction {

  /** Creates new FileAGetFiles */
  public FileAGetFiles() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileAGetFiles.class, "FileAGetFiles()");
    if (trace != null) trace.exit(FileAGetFiles.class);
  }

  /**
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileAGetFiles.class, "runAction(Connection)");

    File_GetLinks_Rp reply = (File_GetLinks_Rp) getMsgDataSet();

    Short fetchingOwnerObjType = reply.ownerObjType;
    Long fetchingOwnerObjId = reply.ownerObjId;
    Short fetchNumMax = reply.fetchNumMax;
    Timestamp timestamp = reply.timestamp;
    FileLinkRecord[] fileLinks = reply.fileLinks;
    StatRecord[] statRecords = reply.stats_rp != null ? reply.stats_rp.stats : null;

    FetchedDataCache cache = getFetchedDataCache();
    Set groupIDsSet = null;

    // Since getting file links may be a result of another user creating it,
    // make sure we have the share (and its key) to unSeal the file link.
    // Share might have been created and not successfuly delivered.

    // Gather all necessary folders for incoming file links.
    Vector folderIDsV = null;
    for (int i=0; i<fileLinks.length; i++) {
      FileLinkRecord fLink = fileLinks[i];
      switch (fLink.ownerObjType.shortValue()) {
        case Record.RECORD_TYPE_FOLDER:
          if (groupIDsSet == null) groupIDsSet = cache.getFolderGroupIDsMySet();
          if (cache.getFolderShareRecordMy(fLink.ownerObjId, groupIDsSet) == null) {
            if (folderIDsV == null) folderIDsV = new Vector();
            folderIDsV.addElement(fLink.ownerObjId);
          }
          break;
        case Record.RECORD_TYPE_MESSAGE:
          // if an owner is a message, we already have it, no need to fetch the message
          break;

        default:
          throw new IllegalArgumentException("Not supported: ownerObjType=" + fLink.ownerObjType);
      }
    }
    if (folderIDsV != null && folderIDsV.size() > 0) {
      Long[] folderIDs = (Long[]) ArrayUtils.toArray(folderIDsV, Long.class);
      folderIDs = (Long[]) ArrayUtils.removeDuplicates(folderIDs);
      getServerInterfaceLayer().submitAndWait(new MessageAction(CommandCodes.FLD_Q_GET_FOLDERS_SOME, new Obj_IDList_Co(folderIDs)), 60000);
    }


    // update the fetched stats if any
    if (statRecords != null)
      cache.addStatRecords(statRecords);

    // set the file links
    cache.addFileLinkRecords(fileLinks);

    // Gather all File Links that we don't have stat records for, and fetch the stats
    {
      Vector shareIDsV = null;
      Vector objLinkIDsV = null;
      for (int i=0; i<fileLinks.length; i++) {
        FileLinkRecord link = fileLinks[i];
        if (cache.getStatRecord(link.fileLinkId, FetchedDataCache.STAT_TYPE_FILE) == null) {
          if (link.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER) {
            if (groupIDsSet == null) groupIDsSet = cache.getFolderGroupIDsMySet();
            FolderShareRecord share = cache.getFolderShareRecordMy(link.ownerObjId, groupIDsSet);
            if (shareIDsV == null) shareIDsV = new Vector();
            if (objLinkIDsV == null) objLinkIDsV = new Vector();
            if (!shareIDsV.contains(share.shareId))
              shareIDsV.addElement(share.shareId);
            if (!objLinkIDsV.contains(link.fileLinkId))
              objLinkIDsV.addElement(link.fileLinkId);
          }
        }
      }
      if (shareIDsV != null && shareIDsV.size() > 0 && objLinkIDsV != null && objLinkIDsV.size() > 0) {
        Long[] shareIDs = new Long[shareIDsV.size()];
        shareIDsV.toArray(shareIDs);
        Long[] objLinkIDs = new Long[objLinkIDsV.size()];
        objLinkIDsV.toArray(objLinkIDs);

        Stats_Get_Rq request = new Stats_Get_Rq();
        request.statsForObjType = Short.valueOf(Record.RECORD_TYPE_FILE_LINK);
        request.ownerObjType = Short.valueOf(Record.RECORD_TYPE_SHARE);
        request.ownerObjIDs = shareIDs;
        request.objLinkIDs = objLinkIDs;

        getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.STAT_Q_GET, request));
      }
    }


    // if this was a bulk fetch done in stages, continue the fetch
    if (fileLinks != null && fetchNumMax != null) {
      int fetchNumMaxLength = Math.abs(fetchNumMax.shortValue());
      if (fetchingOwnerObjType != null && fetchingOwnerObjType.shortValue() != Record.RECORD_TYPE_FOLDER)
        throw new IllegalStateException("Don't know how to handle staged fetching of non-folder files.");
      Long fetchingFolderId = fetchingOwnerObjId; // this was always a folder staged fetch
      FolderRecord fetchingFolderRec = cache.getFolderRecord(fetchingFolderId);
      if (fileLinks.length < fetchNumMaxLength) {
        // fetch completed, time to notify listeners
        // check it just in case...
        if (fetchingFolderRec != null)
          cache.fireFolderRecordUpdated(new FolderRecord[] { fetchingFolderRec }, RecordEvent.FOLDER_FETCH_COMPLETED);
      } else {
        if (isInterrupted()) {
          interrupt();
          // When connecting to pre build 388 engine this may be null when number of messages in the folder modulus fetchNumMax = 0, so check it just in case...
          if (fetchingFolderRec != null)
            cache.fireFolderRecordUpdated(new FolderRecord[] { fetchingFolderRec }, RecordEvent.FOLDER_FETCH_INTERRUPTED);
        } else {
          Timestamp timeStamp = fileLinks[fileLinks.length-1].recordCreated;
          if (groupIDsSet == null) groupIDsSet = cache.getFolderGroupIDsMySet();
          Long fetchingShareId = cache.getFolderShareRecordMy(fetchingFolderId, groupIDsSet).shareId;

          short numMax = fetchNumMax.shortValue();

          long startTime = getStampTime();
          long endTime = System.currentTimeMillis();
          double ellapsed = (double) Math.max(1, endTime-startTime); // avoid division by zero
          double multiplier = ((double) MsgAGet.MAX_BATCH_MILLIS) / ellapsed; // adjust the new fetch size so that it doesn't take too much time
          // multiplier cannot make too drastic of a change
          multiplier = Math.max(0.2, Math.min(10.0, multiplier));

          // apply adjustment to regular fetch size
          numMax = (short) (fetchNumMax.shortValue() * multiplier);
          if (numMax == 0) numMax = fetchNumMax.shortValue(); // on error repeat previous iteration value

          // apply hard maximum limits
          if (numMax > File_GetFiles_Rq.FETCH_NUM_LIST__MAX_SIZE__HARD_LIMIT)
            numMax = File_GetFiles_Rq.FETCH_NUM_LIST__MAX_SIZE__HARD_LIMIT;
          else if (numMax < -File_GetFiles_Rq.FETCH_NUM_LIST__MAX_SIZE__HARD_LIMIT)
            numMax = -File_GetFiles_Rq.FETCH_NUM_LIST__MAX_SIZE__HARD_LIMIT;

          // apply hard minimum limits
          if (numMax > 0)
            numMax = (short) Math.max(numMax, File_GetFiles_Rq.FETCH_NUM_LIST__INITIAL_SIZE);
          else if (numMax < 0)
            numMax = (short) -Math.max(-numMax, File_GetFiles_Rq.FETCH_NUM_LIST__INITIAL_SIZE);

          File_GetFiles_Rq request = new File_GetFiles_Rq(fetchingShareId, Record.RECORD_TYPE_FOLDER, fetchingFolderId, numMax, timeStamp);
          MessageAction msgAction = new MessageAction(CommandCodes.FILE_Q_GET_FILES_STAGED, request);
          msgAction.setInterruptsFrom(this);
          getServerInterfaceLayer().submitAndReturn(msgAction);
        }
      }
    }

    if (trace != null) trace.exit(FileAGetFiles.class, null);
    return null;
  }

}