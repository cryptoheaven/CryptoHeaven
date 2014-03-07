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

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.util.GlobalSubProperties;
import com.CH_cl.service.engine.*;

import com.CH_co.cryptx.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.key.Key_KeyRecov_Co;
import com.CH_co.service.msg.dataSets.obj.Obj_IDList_Co;
import com.CH_co.service.records.*;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.7 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class KeyOps extends Object {

  public static boolean isKeyStoredLocally(Long keyID) {
    String keyPropertyName = "Enc" + RSAPrivateKey.OBJECT_NAME + "_" + keyID;
    GlobalSubProperties keyProperties = new GlobalSubProperties(GlobalSubProperties.PROPERTY_EXTENSION_KEYS);
    String property = keyProperties.getProperty(keyPropertyName);
    boolean isLocalKey = (property != null && property.length() > 0);
    return isLocalKey;
  }

  public static void sendKeyRecovery(final ServerInterfaceLayer SIL) {
    // prepare required password reset data set
    final FetchedDataCache cache = SIL.getFetchedDataCache();
    UserRecord uRec = cache.getUserRecord();
    KeyRecord masterKey = cache.getKeyRecordForUser(uRec.masterId);
    Runnable afterJob = new Runnable() {
      public void run() {
        KeyRecord masterKey = cache.getKeyRecordForUser(cache.getUserRecord().masterId);
        if (masterKey != null) {
          KeyRecoveryRecord recoveryRecord = new KeyRecoveryRecord();
          recoveryRecord.seal(masterKey, cache.getKeyRecordMyCurrent(), new BASymmetricKey(32));
          SIL.submitAndReturn(new MessageAction(CommandCodes.KEY_Q_SET_KEY_RECOVERY, new Key_KeyRecov_Co(recoveryRecord)));
        }
      }
    };
    if (masterKey == null) {
      SIL.submitAndReturn(new MessageAction(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_USERS, new Obj_IDList_Co(uRec.masterId)), 30000, afterJob, null);
    } else {
      afterJob.run();
    }
  }
}