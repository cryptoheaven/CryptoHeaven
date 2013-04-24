/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_gui.recycleTable;

import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;

import com.CH_co.service.records.*;
import com.CH_co.util.*;

import com.CH_gui.fileTable.*;
import com.CH_gui.msgTable.*;
import com.CH_gui.tree.*;

/** 
 * <b>Copyright</b> &copy; 2001-2013
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
 * <b>$Revision: 1.1 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class RecycleDND_Transferable extends Object implements Transferable {

  public static final DataFlavor RECYCLE_RECORD_FLAVOR = new DataFlavor(RecycleDND_TransferableData.class, "RecycleRecordIDs");
  private RecycleDND_TransferableData data;
  private FileDND_TransferableData fileData;
  private FolderDND_TransferableData folderData;
  private MsgDND_TransferableData msgData;

  public RecycleDND_Transferable(FolderPair[] fPairs, FileLinkRecord[] fLinks, FileLinkRecord[] fLinksAllVersions, MsgLinkRecord[] mLinks) {
    data = new RecycleDND_TransferableData(fPairs, fLinks, fLinksAllVersions, mLinks);
    if (any(mLinks)) {
      if (none(fPairs) && none(fLinks))
        msgData = new MsgDND_TransferableData(mLinks);
    } else {
      if (any(fPairs) || any(fLinks))
        fileData = new FileDND_TransferableData(fPairs, fLinks, fLinksAllVersions);
      if (any(fPairs) && none(fLinks))
        folderData = new FolderDND_TransferableData(RecordUtils.getIDs(fPairs));
    }
  }

  private boolean any(Object[] objs) {
    return objs != null && objs.length > 0;
  }
  private boolean none(Object[] objs) {
    return objs == null || objs.length == 0;
  }

  public DataFlavor[] getTransferDataFlavors() {
    Vector flavoursV = new Vector();
    flavoursV.addElement(RECYCLE_RECORD_FLAVOR);
    if (fileData != null)
      flavoursV.addElement(FileDND_Transferable.FILE_RECORD_FLAVOR);
    if (folderData != null)
      flavoursV.addElement(FolderDND_Transferable.FOLDER_RECORD_FLAVOR);
    if (msgData != null)
      flavoursV.addElement(MsgDND_Transferable.MSG_RECORD_FLAVOR);
    DataFlavor[] flavors = (DataFlavor[]) ArrayUtils.toArray(flavoursV, DataFlavor.class);
    return flavors;
  }

  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    Object returnObject;
    if (flavor.equals(RECYCLE_RECORD_FLAVOR)) {
      returnObject = data;
    } else if (flavor.equals(FileDND_Transferable.FILE_RECORD_FLAVOR)) {
      returnObject = fileData;
    } else if (flavor.equals(FolderDND_Transferable.FOLDER_RECORD_FLAVOR)) {
      returnObject = folderData;
    } else if (flavor.equals(MsgDND_Transferable.MSG_RECORD_FLAVOR)) {
      returnObject = msgData;
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
    return returnObject;
  }

  public boolean isDataFlavorSupported(DataFlavor flavor) {
    boolean isSupported = flavor.equals(RECYCLE_RECORD_FLAVOR);
    if (!isSupported && flavor.equals(FileDND_Transferable.FILE_RECORD_FLAVOR) && fileData != null) {
      isSupported = true;
    }
    if (!isSupported && flavor.equals(FolderDND_Transferable.FOLDER_RECORD_FLAVOR) && folderData != null) {
      isSupported = true;
    }
    if (!isSupported && flavor.equals(MsgDND_Transferable.MSG_RECORD_FLAVOR) && msgData != null) {
      isSupported = true;
    }
    return isSupported;
  }
} // end class RecycleDND_Transferable