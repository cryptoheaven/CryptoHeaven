/**
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
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
import com.CH_cl.util.MsgUtils;
import com.CH_co.queue.ProcessingFunctionI;
import com.CH_co.queue.QueueMM1;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.stat.Stats_Update_Rq;
import com.CH_co.service.records.MsgLinkRecord;
import com.CH_co.service.records.StatRecord;
import java.util.ArrayList;

/**
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* @author  Marcin Kurzawa
*/
public class StatOps {

  private static QueueMM1 statFetchQueue = null;

  public synchronized static void addToStatFetchQueue(ServerInterfaceLayer SIL, MsgLinkRecord msgLink) {
    if (statFetchQueue == null) {
      statFetchQueue = new QueueMM1("Stat Fetch Queue", new QueueFetchProcessor());
    }
    if (!SIL.getFetchedDataCache().wasStatFetchForMsgIdRecent(msgLink.msgId)) { 
      statFetchQueue.getFifoWriterI().add(new Object[] { SIL, msgLink });
    }
  }

  private static class QueueFetchProcessor implements ProcessingFunctionI {
    public Object processQueuedObject(Object obj) {
      Object[] objSet = (Object[]) obj;
      fetchStatsForMsgIfNotRecent((ServerInterfaceLayer) objSet[0], (MsgLinkRecord) objSet[1]);
      return null;
    }
  }

  private static void fetchStatsForMsgIfNotRecent(ServerInterfaceLayer SIL, MsgLinkRecord msgLink) {
    if (!SIL.getFetchedDataCache().wasStatFetchForMsgIdRecent(msgLink.msgId)) { 
      fetchStatsForMsg(SIL, msgLink);
    }
  }

  private static void fetchStatsForMsg(ServerInterfaceLayer SIL, MsgLinkRecord msgLink) {
    ClientMessageAction msgAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.STAT_Q_FETCH_OBJ_EXISTING, MsgUtils.prepMsgStatRequest(SIL.getFetchedDataCache(), msgLink)), 60000);
    if (msgAction != null && msgAction.getActionCode() == CommandCodes.STAT_A_GET) {
      SIL.getFetchedDataCache().markStatFetchedForMsgId(msgLink.msgId);
    }
    DefaultReplyRunner.nonThreadedRun(SIL, msgAction);
  }

  public static void markOldIfNeeded(ServerInterfaceLayer SIL, Long objLinkId, int statType) {
    StatRecord stat = SIL.getFetchedDataCache().getStatRecordMyLinkId(objLinkId, statType);
    if (stat != null && stat.isFlagRed()) {
      // clone the stats to send the request
      StatRecord statClone = (StatRecord) stat.clone();
      // set mark to "old" on the clone
      statClone.mark = StatRecord.FLAG_OLD;
      Stats_Update_Rq request = new Stats_Update_Rq(new StatRecord[] { statClone });
      SIL.submitAndReturn(new MessageAction(CommandCodes.STAT_Q_UPDATE, request));
    }
  }

  public static void markOldCachedMsgsInFolder(ServerInterfaceLayer SIL, Long folderId) {
    FetchedDataCache cache = SIL.getFetchedDataCache();
    MsgLinkRecord[] msgLinks = cache.getMsgLinkRecordsForFolder(folderId);
    if (msgLinks != null && msgLinks.length > 0) {
      ArrayList statUpdatesL = new ArrayList();
      for (int i=0; i<msgLinks.length; i++) {
        StatRecord stat = cache.getStatRecordMyLinkId(msgLinks[i].msgLinkId, FetchedDataCache.STAT_TYPE_INDEX_MESSAGE);
        if (stat != null && stat.mark.equals(StatRecord.FLAG_NEW)) {
          // clone the stats to send the request
          StatRecord statClone = (StatRecord) stat.clone();
          // set mark to "old" on the clone
          statClone.mark = StatRecord.FLAG_OLD;
          statUpdatesL.add(statClone);
        }
      }
      if (!statUpdatesL.isEmpty()) {
        StatRecord[] statUpdates = new StatRecord[statUpdatesL.size()];
        statUpdatesL.toArray(statUpdates);
        Stats_Update_Rq request = new Stats_Update_Rq(statUpdates);
        SIL.submitAndReturn(new MessageAction(CommandCodes.STAT_Q_UPDATE, request));
      }
    }
  }

}