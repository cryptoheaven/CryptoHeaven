/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.actions.key;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.ops.KeyOps;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.key.Key_KeyPairs_Rp;
import com.CH_co.service.records.KeyRecord;
import com.CH_co.service.records.UserRecord;
import com.CH_co.trace.Trace;
import com.CH_co.util.Misc;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.7 $</b>
 *
 * @author  Marcin Kurzawa
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