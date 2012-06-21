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

package com.CH_cl.service.ops;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.cache.TextRenderer;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_co.cryptx.BASymmetricKey;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.ProtocolMsgDataSet;
import com.CH_co.service.msg.dataSets.obj.Obj_IDList_Co;
import com.CH_co.service.records.*;

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
* <b>$Revision: 1.9 $</b>
* @author  Marcin Kurzawa
* @version
*/
public class MsgDataOps extends Object {

  private static String STR_RE = com.CH_cl.lang.Lang.rb.getString("msg_Re");
  private static String STR_FWD = com.CH_cl.lang.Lang.rb.getString("msg_Fwd");

  public static String getSubjectForward(Object[] attachments) {
    return getSubjectForward(attachments, 0);
  }
  public static String getSubjectForward(Object[] attachments, int truncateShort) {
    StringBuffer sb = new StringBuffer();
    for (int i=0; i<attachments.length; i++) {
      sb.append(TextRenderer.getRenderedText(attachments[i]));
      if (i < attachments.length - 1)
        sb.append("; ");
    }
    String subject = STR_FWD + " [" + sb.toString() + "]";
    if (truncateShort > 0 && subject.length() > truncateShort) {
      subject = subject.substring(0, truncateShort) + "...";
    }
    return subject;
  }

  public static String getSubjectReply(MsgLinkRecord replyToLink) {
    return getSubjectReply(replyToLink, 0);
  }
  public static String getSubjectReply(MsgLinkRecord replyToLink, int truncateShort) {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    MsgDataRecord replyToData = cache.getMsgDataRecord(replyToLink.msgId);
    return getSubjectReply(replyToData, truncateShort);
  }

  public static String getSubjectReply(MsgDataRecord replyToData, int truncateShort) {
    String subject = STR_RE + " " + eliminatePrefixes(replyToData.getSubject());
    if (truncateShort > 0 && subject.length() > truncateShort) {
      subject = subject.substring(0, truncateShort) + "...";
    }
    return subject;
  }

  public static String eliminatePrefixes(String str) {
    boolean changed = false;
    while (true) {
      str = str.trim();
      if (str.startsWith(STR_RE + " ") || str.toUpperCase().startsWith(STR_RE.toUpperCase() + " ")) {
        str = str.substring(STR_RE.length() + 1);
        changed = true;
      }
      if (str.startsWith(STR_FWD + " ") || str.toUpperCase().startsWith(STR_FWD.toUpperCase() + " ")) {
        str = str.substring(STR_FWD.length() + 1);
        changed = true;
      }

      if (!changed)
        break;
      else
        changed = false;
    }
    return str;
  }

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