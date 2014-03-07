/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.actions.usr;

import com.CH_cl.service.actions.*;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.ops.KeyOps;
import com.CH_co.trace.Trace;
import com.CH_co.service.msg.*;
import com.CH_co.service.records.*;
import com.CH_co.service.msg.dataSets.usr.*;
import com.CH_co.util.Misc;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.10 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class UsrAGetSubAcc extends ClientMessageAction {

  /** Creates new UsrAGetSubAcc */
  public UsrAGetSubAcc() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UsrAGetSubAcc.class, "UsrAGetSubAcc()");
    if (trace != null) trace.exit(UsrAGetSubAcc.class);
  }

  /**
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UsrAGetSubAcc.class, "runAction(Connection)");

    Usr_GetSubAcc_Rp reply = (Usr_GetSubAcc_Rp) getMsgDataSet();
    UserRecord[] userRecords = reply.userRecords;
    EmailRecord[] emailRecords = reply.emailRecords;

    FetchedDataCache cache = getFetchedDataCache();
    Long myUserId = cache.getMyUserId();
    UserRecord myUserRec = cache.getUserRecord();

    boolean isMyPasswordResetBitSwitchedON = false;
    if (myUserRec != null && myUserId != null && userRecords != null) {
      for (int i=0; i<userRecords.length; i++) {
        UserRecord uRec = userRecords[i];
        if (myUserId.equals(uRec.userId) &&
               !Misc.isBitSet(myUserRec.flags, UserRecord.FLAG_ENABLE_PASSWORD_RESET_KEY_RECOVERY) &&
                Misc.isBitSet(uRec.flags, UserRecord.FLAG_ENABLE_PASSWORD_RESET_KEY_RECOVERY))
        {
          isMyPasswordResetBitSwitchedON = true;
        }
      }
    }

    cache.addEmailRecords(emailRecords);
    cache.addUserRecords(userRecords);

    // if Password Reset bit was switched ON, then send the update
    if (isMyPasswordResetBitSwitchedON && myUserRec.isBusinessSubAccount()) {
      if (getClientContext().serverBuild >= 452 && getClientContext().clientBuild >= 452) {
        KeyOps.sendKeyRecovery(getServerInterfaceLayer());
      }
    }

    if (trace != null) trace.exit(UsrAGetSubAcc.class, null);
    return null;
  }

}