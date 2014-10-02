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
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_co.queue.ProcessingFunctionI;
import com.CH_co.queue.QueueMM1;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.msg.dataSets.obj.Obj_IDList_Co;
import com.CH_co.service.records.*;
import com.CH_co.util.CallbackI;

/**
* Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.9 $</b>
*
* @author  Marcin Kurzawa
*/
public class MsgDataOps extends Object {

  private static QueueMM1 bodyFetchQueue = null;

  public static MsgDataRecord getOrFetchMsgBody(ServerInterfaceLayer SIL, Long msgLinkId, Long msgId) {
    FetchedDataCache cache = SIL.getFetchedDataCache();
    MsgDataRecord mData = cache.getMsgDataRecord(msgId);
    if (mData != null && mData.getEncText() != null) {
      // msg was cached, return it
    } else {
      MsgLinkRecord mLink = cache.getMsgLinkRecord(msgLinkId);
      ProtocolMsgDataSet request = prepareRequestToFetchMsgBody(cache, mLink);
      SIL.submitAndWait(new MessageAction(CommandCodes.MSG_Q_GET_BODY, request), 30000);
      // re-check the cache after request has completed
      mData = cache.getMsgDataRecord(msgId);
    }
    return mData;
  }

  public static ProtocolMsgDataSet prepareRequestToFetchMsgBody(final FetchedDataCache cache, MsgLinkRecord msgLink) {
    Obj_IDList_Co request = null;
    Long shareId = null;
    // if not an attachment
    if (msgLink.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER) {
      FolderShareRecord share = cache.getFolderShareRecordMy(msgLink.ownerObjId, true);
      if (share != null)
        shareId = share.shareId;
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

  public synchronized static void addToBodyFetchQueue(ServerInterfaceLayer SIL, MsgLinkRecord msgLink, CallbackI callback) {
    if (bodyFetchQueue == null) {
      bodyFetchQueue = new QueueMM1("Msg Body Fetch Queue", new QueueFetchProcessor());
    }
    bodyFetchQueue.getFifoWriterI().add(new Object[] { SIL, msgLink, callback });
  }

  private static class QueueFetchProcessor implements ProcessingFunctionI {
    public Object processQueuedObject(Object obj) {
      Object[] objSet = (Object[]) obj;
      ServerInterfaceLayer SIL = (ServerInterfaceLayer) objSet[0];
      MsgLinkRecord msgLink = (MsgLinkRecord) objSet[1];
      CallbackI callback = (CallbackI) objSet[2];
      MsgDataRecord msgData = null;
      if (SIL != null && msgLink != null)
        msgData = getOrFetchMsgBody(SIL, msgLink.msgLinkId, msgLink.msgId);
      if (callback != null)
        callback.callback(new Object[] { msgLink, msgData });
      return null;
    }
  }
}