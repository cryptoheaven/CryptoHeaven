/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.localFileTable;

import java.awt.datatransfer.*;
import java.util.List;
import java.io.*;
import java.util.*;

import com.CH_co.trace.Trace;

/** 
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.10 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class LocalFileDND_Transferable extends Object implements Transferable {

  private DataFlavor flavors[] = { DataFlavor.javaFileListFlavor };
  private List data;

  protected LocalFileDND_Transferable(File[] files) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(LocalFileDND_Transferable.class, "LocalFileDND_Transferable(File[] files)");
    this.data = new LinkedList(Arrays.asList(files));
    if (trace != null) trace.exit(LocalFileDND_Transferable.class);
  }

  public DataFlavor[] getTransferDataFlavors() {
    return flavors;
  }

  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    Object returnObject;
    if (flavor.equals(DataFlavor.javaFileListFlavor)) {
      returnObject = data;
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
    return returnObject;
  }

  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return (flavor.equals(DataFlavor.javaFileListFlavor));
  }
} // end class LocalFileDND_Transferable