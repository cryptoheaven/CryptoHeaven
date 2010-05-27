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

package com.CH_cl.service.actions.usr;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.*;
import com.CH_cl.service.ops.*;

import com.CH_co.trace.Trace;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.msg.dataSets.usr.*;
import com.CH_co.service.records.*;
import com.CH_co.util.*;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
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
      getServerInterfaceLayer().submitAndReturn(new MessageAction(CommandCodes.SYS_Q_GET_AUTO_UPDATE));
    }

    if (trace != null) trace.exit(UsrAGetInfo.class);
    return null;
  }

}