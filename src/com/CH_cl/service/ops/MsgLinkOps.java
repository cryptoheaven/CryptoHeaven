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
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.Obj_IDs_Co;
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
 * <b>$Revision: 1.0 $</b>
 * @author  Marcin
 * @version
 */
public class MsgLinkOps {

  public static MsgLinkRecord[] getOrFetchMsgLinksByOwner(ServerInterfaceLayer SIL, Long ownerLinkId, Long ownerObjId, short ownerType) {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    MsgLinkRecord[] mLinks = null;
    if (ownerType == Record.RECORD_TYPE_MESSAGE) {
      MsgDataRecord mData = cache.getMsgDataRecord(ownerObjId);
      if (mData.attachedMsgs.shortValue() > 0) {
        mLinks = cache.getMsgLinkRecordsOwnerAndType(ownerObjId, new Short(ownerType));
        if (mLinks != null && mLinks.length == mData.attachedMsgs.shortValue()) {
          // we have all msg link attachments
        } else {
          // fetch msg link attachments
          Long[] fromShareIDs = null;

          // if message belongs to folder, specify the shareId in the query
          MsgLinkRecord mLink = cache.getMsgLinkRecord(ownerLinkId);
          if (mLink.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER) {
            FolderShareRecord fShare = cache.getFolderShareRecordMy(mLink.ownerObjId, true);
            fromShareIDs = new Long[] { fShare.shareId };
          }

          Obj_IDs_Co request = new Obj_IDs_Co();
          request.IDs = new Long[2][];
          request.IDs[0] = new Long[] { mLink.msgLinkId };
          request.IDs[1] = fromShareIDs;

          SIL.submitAndWait(new MessageAction(CommandCodes.MSG_Q_GET_MSG_ATTACHMENT_BRIEFS, request));

          // re-query the cache after the request has completed
          mLinks = cache.getMsgLinkRecordsOwnerAndType(ownerObjId, new Short(ownerType));
        }
      }
    } else {
      throw new IllegalArgumentException("Don't know how to handle ownerType " + ownerType);
    }
    return mLinks;
  }

}