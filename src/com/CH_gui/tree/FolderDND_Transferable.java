/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.tree;

import com.CH_gui.recycleTable.*;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.9 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class FolderDND_Transferable implements Transferable {

  public static final DataFlavor FOLDER_RECORD_FLAVOR = new DataFlavor(FolderDND_TransferableData.class, "FolderIDs");
  private static final DataFlavor flavors[] = { FOLDER_RECORD_FLAVOR, RecycleDND_Transferable.RECYCLE_RECORD_FLAVOR };
  private FolderDND_TransferableData data;

  protected FolderDND_Transferable(Long[] folderIDs) {
    data = new FolderDND_TransferableData(folderIDs);
  }

  public DataFlavor[] getTransferDataFlavors() {
    return flavors;
  }

  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    Object returnObject;
    if (flavor.equals(FOLDER_RECORD_FLAVOR)) {
      returnObject = data;
    } else if (flavor.equals(RecycleDND_Transferable.RECYCLE_RECORD_FLAVOR)) {
      returnObject = new RecycleDND_TransferableData(data, null, null);
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
    return returnObject;
  }

  public boolean isDataFlavorSupported(DataFlavor flavor) {
    boolean isSupported = false;
    if (!isSupported) isSupported = flavor.equals(FOLDER_RECORD_FLAVOR);
    if (!isSupported) isSupported = flavor.equals(RecycleDND_Transferable.RECYCLE_RECORD_FLAVOR);
    return isSupported;
  }
} // end class FolderDND_Transferable