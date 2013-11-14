/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.folder;

import java.util.EventObject;

import com.CH_co.service.records.FolderRecord;
import com.CH_co.trace.Trace;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.7 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class FolderSelectionEvent extends EventObject {

  public FolderRecord selectedFolderRecord;

  /** Creates new FolderSelectionEvent */
  public FolderSelectionEvent(Object source, FolderRecord fRec) {
    super(source);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderSelectionEvent.class, "FolderSelectionEvent(Object source, FolderRecord fRec)");
    this.selectedFolderRecord = fRec;
    if (trace != null) trace.exit(FolderSelectionEvent.class);
  }

}