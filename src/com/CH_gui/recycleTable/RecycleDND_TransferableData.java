/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
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
 * <b>Copyright</b> &copy; 2001-2009
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
 * <b>$Revision: 1.1 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class RecycleDND_TransferableData extends Object implements Serializable {
  public Long[][] recycleRecordIDs;
  public RecycleDND_TransferableData() {
  }
  public RecycleDND_TransferableData(FolderPair[] fPairs, FileLinkRecord[] fLinks, MsgLinkRecord[] mLinks) {
    recycleRecordIDs = new Long[3][];
    recycleRecordIDs[0] = RecordUtils.getIDs(fPairs);
    recycleRecordIDs[1] = RecordUtils.getIDs(fLinks);
    recycleRecordIDs[2] = RecordUtils.getIDs(mLinks);
  }
  public RecycleDND_TransferableData(FolderDND_TransferableData folderDND, FileDND_TransferableData fileDND, MsgDND_TransferableData msgDND) {
    recycleRecordIDs = new Long[3][];
    recycleRecordIDs[0] = folderDND != null ? folderDND.folderIDs : null;
    if (fileDND != null)
      recycleRecordIDs[0] = (Long[]) ArrayUtils.concatinate(recycleRecordIDs[0], fileDND.fileRecordIDs[0], Long.class);
    recycleRecordIDs[1] = fileDND != null ? fileDND.fileRecordIDs[1] : null;
    recycleRecordIDs[2] = msgDND != null ? msgDND.msgLinkIDs : null;
  }
  public RecycleDND_TransferableData(AddrDND_TransferableData addrDND) {
    recycleRecordIDs = new Long[3][];
    recycleRecordIDs[0] = null;
    recycleRecordIDs[1] = null;
    recycleRecordIDs[2] = addrDND != null ? addrDND.msgLinkIDs : null;
  }
}