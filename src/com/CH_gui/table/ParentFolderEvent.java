/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.table;

import java.util.EventObject;

import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.7 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class ParentFolderEvent extends EventObject {

  private RecordTableModel recordTableModel;
  private FolderPair prevFolderPair;
  private FolderPair newFolderPair;

  /** Creates new ParentFolderEvent */
  public ParentFolderEvent(RecordTableModel tableModel, FolderPair prevFolder, FolderPair newFolder) {
    super(tableModel);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ParentFolderEvent.class, "(RecordTableModel tableModel, FolderPair prevFolder, FolderPair newFolder)");
    if (trace != null) trace.args(tableModel, prevFolder, newFolder);
    recordTableModel = tableModel;
    prevFolderPair = prevFolder;
    newFolderPair = newFolder;
    if (trace != null) trace.exit(ParentFolderEvent.class);
  }

  public RecordTableModel getTableModel() {
    return recordTableModel;
  }
  public FolderPair getPrevFolder() {
    return prevFolderPair;
  }
  public FolderPair getNewFolder() {
    return newFolderPair;
  }
}