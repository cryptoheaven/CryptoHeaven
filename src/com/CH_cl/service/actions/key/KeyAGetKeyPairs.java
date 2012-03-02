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

package com.CH_cl.service.actions.key;

import com.CH_cl.service.actions.*;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.ops.KeyOps;
import com.CH_co.cryptx.BASymmetricKey;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.trace.Trace;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.records.KeyRecord;
import com.CH_co.service.msg.dataSets.key.*;
import com.CH_co.service.msg.dataSets.obj.Obj_IDList_Co;
import com.CH_co.service.records.KeyRecoveryRecord;
import com.CH_co.service.records.UserRecord;
import com.CH_co.util.Misc;

/** 
 * <b>Copyright</b> &copy; 2001-2012
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
 * <b>$Revision: 1.7 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class KeyAGetKeyPairs extends ClientMessageAction {

  /** Creates new KeyAGetKeyPairs */
  public KeyAGetKeyPairs() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(KeyAGetKeyPairs.class, "KeyAGetKeyPairs()");
    if (trace != null) trace.exit(KeyAGetKeyPairs.class);
  }

  /**
   * The action handler performs all actions related to the received message (reply),
   * and optionally returns a request Message.  If there is no request, null is returned.
   */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(KeyAGetKeyPairs.class, "runAction(Connection)");

    final FetchedDataCache cache = getFetchedDataCache();

    Key_KeyPairs_Rp set = (Key_KeyPairs_Rp) getMsgDataSet();
    KeyRecord[] keyRecords = set.keyRecords;
    cache.addKeyRecords(keyRecords);

    if (getCommonContext().clientBuild >= 452 && getCommonContext().serverBuild >= 452) {
      // Key Recovery option for sub-accounts
      UserRecord uRec = cache.getUserRecord();
      if (Misc.isBitSet(uRec.flags, UserRecord.FLAG_ENABLE_PASSWORD_RESET_KEY_RECOVERY)) {
        if (uRec.isBusinessSubAccount()) {
          boolean found = false;
          if (set.recoveryRecords != null) {
            for (int i=0; i<set.recoveryRecords.length; i++) {
              if (set.recoveryRecords[i].masterUID.equals(uRec.masterId)) {
                if (set.recoveryRecords[i].masterKeyId != null) {
                  found = true;
                  break;
                }
              }
            }
          }
          if (!found) {
            KeyOps.sendKeyRecovery(getServerInterfaceLayer());
          }
        }
      }
    }

    if (trace != null) trace.exit(KeyAGetKeyPairs.class, null);
    return null;
  }

}