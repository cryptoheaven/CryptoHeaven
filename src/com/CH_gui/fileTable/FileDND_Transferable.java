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

package com.CH_gui.fileTable;

import java.awt.datatransfer.*;
import java.io.*;

import com.CH_co.service.records.*;

import com.CH_gui.recycleTable.*;

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
public class FileDND_Transferable extends Object implements Transferable {

  public static final DataFlavor FILE_RECORD_FLAVOR = new DataFlavor(FileDND_TransferableData.class, "FileRecordIDs");
  private static final DataFlavor flavors[] = { FILE_RECORD_FLAVOR, RecycleDND_Transferable.RECYCLE_RECORD_FLAVOR };
  private FileDND_TransferableData data;

  public FileDND_Transferable(FolderPair[] fPairs, FileLinkRecord[] fLinks, FileLinkRecord[] fLinksAllVersions) {
    data = new FileDND_TransferableData(fPairs, fLinks, fLinksAllVersions);
  }

  public DataFlavor[] getTransferDataFlavors() {
    return flavors;
  }

  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    Object returnObject;
    if (flavor.equals(FILE_RECORD_FLAVOR)) {
      returnObject = data;
    } else if (flavor.equals(RecycleDND_Transferable.RECYCLE_RECORD_FLAVOR)) {
      returnObject = new RecycleDND_TransferableData(null, data, null);
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
    return returnObject;
  }

  public boolean isDataFlavorSupported(DataFlavor flavor) {
    boolean isSupported = false;
    if (!isSupported) isSupported = flavor.equals(FILE_RECORD_FLAVOR);
    if (!isSupported) isSupported = flavor.equals(RecycleDND_Transferable.RECYCLE_RECORD_FLAVOR);
    return isSupported;
  }
} // end class FileDND_Transferable