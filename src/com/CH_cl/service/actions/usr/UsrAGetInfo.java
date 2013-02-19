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

package com.CH_cl.service.actions.usr;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.ops.AutoUpdater;
import com.CH_cl.service.ops.KeyOps;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.obj.Obj_List_Co;
import com.CH_co.service.msg.dataSets.usr.Usr_GetMyInfo_Rp;
import com.CH_co.service.records.UserRecord;
import com.CH_co.service.records.UserSettingsRecord;
import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;

/** 
* <b>Copyright</b> &copy; 2001-2013
* <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
* CryptoHeaven Corp.
* </a><br>All rights reserved.<p>
*
* @author  Marcin Kurzawa
* @version
*/
public class UsrAGetInfo extends ClientMessageAction {

  /** Creates new UsrAGetInfo */
  public UsrAGetInfo() {
  }

  /** The action handler performs all actions related to the received message (reply),
      and optionally returns a request Message.  If there is no request, null is returned.
  */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UsrAGetInfo.class, "runAction()");

    FetchedDataCache cache = getFetchedDataCache();

    Usr_GetMyInfo_Rp reply = (Usr_GetMyInfo_Rp) getMsgDataSet();
    UserRecord uRec = reply.userRecord;

    boolean isMyPasswordResetBitSwitchedON = false;
    if (cache.getUserRecord() != null &&
          !Misc.isBitSet(cache.getUserRecord().flags, UserRecord.FLAG_ENABLE_PASSWORD_RESET_KEY_RECOVERY) &&
            Misc.isBitSet(uRec.flags, UserRecord.FLAG_ENABLE_PASSWORD_RESET_KEY_RECOVERY))
    {
      isMyPasswordResetBitSwitchedON = true;
    }

    if (cache.getUserRecord() != null &&
            Misc.isBitSet(cache.getUserRecord().flags, UserRecord.FLAG_DISABLE_AUTO_UPDATES) &&
            !Misc.isBitSet(uRec.flags, UserRecord.FLAG_DISABLE_AUTO_UPDATES))
    {
      AutoUpdater.resetInactiveStamp();
    }

    if (uRec != null) {
      cache.setUserRecord(uRec);
    }
    UserSettingsRecord userSettingsRecord = reply.userSettingsRecord;
    if (userSettingsRecord != null) {
      cache.setUserSettingsRecord(userSettingsRecord);
    }

    if (uRec != null &&
        uRec.defaultEmlId != null &&
        uRec.defaultEmlId.longValue() != UserRecord.GENERIC_EMAIL_ID &&
        cache.getEmailRecord(uRec.defaultEmlId) == null)
    {
      Object[] set = new Object[] { null, new Object[] { uRec.userId }, null, null };
      Obj_List_Co request = new Obj_List_Co(set);
      getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.EML_Q_GET, request));
    }

    // if Password Reset bit was switched ON, then send the update
    if (isMyPasswordResetBitSwitchedON && uRec.isBusinessSubAccount()) {
      if (getClientContext().serverBuild >= 452 && getClientContext().clientBuild >= 452) {
        KeyOps.sendKeyRecovery(getServerInterfaceLayer());
      }
    }

    if (!Misc.isAllGUIsuppressed() && AutoUpdater.isLongInactive()) {
      if (AutoUpdater.isRunningFromJar()) {
        // Request Auto-Update if we are running from JAR, and skip for classes or Android.
        AutoUpdater.markActivityStamp();
        getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.SYS_Q_GET_AUTO_UPDATE));
      }
    }

    if (trace != null) trace.exit(UsrAGetInfo.class);
    return null;
  }

}