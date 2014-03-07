/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.util;

import com.CH_cl.service.cache.CacheMsgUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_co.cryptx.BASymCipherBulk;
import com.CH_co.service.msg.dataSets.stat.Stats_Get_Rq;
import com.CH_co.service.records.MsgDataRecord;
import com.CH_co.service.records.MsgLinkRecord;
import com.CH_co.service.records.Record;
import com.CH_co.service.records.RecordUtils;
import com.CH_co.util.Hasher;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author  Marcin Kurzawa
 */
public class MsgUtils {

    public static Hasher.Set getMatchingPasswordHasher(MsgDataRecord msgDataRecord, String pass) {
    Hasher.Set matchingSet = null;
    pass = pass.trim();
    Hasher.Set set = new Hasher.Set(pass.toCharArray());
    if (set.passwordHash.equals(msgDataRecord.bodyPassHash)) {
      matchingSet = set;
    } else {
      // also try the new trimmed versions for Question & Answer
      pass = getTrimmedPassword(pass);
      set = new Hasher.Set(pass.toCharArray());
      if (set.passwordHash.equals(msgDataRecord.bodyPassHash)) {
        matchingSet = set;
      }
    }
    return matchingSet;
  }

  public static String getTrimmedPassword(String pass) {
    String passStripped = null;
    if (pass != null) {
      StringBuffer passStrippedBuf = new StringBuffer();
      for (int i=0; i<pass.length(); i++) {
        char lCh = pass.charAt(i);
        lCh = Character.toLowerCase(lCh);
        if (Character.isLetterOrDigit(lCh))
          passStrippedBuf.append(lCh);
      }
      passStripped = passStrippedBuf.toString();
    }
    return passStripped;
  }

  public static Stats_Get_Rq prepMsgStatRequest(MsgLinkRecord msgLink) {
    Stats_Get_Rq request = new Stats_Get_Rq();
    request.statsForObjType = new Short(Record.RECORD_TYPE_MSG_LINK);
    request.objLinkIDs = new Long[] { msgLink.msgLinkId };
    if (msgLink.getOwnerObjType().shortValue() == Record.RECORD_TYPE_MESSAGE) {
      request.ownerObjType = new Short(Record.RECORD_TYPE_MESSAGE);
      Long[] msgIDs = new Long[] { msgLink.ownerObjId };
      request.ownerObjIDs = msgIDs;
    } else {
      request.ownerObjType = new Short(Record.RECORD_TYPE_SHARE);
      Long[] folderIDs = new Long[] { msgLink.ownerObjId };
      Long[] shareIDs = RecordUtils.getIDs(FetchedDataCache.getSingleInstance().getFolderSharesMyForFolders(folderIDs, true));
      request.ownerObjIDs = shareIDs;
    }
    return request;
  }

  public static void unlockPassProtectedMsg(MsgDataRecord msgDataRecord, Hasher.Set matchingSet) {
    if (matchingSet != null) {
      BASymCipherBulk encText = msgDataRecord.getEncText();
      if (encText != null && encText.size() > 0 && msgDataRecord.getTextBody() == null) {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();
        cache.addMsgBodyKey(matchingSet);
        CacheMsgUtils.unlockPassProtectedMsgIncludingCached(msgDataRecord, matchingSet);
      }
    }
  }
  
}