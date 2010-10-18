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

package com.CH_cl.service.ops;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.engine.*;

import com.CH_co.cryptx.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;

/**
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.9 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class MsgDataOps extends Object {

  public static MsgDataRecord getOrFetchMsgBody(ServerInterfaceLayer SIL, Long msgLinkId, Long msgId) {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    MsgDataRecord mData = cache.getMsgDataRecord(msgId);
    if (mData != null && mData.getEncText() != null) {
      // msg was cached, return it
    } else {
      MsgLinkRecord mLink = cache.getMsgLinkRecord(msgLinkId);
      ProtocolMsgDataSet request = prepareRequestToFetchMsgBody(mLink);
      SIL.submitAndWait(new MessageAction(CommandCodes.MSG_Q_GET_BODY, request), 30000);
      // re-check the cache after request has completed
      mData = cache.getMsgDataRecord(msgId);
    }
    return mData;
  }

  public static ProtocolMsgDataSet prepareRequestToFetchMsgBody(MsgLinkRecord msgLink) {
    Obj_IDList_Co request = null;
    Long shareId = null;
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    // if not an attachment
    if (msgLink.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER) {
      shareId = cache.getFolderShareRecordMy(msgLink.ownerObjId, true).shareId;
      request = new Obj_IDList_Co(new Long[] {shareId, msgLink.msgLinkId});
    } else if (msgLink.ownerObjType.shortValue() == Record.RECORD_TYPE_MESSAGE) {
      MsgDataRecord parentMsgData = cache.getMsgDataRecord(msgLink.ownerObjId);
      MsgLinkRecord[] parentMsgLinks = cache.getMsgLinkRecordsForMsg(parentMsgData.msgId);
      MsgLinkRecord parentMsgLink = parentMsgLinks != null && parentMsgLinks.length > 0 ? parentMsgLinks[0] : null;
      FolderShareRecord shareRecord = null;
      if (parentMsgLink != null && parentMsgLink.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER)
        shareRecord = cache.getFolderShareRecordMy(parentMsgLink.ownerObjId, true);
      if (shareRecord != null)
        shareId = shareRecord.shareId;
      Long parentLinkId = parentMsgLink != null ? parentMsgLink.msgLinkId : null;
      request = new Obj_IDList_Co(new Long[] {shareId, msgLink.msgLinkId, parentLinkId});
    } else {
      throw new IllegalArgumentException("Unsupported owner object type!");
    }
    return request;
  }

  public static void tryToUnsealMsgDataWithVerification(MsgDataRecord dataRecord) {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    MsgLinkRecord[] linkRecords = cache.getMsgLinkRecordsForMsg(dataRecord.msgId);
    if (linkRecords != null && linkRecords.length > 0) {
      // if this data record contains sendPrivKeyId, then signature needs to be verified
      if (dataRecord.getSendPrivKeyId() != null) {
        KeyRecord msgSigningKeyRec = cache.getKeyRecord(dataRecord.getSendPrivKeyId());
        if (msgSigningKeyRec != null) {
          BASymmetricKey symKey = null;
          for (int i=0; i<linkRecords.length; i++) {
            if (linkRecords[i] != null)
              symKey = linkRecords[i].getSymmetricKey();
            if (symKey != null)
              break;
          }
          if (symKey != null)
            dataRecord.unSeal(symKey, cache.getMsgBodyKeys(), msgSigningKeyRec);
        }
      }
    }
  }

}