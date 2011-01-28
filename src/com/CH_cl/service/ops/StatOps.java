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

package com.CH_cl.service.ops;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.stat.Stats_Update_Rq;
import com.CH_co.service.records.StatRecord;

/**
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
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
}