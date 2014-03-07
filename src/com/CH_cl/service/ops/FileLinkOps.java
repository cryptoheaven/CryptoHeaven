/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.ops;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.DefaultReplyRunner;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_co.queue.ProcessingFunctionI;
import com.CH_co.queue.QueueMM1;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.obj.Obj_IDs_Co;
import com.CH_co.service.msg.dataSets.stat.Stats_Update_Rq;
import com.CH_co.service.records.*;
import com.CH_co.util.Misc;
import java.util.ArrayList;

/**
* Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: $</b>
* @author  Marcin
* @version
*/
public class FileLinkOps {

  private static QueueMM1 linkFetchQueue = null;

  /**
  * Fetches from Server or returns from cache... object state is not checked.. so if file uploads are incomplete consider refreshing instead.
  * @param SIL
  * @param ownerLinkId
  * @param ownerObjId
  * @param ownerType
  * @return
  */
  public static FileLinkRecord[] getOrFetchFileLinksByOwner(ServerInterfaceLayer SIL, Long ownerLinkId, Long ownerObjId, short ownerType) {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    FileLinkRecord[] fLinks = null;
    if (ownerType == Record.RECORD_TYPE_MESSAGE) {
      MsgDataRecord mData = cache.getMsgDataRecord(ownerObjId);
      if (mData != null && mData.attachedFiles.shortValue() > 0) {
        fLinks = cache.getFileLinkRecordsOwnerAndType(ownerObjId, new Short(ownerType));
        if (fLinks != null && fLinks.length == mData.attachedFiles.shortValue()) {
          // we have all file link attachments
        } else {
          // fetch file link attachments
          Long[] fromShareIDs = null;

          // if message belongs to folder, specify the shareId in the query
          MsgLinkRecord mLink = cache.getMsgLinkRecord(ownerLinkId);
          if (mLink != null) {
            if (mLink.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER) {
              FolderShareRecord fShare = cache.getFolderShareRecordMy(mLink.ownerObjId, true);
              fromShareIDs = new Long[] { fShare.shareId };
            }

            Obj_IDs_Co request = new Obj_IDs_Co();
            request.IDs = new Long[2][];
            request.IDs[0] = new Long[] { mLink.msgLinkId };
            request.IDs[1] = fromShareIDs;

            ClientMessageAction msgAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.FILE_Q_GET_MSG_FILE_ATTACHMENTS, request), 60000);
            if (msgAction != null) {
              Misc.suppressMsgDialogsGUI(true);
              DefaultReplyRunner.nonThreadedRun(SIL, msgAction);
              Misc.suppressMsgDialogsGUI(false);
              if (msgAction.getActionCode() > 0) {
                // no error
                // re-query the cache after the request has completed
                fLinks = cache.getFileLinkRecordsOwnerAndType(ownerObjId, new Short(ownerType));
                // when we are all done fetching, resubmit owner Msg to cache for listeners to update rendering of attachments
                cache.addMsgLinkRecords(new MsgLinkRecord[] { mLink });
              }
            }
          }
        }
      }
    } else {
      throw new IllegalArgumentException("Don't know how to handle ownerType " + ownerType);
    }
    return fLinks;
  }

  public static void markRecordsAs(ServerInterfaceLayer SIL, FileLinkRecord[] records, Short newMark) {
    if (records != null && records.length > 0) {
      // gather all stats which need to be updated
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      ArrayList statsL = new ArrayList();
      for (int i=0; i<records.length; i++) {
        StatRecord statRecord = cache.getStatRecordMyLinkId(records[i].fileLinkId, FetchedDataCache.STAT_TYPE_INDEX_FILE);
        if (statRecord != null && !statRecord.mark.equals(newMark))
          statsL.add(statRecord);
      }
      if (statsL.size() > 0) {
        StatRecord[] stats = new StatRecord[statsL.size()];
        statsL.toArray(stats);
        // clone the stats to send the request
        StatRecord[] statsClones = (StatRecord[]) RecordUtils.cloneRecords(stats);

        // set mark to "newMark" on the clones
        for (int i=0; i<statsClones.length; i++)
          statsClones[i].mark = newMark;

        Stats_Update_Rq request = new Stats_Update_Rq(statsClones);
        SIL.submitAndReturn(new MessageAction(CommandCodes.STAT_Q_UPDATE, request));
      }
    }
  }
  
  public synchronized static void addToLinkFetchQueue(ServerInterfaceLayer SIL, Long ownerLinkId, Long ownerObjId, short ownerType) {
    if (linkFetchQueue == null) {
      linkFetchQueue = new QueueMM1("File Link Fetch Queue", new QueueFetchProcessor());
    }
    linkFetchQueue.getFifoWriterI().add(new Object[] { SIL, ownerLinkId, ownerObjId, new Short(ownerType) });
  }

  private static class QueueFetchProcessor implements ProcessingFunctionI {
    public Object processQueuedObject(Object obj) {
      Object[] objSet = (Object[]) obj;
      getOrFetchFileLinksByOwner((ServerInterfaceLayer) objSet[0], (Long) objSet[1], (Long) objSet[2], ((Short) objSet[3]).shortValue());
      return null;
    }
  }
}