/*
* Copyright 2001-2012 by CryptoHeaven Corp.,
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
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.stat.Stats_Update_Rq;
import com.CH_co.service.records.MsgLinkRecord;
import com.CH_co.service.records.StatRecord;
import java.util.ArrayList;

/**
* <b>Copyright</b> &copy; 2001-2012
* <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
* CryptoHeaven Corp.
* </a><br>All rights reserved.<p>
*
*
* @author  Marcin Kurzawa
* @version
*/
public class StatOps {

  public static void markOldIfNeeded(ServerInterfaceLayer SIL, Long statId, int statType) {
    StatRecord stat = FetchedDataCache.getSingleInstance().getStatRecord(statId, statType);
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
        StatRecord stat = cache.getStatRecord(msgLinks[i].msgLinkId, FetchedDataCache.STAT_TYPE_INDEX_MESSAGE);
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