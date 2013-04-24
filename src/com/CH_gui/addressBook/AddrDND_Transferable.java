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

package com.CH_gui.addressBook;

import java.awt.datatransfer.*;
import java.io.*;

import com.CH_co.service.records.*;

import com.CH_gui.msgTable.*;
import com.CH_gui.recycleTable.*;

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
 * <b>$Revision: 1.2 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class AddrDND_Transferable extends Object implements Transferable {

  public static DataFlavor ADDR_RECORD_FLAVOR = new DataFlavor(AddrDND_TransferableData.class, "AddrRecordIDs");
  private DataFlavor flavors[] = { ADDR_RECORD_FLAVOR, RecycleDND_Transferable.RECYCLE_RECORD_FLAVOR };
  private AddrDND_TransferableData data;

  public AddrDND_Transferable(MsgLinkRecord[] msgLinks) {
    data = new AddrDND_TransferableData(msgLinks);
  }

  public DataFlavor[] getTransferDataFlavors() {
    return flavors;
  }

  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    Object returnObject;
    if (flavor.equals(ADDR_RECORD_FLAVOR)) {
      returnObject = data;
    } else if (flavor.equals(RecycleDND_Transferable.RECYCLE_RECORD_FLAVOR)) {
      returnObject = new RecycleDND_TransferableData(data);
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
    return returnObject;
  }

  public boolean isDataFlavorSupported(DataFlavor flavor) {
    boolean isSupported = false;
    if (!isSupported) isSupported = flavor.equals(ADDR_RECORD_FLAVOR);
    if (!isSupported) isSupported = flavor.equals(RecycleDND_Transferable.RECYCLE_RECORD_FLAVOR);
    return isSupported;
  }
} // end class AddrDND_Transferable