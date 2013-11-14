/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_cl.service.actions.cnt;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.obj.Obj_List_Co;
import com.CH_co.service.records.ContactRecord;
import com.CH_co.service.records.FolderShareRecord;
import com.CH_co.service.records.MsgLinkRecord;
import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;
import com.CH_co.util.Misc;
import java.sql.Timestamp;
import java.util.ArrayList;

/** 
* Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.10 $</b>
*
* @author  Marcin Kurzawa
*/
public class CntAUpdateUsed extends ClientMessageAction {

  /** Creates new CntAUpdateUsed */
  public CntAUpdateUsed() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CntAUpdateUsed.class, "CntAUpdateUsed()");
    if (trace != null) trace.exit(CntAUpdateUsed.class);
  }

  /**
  * The action handler performs all actions related to the received message (reply),
  * and optionally returns a request Message.  If there is no request, null is returned.
  */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(CntAUpdateUsed.class, "runAction(Connection)");

    Object[] objSet = ((Obj_List_Co) getMsgDataSet()).objs;
    FetchedDataCache cache = getFetchedDataCache();

    Obj_List_Co cntIDsStamps = (Obj_List_Co) objSet[0];
    Obj_List_Co adrIDsStamps = (Obj_List_Co) objSet[1];
    Obj_List_Co shrIDsStamps = (Obj_List_Co) objSet[2];

    if (cntIDsStamps != null && cntIDsStamps.objs != null && cntIDsStamps.objs.length >= 2) {
      Object[] ids = (Object[]) cntIDsStamps.objs[0];
      Object[] usedStamps = (Object[]) cntIDsStamps.objs[1];
      ArrayList recsL = new ArrayList();
      for (int i=0; i<ids.length; i++) {
        Long id = (Long) ids[i];
        Timestamp used = (Timestamp) usedStamps[i];
        ContactRecord rec = cache.getContactRecord(id);
        if (rec != null) {
          rec.dateUsed = used;
          recsL.add(rec);
        }
      }
      if (recsL.size() > 0) {
        ContactRecord[] recs = (ContactRecord[]) ArrayUtils.toArray(recsL, ContactRecord.class);
        cache.addContactRecords(recs);
//        System.out.println("updating used contacts "+Misc.objToStr(recs));
      }
    }

    if (adrIDsStamps != null && adrIDsStamps.objs != null && adrIDsStamps.objs.length >= 2) {
      Object[] ids = (Object[]) adrIDsStamps.objs[0];
      Object[] usedStamps = (Object[]) adrIDsStamps.objs[1];
      ArrayList recsL = new ArrayList();
      for (int i=0; i<ids.length; i++) {
        Long id = (Long) ids[i];
        Timestamp used = (Timestamp) usedStamps[i];
        MsgLinkRecord rec = cache.getMsgLinkRecord(id);
        if (rec != null) {
          rec.dateUsed = used;
          recsL.add(rec);
        }
      }
      if (recsL.size() > 0) {
        MsgLinkRecord[] recs = (MsgLinkRecord[]) ArrayUtils.toArray(recsL, MsgLinkRecord.class);
        cache.addMsgLinkRecords(recs);
//        System.out.println("updating used addresses "+Misc.objToStr(recs));
      }
    }

    if (shrIDsStamps != null && shrIDsStamps.objs != null && shrIDsStamps.objs.length >= 2) {
      Object[] ids = (Object[]) shrIDsStamps.objs[0];
      Object[] usedStamps = (Object[]) shrIDsStamps.objs[1];
      ArrayList recsL = new ArrayList();
      for (int i=0; i<ids.length; i++) {
        Long id = (Long) ids[i];
        Timestamp used = (Timestamp) usedStamps[i];
        FolderShareRecord rec = cache.getFolderShareRecord(id);
        if (rec != null) {
          rec.dateUsed = used;
          recsL.add(rec);
        }
      }
      if (recsL.size() > 0) {
        FolderShareRecord[] recs = (FolderShareRecord[]) ArrayUtils.toArray(recsL, FolderShareRecord.class);
        cache.addFolderShareRecords(recs);
//        System.out.println("updating used shares "+Misc.objToStr(recs));
      }
    }

    if (trace != null) trace.exit(CntAUpdateUsed.class, null);
    return null;
  }

}