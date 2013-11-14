/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.recycleTable;

import java.io.Serializable;

import com.CH_co.service.records.*;
import com.CH_co.util.*;

import com.CH_gui.addressBook.*;
import com.CH_gui.fileTable.*;
import com.CH_gui.msgTable.*;
import com.CH_gui.tree.*;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.1 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class RecycleDND_TransferableData extends Object implements Serializable {
  public Long[][] recycleRecordIDs;
  public RecycleDND_TransferableData() {
  }
  public RecycleDND_TransferableData(FolderPair[] fPairs, FileLinkRecord[] fLinks, FileLinkRecord[] fLinksAllVersions, MsgLinkRecord[] mLinks) {
    recycleRecordIDs = new Long[4][];
    recycleRecordIDs[0] = RecordUtils.getIDs(fPairs);
    recycleRecordIDs[1] = RecordUtils.getIDs(fLinks);
    recycleRecordIDs[2] = RecordUtils.getIDs(fLinksAllVersions);
    recycleRecordIDs[3] = RecordUtils.getIDs(mLinks);
  }
  public RecycleDND_TransferableData(FolderDND_TransferableData folderDND, FileDND_TransferableData fileDND, MsgDND_TransferableData msgDND) {
    recycleRecordIDs = new Long[4][];
    recycleRecordIDs[0] = folderDND != null ? folderDND.folderIDs : null;
    if (fileDND != null)
      recycleRecordIDs[0] = (Long[]) ArrayUtils.concatinate(recycleRecordIDs[0], fileDND.fileRecordIDs[0], Long.class);
    recycleRecordIDs[1] = fileDND != null ? fileDND.fileRecordIDs[1] : null;
    recycleRecordIDs[2] = fileDND != null ? fileDND.fileRecordIDs[2] : null;
    recycleRecordIDs[3] = msgDND != null ? msgDND.msgLinkIDs : null;
  }
  public RecycleDND_TransferableData(AddrDND_TransferableData addrDND) {
    recycleRecordIDs = new Long[4][];
    recycleRecordIDs[0] = null;
    recycleRecordIDs[1] = null;
    recycleRecordIDs[2] = null;
    recycleRecordIDs[3] = addrDND != null ? addrDND.msgLinkIDs : null;
  }
}