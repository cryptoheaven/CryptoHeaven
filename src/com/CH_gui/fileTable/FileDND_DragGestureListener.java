/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.fileTable;

import java.awt.dnd.*;

import com.CH_co.trace.Trace;
import com.CH_co.service.records.*;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.10 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class FileDND_DragGestureListener extends Object implements DragGestureListener {

  private FileActionTable fileActionTable;

  /** Creates new FileDND_DragGestureListener */
  protected FileDND_DragGestureListener(FileActionTable fileActionTable) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileDND_DragGestureListener.class, "FileDND_DragGestureListener(FileActionTable fileActionTable)");
    if (trace != null) trace.args(fileActionTable);
    this.fileActionTable = fileActionTable;
    if (trace != null) trace.exit(FileDND_DragGestureListener.class);
  }

  public void dragGestureRecognized(DragGestureEvent event) {
    FolderPair[] fPairs = (FolderPair[]) fileActionTable.getSelectedInstancesOf(FolderPair.class);
    FileLinkRecord[] fLinks = (FileLinkRecord[]) fileActionTable.getSelectedInstancesOf(FileLinkRecord.class);
    if ((fPairs != null && fPairs.length > 0) ||
        (fLinks != null && fLinks.length > 0))
    {
      FileLinkRecord[] fLinkAllVersions = null;
      if (fileActionTable.getTableModel().getIsCollapseFileVersions())
        fLinkAllVersions = (FileLinkRecord[]) fileActionTable.getSelectedInstancesOf(FileLinkRecord.class, true);
      FileDND_Transferable transferable = new FileDND_Transferable(fPairs, fLinks, fLinkAllVersions);
      // as the name suggests, starts the dragging
      event.getDragSource().startDrag(event, null, transferable, new FileDND_DragSourceListener());
    } else {
      //System.out.println( "nothing was selected");   
    }
  }
} // end class FileDND_DragGestureListener